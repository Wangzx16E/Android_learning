package ru.threeguns.engine.tp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.network.HClient;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.event.ActivityResultEvent;
import ru.threeguns.event.FollowEvent;
import ru.threeguns.event.InviteFriendEvent;
import ru.threeguns.event.ShareEvent;
import ru.threeguns.event.UserLogoutEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.utils.ActivityHolder;

public class Facebook extends ThirdPlatform {
  private static final String TAG = "TPFacebook";
  private static final int STATE_NORMAL = 0;
  private static final int STATE_SHARING = 1;
  private static final int STATE_INVITING = 2;
  private static final int STATE_FLLOWING = 3;
  private static final int STATE_SHARPHOTOING = 5;

  private CallbackManager mCallbackManager;

  private String oriAppId;

  private Parameter parameterHolder;
  private AtomicInteger currentState;

  @Override
  protected void onLoad(Parameter parameter) {
    currentState = new AtomicInteger(STATE_NORMAL);
    final Parameter p = parameter;
    Module.of(UIHelper.class).post2MainThread(new Runnable() {
      @Override
      public void run() {
        initFB(p);
      }
    });

    new TGHandler() {
      @Handle
      private void onActivityResult(ActivityResultEvent event) {
        mCallbackManager.onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
      }

      @Handle
      private void onUserLogout(UserLogoutEvent event) {
        if (event.getResult() == UserLogoutEvent.SUCCESS) {
          LoginManager.getInstance().logOut();
        }
      }
    }.register();
  }

  protected void initFB(Parameter parameter) {
    oriAppId = parameter.optString("app_id");
    FacebookSdk.setApplicationId(oriAppId);
    FacebookSdk.sdkInitialize(getContext());

    mCallbackManager = CallbackManager.Factory.create();

    //.setLoginBehavior(LoginBehavior.DIALOG_ONLY)
    LoginManager.getInstance().setLoginBehavior(LoginBehavior.DIALOG_ONLY).registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onCancel() {
        HL.i("callbackManager onCancel");
        if (currentState.get() == STATE_SHARING) {
          EventManager.instance.dispatch(new ShareEvent(ShareEvent.USER_CANCEL, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_INVITING) {
          EventManager.instance.dispatch(new InviteFriendEvent(InviteFriendEvent.USER_CANCEL, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_FLLOWING) {
          EventManager.instance.dispatch(new FollowEvent(FollowEvent.USER_CANCEL, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_SHARPHOTOING) {
          EventManager.instance.dispatch(new ShareEvent(ShareEvent.USER_CANCEL, null));
          currentState.set(STATE_SHARPHOTOING);
        }
      }

      @Override
      public void onError(FacebookException exception) {
        exception.printStackTrace();
        HL.i("callbackManager onError : {}", exception.getLocalizedMessage());
        if (currentState.get() == STATE_SHARING) {
          EventManager.instance.dispatch(new ShareEvent(ShareEvent.FAILED, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_INVITING) {
          EventManager.instance.dispatch(new InviteFriendEvent(InviteFriendEvent.FAILED, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_FLLOWING) {
          EventManager.instance.dispatch(new FollowEvent(FollowEvent.FAILED, null));
          currentState.set(STATE_NORMAL);
        } else if (currentState.get() == STATE_SHARPHOTOING) {
          EventManager.instance.dispatch(new ShareEvent(ShareEvent.FAILED, null));
          currentState.set(STATE_SHARPHOTOING);
        } else {
          Module.of(UIHelper.class).showToast("Facebook Login Error.");
          Module.of(UIHelper.class).showToast(System.err.toString());
          Module.of(UIHelper.class).showToast(exception.getLocalizedMessage());
        }
      }

      @Override
      public void onSuccess(LoginResult loginResult) {
        HL.i("callbackManager onSuccess");
        if (currentState.get() == STATE_SHARING) {
          currentState.set(STATE_NORMAL);
          requestShare(parameterHolder);
        } else if (currentState.get() == STATE_INVITING) {
          currentState.set(STATE_NORMAL);
          requestInviteFriend(parameterHolder);
        } else if (currentState.get() == STATE_FLLOWING) {
          currentState.set(STATE_NORMAL);
          requestFollow(parameterHolder);
        } else if (currentState.get() == STATE_SHARPHOTOING) {
          currentState.set(STATE_SHARPHOTOING);
          requestPhotoShare(parameterHolder);
        } else {
          //登录成功
          loginSucceedHandler(loginResult.getAccessToken().getToken());
        }
      }
    });
  }

  private boolean hasInstallFacebook() {
    Context c = getContext();
    PackageManager packageManager = c.getPackageManager();
    try{
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0 );
      return applicationInfo.enabled;
    } catch( PackageManager.NameNotFoundException e ){
      return false;
    }
  }

  private void loginSucceedHandler(String token) {
    HClient.of(UserApi.class).tpLogin("facebook", token, null, new TGResultHandler() {
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
        Module.of(UIHelper.class).showToast("Facebook Login Failed.(" + msg + ")");
        Module.of(UIHelper.class).closeProgressDialog();
        HL.w(">>>>>>>>>>>>>>>>>Facebook Login Failed Code:" + errorCode + " msg:" + msg);
      }
    });
  }

  @Override
  public String __getExtraConfig(String key) {
    return "";//fbFansPage;
  }

  @Override
  protected void onRelease() {
    LoginManager.getInstance().unregisterCallback(mCallbackManager);
  }

  @Override
  public int getIconResource() {
    return getContext().getResources().getIdentifier("tg_facebook_icon", "drawable", getContext().getPackageName());
  }

  @Override
  public int getLoginIcon() {
    return getContext().getResources().getIdentifier("tg_fb_login_icon", "drawable", getContext().getPackageName());
  }

  @Override
  public String getPlatformName() {
    return "Facebook";
  }

  @Override
  public void requestLogin() {
    currentState.set(STATE_NORMAL);
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    if (accessToken == null) {
      if(!hasInstallFacebook()) {
        Module.of(UIHelper.class).showToast("未检测到Facebook APP,请首先安装或启用Facebook APP");
        return;
      }
      Activity activity = Module.of(ActivityHolder.class).getTopActivity();
      LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email","public_profile"));
    } else {
      loginSucceedHandler(accessToken.getToken());
    }
  }

  @Override
  public void requestLogout() {
    LoginManager.getInstance().logOut();
  }

  @Override
  public void requestShare(Parameter parameter) {}

  @Override
  public void requestPhotoShare(Parameter parameter) {}

  @Override
  public void requestInviteFriend(Parameter parameter) {}

  @Override
  public void requestFollow(Parameter parameter) {}
}
