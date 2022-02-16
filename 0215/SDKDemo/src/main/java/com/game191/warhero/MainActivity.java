package com.game191.warhero;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ru.threeguns.engine.TGPlatform;
import ru.threeguns.entity.User;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.event.handler.PaymentHandler;
import ru.threeguns.event.handler.UserLoginHandler;
import ru.threeguns.event.handler.UserLogoutHandler;
import ru.threeguns.manager.PaymentManager;
import ru.threeguns.manager.UserManager;
import ru.threeguns.network.TGResultHandler;

public class MainActivity extends Activity {
  protected String TAG = "DemoMainActivity";
  private Button loginBtn;
  private Button logoutBtn;
  private Button payBtn;
  private Button orientationBtn;
  private TextView loginStateText;

  private Button tgdemo_selectsever;
  private Button tgdemo_ceshi;

  private Handler handler;


  private boolean land = false;

  private String strings1 = "{\"data\":\"服务器111\"}";

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    setContentView(R.layout.test_activity_main);

    TGPlatform.getInstance().init(this, new TGPlatform.InitializeCallback() {

      @Override
      public void onInitializeComplete(int code) {
        switch (code) {
          case TGPlatform.InitializeCallback.SUCCESS:
            handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
              @Override
              public void run() {
                selfInit();
              }
            });
            break;
          default:
            break;
        }
      }
    });
    TGPlatform.getInstance().createToolbar(MainActivity.this);
  }


  private void selfInit() {
    TGPlatform.getInstance().getUserManager().setOnUserLogoutListener(new UserManager.OnUserLogoutListener() {
      @Override
      public void onUserLogoutInternel(final User user) {
        handler.post(new Runnable() {
          @Override
          public void run() {
            loginStateText.setText("Not Login.");
            Toast.makeText(MainActivity.this, user.getNickname() + " Logout Success.", Toast.LENGTH_LONG).show();
          }
        });
      }
    });

    loginStateText = (TextView) findViewById(R.id.tgdemo_login_state);
    tgdemo_selectsever = (Button) findViewById(R.id.tgdemo_selectsever);
    tgdemo_ceshi = (Button) findViewById(R.id.tgdemo_ceshi);
    tgdemo_ceshi.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {}
    });
    loginStateText.setText("Not Login.");
    loginBtn = (Button) findViewById(R.id.tgdemo_login);
    loginBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        TGPlatform.getInstance().getUserManager().requestLogin(MainActivity.this, new UserLoginHandler() {
          @Override
          protected void onLoginSuccess(final User user, final boolean tgregister) {
            handler.post(new Runnable() {
              @Override
              public void run() {
                loginStateText.setText("Hello! " + user.getNickname());
                Toast.makeText(MainActivity.this, user.getNickname() + " Login Successed.", Toast.LENGTH_LONG).show();
              }
            });
          }

          @Override
          protected void onUserCancel() {
            showToast("User Canceled Login.");
          }

          @Override
          protected void onLoginFailed() {
            showToast("Login Failed.");
          }
        });

      }
    });

    tgdemo_selectsever.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TGPlatform.getInstance().getUserManager().TgAction(strings1, new TGResultHandler() {
          @Override
          protected void onSuccess(JSONObject json) throws JSONException {

          }

          @Override
          protected void onFailed(int errorCode, String msg) {

          }
        });
      }
    });

    logoutBtn = (Button) findViewById(R.id.tgdemo_logout);
    logoutBtn.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        TGPlatform.getInstance().getUserManager().requestLogout(//
            new UserLogoutHandler() {

              @Override
              protected void onUserNotLogin() {
                showToast("You haven't Logged in.");
              }

              @Override
              protected void onLogoutSuccess(final User user) {
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    loginStateText.setText("Not Login.");
                    Toast.makeText(MainActivity.this, user.getNickname() + " Logout Success.", Toast.LENGTH_LONG).show();
                  }
                });
              }
            });
      }
    });

    payBtn = (Button) findViewById(R.id.tgdemo_pay);
    payBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        PaymentManager.PaymentRequest request = new PaymentManager.PaymentRequest();
        request.setProductId("com.hero.conquer.4006");// 商品id
        request.setProductName("Regalo de recién llegado");// 商品名称
        request.setProductDescription("Regalo de recién llegado");// 商品描述
        request.setAmount(0.01f);// 商品价格,以元为单位
        request.setCurrency("CNY");//货币类型: USD - 美元；CNY - 人民币，必需
        request.setServerId("50");// 服务器ID
        request.setGameExtra("GameExtra");// 透传参数,该字段将原样回调给应用服务器

        PaymentManager paymentManager = TGPlatform.getInstance().getPaymentManager();
        paymentManager.requestPay(MainActivity.this, request, new PaymentHandler() {
          @Override
          protected void onPaymentSuccess(PaymentEvent event) {
            showToast("Payment Success , orderId : " + event.getOrderId());
            Log.e(TAG, "onPaymentSuccess: " + event.getOrderId());
          }

          @Override
          protected void onPaymentFailed() {
            showToast("Payment Failed.");
          }

          @Override
          protected void onPaymentInProcess() {
            showToast("Payment InProcess.");
          }

          @Override
          protected void onUserCancel() {
            showToast("User Cancel Payment.");
          }

        });
      }
    });

    //TODO added by sxg on 2021.10.10 9:56, for alphapay
    findViewById(R.id.btnAlphaPay).setOnClickListener(v -> {

    });


    orientationBtn =findViewById(R.id.btnChangeOrientation);
    orientationBtn.setOnClickListener(v -> {
      if (land) {
        land = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        orientationBtn.setText("竖版");
      } else {
        land = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        orientationBtn.setText("横版");
      }
    });

  }

  protected void showToast(final String message) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  protected void onStart() {
    Log.e(TAG, "Demo MainActivity onStart");
    TGPlatform.getInstance().onActivityStart(this);
    super.onStart();
  }

  @Override
  protected void onResume() {
    TGPlatform.getInstance().onActivityResume(this);
    super.onResume();
  }

  @Override
  protected void onPause() {
    TGPlatform.getInstance().onActivityPause(this);
    super.onPause();
  }

  @Override
  protected void onStop() {
    Log.e(TAG, "Demo MainActivity onStop");
    TGPlatform.getInstance().onActivityStop(this);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    Log.e(TAG, "Demo MainActivity onDestroy");
    TGPlatform.getInstance().releaseToolbar(this);
    TGPlatform.getInstance().release();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    TGPlatform.getInstance().onActivityResult(this, requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
  }
}
