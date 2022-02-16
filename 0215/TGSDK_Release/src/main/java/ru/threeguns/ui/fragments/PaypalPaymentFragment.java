package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.network.HClient;
import kh.hyper.ui.HFragment;
import ru.threeguns.R;
import ru.threeguns.engine.billing.Paypal2PaymentManagerImpl;
import ru.threeguns.engine.billing.PaypalPaymentManagerImpl;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.tp.GooglePay;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.PaymentApi;
import ru.threeguns.ui.dialog.LoadingDialog;
import ru.threeguns.ui.dialog.LoadingDialog2;

public class PaypalPaymentFragment extends HFragment {
  GooglePay pay;
  LoadingDialog dialog;
  private String paymentType = "Alipay";
  private ImageView tg_alipay_choose, tg_paypal_choose, tg_wx_choose, tg_google_choose;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    int tg_fragment_paypal_id = getActivity().getResources().getIdentifier("tg_fragment_paypal3", "layout", getActivity().getPackageName());
    View v = inflater.inflate(tg_fragment_paypal_id, null);
    pay = new GooglePay(getContext());
    dialog = new LoadingDialog(getContext());

    tg_alipay_choose = v.findViewById(R.id.tg_alipay_choose);
    tg_paypal_choose = v.findViewById(R.id.tg_paypal_choose);
    tg_wx_choose = v.findViewById(R.id.tg_wx_choose);
    tg_google_choose = v.findViewById(R.id.tg_google_choose);

    int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requestBack();
      }
    });

    int tg_paypal_img_id = getActivity().getResources().getIdentifier("tg_paypal_img", "id", getActivity().getPackageName());
    v.findViewById(tg_paypal_img_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Module.of(UIHelper.class).post2MainThread(new Runnable() {
          public void run() {
            paymentType = "Paypal";
            setnormal();
            tg_paypal_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_selected));
          }
        });
      }
    });

    int tg_google_img_id = getActivity().getResources().getIdentifier("tg_google_img", "id", getActivity().getPackageName());
    v.findViewById(tg_google_img_id).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Module.of(UIHelper.class).post2MainThread(new Runnable() {
          public void run() {
            paymentType = "google";
            setnormal();
            tg_google_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_selected));
          }
        });
      }
    });

    int tg_alipay_img_id = getActivity().getResources().getIdentifier("tg_alipay_img", "id", getActivity().getPackageName());
    v.findViewById(tg_alipay_img_id).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Module.of(UIHelper.class).post2MainThread(new Runnable() {
          public void run() {
            paymentType = "Alipay";
            setnormal();
            tg_alipay_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_selected));
          }
        });
      }
    });

    int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (paymentType.equals("Alipay") || paymentType.equals("Paypal")) {
          HClient.of(PaymentApi.class).createOrder(paymentType, PaypalPaymentManagerImpl.paymentRequestHolder.toParams(), new TGResultHandler() {
            @Override
            protected void onSuccess(JSONObject jsonData) throws JSONException {
              Module.of(UIHelper.class).showProgressDialog(TGString.network_loading_pay);

              final String orderId = jsonData.getString("order_id");
              Module.of(Paypal2PaymentManagerImpl.class).doPay(orderId);
              getActivity().finish();
            }

            @Override
            protected void onFailed(int errorCode, String msg) {
              EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.FAILED, null));
              getActivity().finish();
            }
          });
        } else if (paymentType.equals("google")) {
          googlepay();
        }

      }
    });

    return v;
  }

  private void setnormal() {
    tg_alipay_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_normal));
    tg_paypal_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_normal));
    tg_wx_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_normal));
    tg_google_choose.setImageDrawable(getResources().getDrawable(R.drawable.tg_hero_normal));
  }

  private void googlepay() {
    pay.doPay(Constants.request);
    pay.setOnClose(new GooglePay.OnClose() {
      @Override
      public void close() {
        if (null == getActivity()) {
          return;
        }
        dialog.dismiss();
        requestBack();
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (paymentType.equals("google")) {
      dialog.show();
    }
  }

  private void pay() {
    HClient.of(PaymentApi.class).createOrder(paymentType, PaypalPaymentManagerImpl.paymentRequestHolder.toParams(), new TGResultHandler() {

      @Override
      protected void onSuccess(JSONObject jsonData) throws JSONException {
        Module.of(UIHelper.class).showProgressDialog(TGString.network_loading_pay);
        final String orderId = jsonData.getString("order_id");
        Module.of(Paypal2PaymentManagerImpl.class).doPay(orderId);
        getActivity().finish();
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.FAILED, null));
        getActivity().finish();
      }
    });
  }

  @Override
  protected void onExit() {
    super.onExit();
    EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.USER_CANCEL, null));
  }
}
