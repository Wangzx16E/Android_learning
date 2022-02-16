package ru.threeguns.engine.tp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.Handle;
import kh.hyper.event.HandleThreadType;
import kh.hyper.network.HClient;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.event.ActivityResultEvent;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.utils.ActivityHolder;

public final class Google extends ThirdPlatform {
  private static final int REQUEST_CODE_GP_LOGIN = 15674;
  private static final String TAG = "tpGoogle";
  private GoogleApiClient googleClient;

  private String gcmSenderId;
  private String token;

  @Override
  protected void onLoad(Parameter parameter) {
    super.onLoad(parameter);
    String serverClientId = parameter.optString("server_client_id");
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestServerAuthCode(serverClientId)
        .requestProfile()
        .requestEmail()
        .build();

    googleClient = new GoogleApiClient.Builder(getContext()).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
      @Override
      public void onConnectionFailed(ConnectionResult result) {
        HL.w("onConnectionFailed : {}", result);
      }
    }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

    //响应谷歌登录返回信息
    new TGHandler() {
      @Handle
      public void onActivityResult(ActivityResultEvent e) {
        if (e.getRequestCode() == REQUEST_CODE_GP_LOGIN) {
          GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(e.getData());

          if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e("TAG", "onActivityResult: " + acct.getServerAuthCode());
            HClient.of(UserApi.class).tpLogin("google", acct.getServerAuthCode(), null, new TGResultHandler() {
              @Override
              protected void onSuccess(JSONObject json) throws JSONException {
                String first_login = json.getString("first_login");
                if ("1".equals(first_login)) {
                  Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent("total_register"));
                }

                Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json), false);
                Activity act = Module.of(ActivityHolder.class).getTopActivity();
                if (act instanceof LoginActivity) {
                  act.finish();
                }
                Module.of(UIHelper.class).closeProgressDialog();
              }

              @Override
              protected void onFailed(int errorCode, String msg) {
                Module.of(UIHelper.class).showToast("Google Login Failed.(" + msg + ")");
                HL.w(">>>>>>>>>>>>>>>>>Google Login Failed Code:" + errorCode + " msg:" + msg);
                Module.of(UIHelper.class).closeProgressDialog();
              }
            });
          } else {
            Module.of(UIHelper.class).showToast("Google login failed");
            if (result.getStatus() != null) {
              HL.w("Google Login fail : {} , {}", result.getStatus().getStatusCode(), result.getStatus().getStatusMessage());
              Log.e(TAG, "onActivityResult: "+result.getStatus().getStatusCode()+","+ result.getStatus().getStatusMessage() );
            } else {
              HL.w("Google Login fail : null status.");
            }
          }
        }
      }

    }.register();

    if (Constants.GCM_ENABLED) {
      gcmSenderId = parameter.optString("sender_id");
      new TGHandler() {
        @Handle(HandleThreadType.ANOTHER)
        public void onUserLogin(UserLoginEvent event) {
          if (event.getResult() == UserLoginEvent.SUCCESS) {
            //						InstanceID instanceID = InstanceID.getInstance(getContext());
            //							final String token = instanceID.getToken(gcmSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            HL.i("GCM getToken = {}", token);
            final String userId = Module.of(UserCenter.class).getUserId();

            String lastToken = Module.of(SPCache.class).load("last_gcm_token");
            String lastUserId = Module.of(SPCache.class).load("last_user_id");
            if (lastToken != null && lastToken.equals(token) && lastUserId != null && lastUserId.equals(userId)) {
              // ignore
            } else {
              HClient.of(UserApi.class).bindToken(token, userId, new TGResultHandler() {
                @Override
                protected void onSuccess(JSONObject json) throws JSONException {
                  HL.i("Bind gcm token success.");
                  Module.of(SPCache.class).save("last_gcm_token", token);
                  Module.of(SPCache.class).save("last_user_id", userId);
                }

                @Override
                protected void onFailed(int errorCode, String msg) {
                  HL.i("Bind gcm token failed.");
                }
              });
            }

          }

        }
      }.register();
    }

    loadGoogleResource();
  }

  protected void loadGoogleResource() {
    long c = System.currentTimeMillis();
    Resources res = getContext().getResources();
    String packName = getContext().getPackageName();
    Class<?> internalRClass;
    try {
      internalRClass = Class.forName("com.google.android.gms.R");
    } catch (ClassNotFoundException e) {
      HL.w("Cannot find internal R class , skip loadResources.");
      return;
    }


    Class<?>[] clazzArr = internalRClass.getDeclaredClasses();
    for (Class<?> clazz : clazzArr) {
      String type = clazz.getSimpleName();
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (!"styleable".equals(type)) {
          int value = res.getIdentifier(field.getName(), type, packName);
          try {
            field.set(null, value);
          } catch (IllegalAccessException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalAccessException : ");
            HL.w(e);
          } catch (IllegalArgumentException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalArgumentException : ");
            HL.w(e);
          }
        } else {
          if (Modifier.isFinal(field.getModifiers())) {
            continue;
          }
          try {
            String rClassName = getContext().getPackageName() + ".R$" + type;
            Class<?> rClass = Class.forName(rClassName);
            Field f = rClass.getField(field.getName());
            Object value = f.get(null);
            field.set(null, value);
          } catch (IllegalAccessException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalAccessException : ");
            HL.w(e);
          } catch (IllegalArgumentException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalArgumentException : ");
            HL.w(e);
          } catch (ClassNotFoundException e) {
            HL.w("init res '" + field.getName() + "' throws ClassNotFoundException : ");
            HL.w(e);
          } catch (NoSuchFieldException e) {
            HL.w("init res '" + field.getName() + "' throws NoSuchFieldException : ");
            HL.w(e);
          }
        }
      }
    }
    c = System.currentTimeMillis() - c;
    HL.i("loadGoogleResource cost : " + c + "ms");
  }

  @Override
  protected void onRelease() {
  }

  @Override
  public int getIconResource() {
    return getContext().getResources().getIdentifier("tg_google_icon", "drawable", getContext().getPackageName());
  }

  @Override
  public int getLoginIcon() {
    return getContext().getResources().getIdentifier("tg_google_login_icon", "drawable", getContext().getPackageName());
  }

  @Override
  public String getPlatformName() {
    return "Google";
  }


  /**
   * 唤起登录
   */
  @Override
  public void requestLogin() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
    Module.of(ActivityHolder.class).getTopActivity().startActivityForResult(signInIntent, REQUEST_CODE_GP_LOGIN);
  }

  @Override
  public void requestLogout() {}

  @Override
  public void requestShare(Parameter parameter) {}

  @Override
  public void requestPhotoShare(Parameter parameter) {}

  @Override
  public void requestInviteFriend(Parameter parameter) {}

  @Override
  public void requestFollow(Parameter parameter) {}

}
