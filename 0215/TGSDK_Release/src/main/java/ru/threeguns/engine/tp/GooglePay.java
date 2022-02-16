package ru.threeguns.engine.tp;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.network.HClient;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.manager.PaymentManager;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.network.NetworkCode;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.PaymentApi;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.TimeUnit;

//该文件好像用不到
public class GooglePay {
  private static final String GOOGLE_PAYMENT_TYPE = "google";
  @SuppressWarnings("unused")
  private static final int UPDATEORDER_RETRY_COUNT = 5;
  public OnClose onClose;
  private boolean purchaseInProgress;
  private Context applicationContext;
  // for 4.0.0 iap api
  private BillingClient mBillingClient;
  private MyPurchasesUpdatedListener purchasesUpdatedListener = new MyPurchasesUpdatedListener();
  private BillingClient.Builder builder;
  private Activity activity;
  private PaymentManager.PaymentRequest request;

  public GooglePay(Context context) {
    applicationContext = context.getApplicationContext();
    activity = Module.of(ActivityHolder.class).getTopActivity();

    if (mBillingClient==null) {
      builder = BillingClient.newBuilder(activity);
      mBillingClient = builder.setListener(purchasesUpdatedListener).enablePendingPurchases().build();
    } else {
      builder.setListener(purchasesUpdatedListener);
    }

    // 与 Google Play 建立连接
    startConnection();
  }

  public void setOnClose(OnClose onCloses) {
    this.onClose = onCloses;
  }

  protected void onRelease() {
    purchaseInProgress = false;
    applicationContext = null;

    builder = null;
    mBillingClient = null;
    activity = null;
  }

  public void doPay(final PaymentManager.PaymentRequest request) {
    //检查是否支持google pay
    Map<String, String> paymentParams = request.toParams();

    HClient.of(PaymentApi.class).createOrder(GOOGLE_PAYMENT_TYPE, paymentParams, new TGResultHandler() {
      @Override
      protected void onSuccess(JSONObject json) throws JSONException {
        final String orderId = json.getString("order_id");
        Module.of(UIHelper.class).post2MainThread(new Runnable() {
          public void run() {
            requestGooglePay(request, orderId);
          }
        });
        Module.of(TrackManager.class).trackEvent(//
            new TrackManager.TrackEvent(TrackManager.CREATE_ORDER)//
                .setExtraParams("orderId", orderId)//
                .setExtraParams("amount", request.getAmount() + "")//
                .setExtraParams("productId", request.getProductId())//
                .setExtraParams("currency", request.getCurrency())//
                .setExtraParams("gameCoinAmount", request.getGameCoinAmount() + "")//
                .setExtraParams("paymentType", GOOGLE_PAYMENT_TYPE)//
        );
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        Module.of(UIHelper.class).showToast("Pay Failed");
      }
    });
  }

  private void verifyPurchase(final Purchase purchase, boolean background, int retryCount) {
    final String orderId = purchase.getDeveloperPayload();
    final String purchaseToken = purchase.getPurchaseToken();

    final int c = ++retryCount;

    String userId = Module.of(UserCenter.class).getUserId();
    HClient.of(PaymentApi.class).updateOrder(PaymentApi.SUCCESS, userId, purchase.getOriginalJson(), purchase.getSignature(), new TGResultHandler() {
      @Override
      protected void onSuccess(JSONObject json) throws JSONException {
        HL.i("onUpdateOrderSuccess orderId : " + orderId);
        HL.i("onUpdateOrderSuccess Consume this purchase.");
        EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.SUCCESS, orderId));
        consumeAsync(purchaseToken);
        Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent(TrackManager.VERIFIED_ORDER).setExtraParams("orderId", orderId));
        onClose.close();
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        HL.i("onUpdateOrderFailed orderId : " + orderId);
        if (errorCode < 0 || errorCode==NetworkCode.SERVER_ERROR) {
          HL.i("Server or network error , Try to update it one minute later.");
          Module.of(UIHelper.class).post2MainThreadDelayed(new Runnable() {
            @Override
            public void run() {
              verifyPurchase(purchase, true, c);
            }
          }, 1 * TimeUnit.MINUTE);
        } else {
          HL.i("Sign error , Consume this purchase.");
          consumeAsync(purchaseToken);
        }
        if(onClose!=null) onClose.close();
      }
    });


  }

  public void requestGooglePay(PaymentManager.PaymentRequest request, final String orderId) {
    this.request = request;
    if (purchaseInProgress) {
      HL.i("Another Payment is in progress. Create payment failed.");
      return;
    }
    if (mBillingClient==null || !startConnection()) {
      Module.of(UIHelper.class).showToast("Connect Google Failed");
      return;
    }

    builder.setListener(purchasesUpdatedListener);
    List<String> skuList = new ArrayList<>();
    skuList.add(request.getProductId());
    SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build();
    mBillingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
      @Override
      public void onSkuDetailsResponse(BillingResult responseResult, List<SkuDetails> skuDetailsList) {
        if (responseResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
          if (skuDetailsList!=null && !skuDetailsList.isEmpty()) {
            purchaseInProgress = true;
            final BillingFlowParams.Builder flowParamsBuilder = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList.get(0));
            //添加自定义订单信息
            flowParamsBuilder.setObfuscatedAccountId(orderId);
            flowParamsBuilder.setObfuscatedProfileId(applicationContext.getPackageName());

            BillingResult billingResult = mBillingClient.launchBillingFlow(activity, flowParamsBuilder.build());
            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();
            HL.w(">>>>>launchBillingFlow: BillingResponse " + responseCode + " " + debugMessage);
          } else {
            Module.of(UIHelper.class).showToast("Google Pay PID Error");
          }
        } else {
          Module.of(UIHelper.class).showToast(responseResult.getResponseCode() + " Get PID Failed"+responseResult.getDebugMessage());
          HL.e("sxg google pay error,BillingResponse: " + responseResult.getResponseCode() + " " + responseResult.getDebugMessage());
        }
      }
    });
  }

  private boolean startConnection() {
    if (mBillingClient==null) {
      HL.i("初始化失败:mBillingClient==null");
      return false;
    }

    if (!mBillingClient.isReady()) {
      mBillingClient.startConnection(new BillingClientStateListener() {
        // 初始化失败
        @Override
        public void onBillingServiceDisconnected() {}

        @Override
        public void onBillingSetupFinished(BillingResult billingResult) {
          if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
            // 查询为消耗订单
            queryPurchases();
          } else { // 初始化失败

          }
        }
      });
      return false;
    }
    return true;
  }

  private List<Purchase> queryPurchases() {
    if (mBillingClient==null) {
      return null;
    }
    if (!mBillingClient.isReady()) {
      startConnection();
    } else {
      Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
      if (purchasesResult!=null) {
        if (purchasesResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
          List<Purchase> purchaseList = purchasesResult.getPurchasesList();
          if (purchaseList!=null && !purchaseList.isEmpty()) {
            for (Purchase purchase : purchaseList) {
              if (purchase.getPurchaseState()==Purchase.PurchaseState.PURCHASED) {
                consumeAsync(purchase.getPurchaseToken());
              }
            }
          }
          return purchaseList;
        }
      }
    }
    return null;
  }

  /**
   * 消耗商品
   *
   * @param purchaseToken {@link Purchase#getPurchaseToken()}
   */
  public void consumeAsync(String purchaseToken) {
    if (mBillingClient==null) {
      return;
    }
    ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();
    mBillingClient.consumeAsync(consumeParams, new MyConsumeResponseListener());
  }

  public interface OnClose {
    void close();
  }

  /**
   * Googlg消耗商品回调
   */
  private class MyConsumeResponseListener implements ConsumeResponseListener {
    @Override
    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
      if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK) {
        // 消耗成功
      } else {
        // 消耗失败
      }
    }
  }

  /**
   * Google购买商品回调接口(订阅和内购都走这个接口)
   */
  private class MyPurchasesUpdatedListener implements PurchasesUpdatedListener {
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
      if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK && list!=null) {
        for (Purchase purchase : list) {
          if (purchase.getPurchaseState()==Purchase.PurchaseState.PURCHASED) {
            verifyPurchase(purchase, true, 0);
          } else if (purchase.getPurchaseState()==Purchase.PurchaseState.PENDING) {
            HL.i("待处理的订单:" + purchase.getDeveloperPayload());
          }
        }
      } else {
        Module.of(UIHelper.class).showToast("Pay Failed");
        if(onClose!=null) onClose.close();
        HL.e("购买失败,responseCode:" + billingResult.getResponseCode() + ",msg:" + billingResult.getDebugMessage());
      }
    }
  }
}
