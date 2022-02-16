package ru.threeguns.engine.manager;

import android.app.Activity;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.network.HClient;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.entity.User;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.handler.SendGiftHandler;
import ru.threeguns.event.handler.UserLoginHandler;
import ru.threeguns.event.handler.UserLogoutHandler;
import ru.threeguns.manager.UserManager;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;

public abstract class AbstractUserManager extends Module implements UserManager {
  protected OnUserLogoutListener userLogoutListener;
  protected OnUserBindListener userBindListener;

  @Override
  public User getActiveUser() {
    return Module.of(UserCenter.class).getActiveUser();
  }

  protected void requestAutoLogin(final Activity activity) {
    //		User activeUser = Module.of(UserInfoCache.class).getActiveUser();
    //		if (activeUser != null) {
    //
    //			HClient.of(UserApi.class).tokenLogin(activeUser.getUserId(), activeUser.getToken(), new TGResultHandler() {
    //
    //				@Override
    //				protected void onSuccess(JSONObject json) throws JSONException {
    //					Module.of(TGController.class).isAutoLogin = true;
    //					Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json),false);
    //
    //					if (Module.of(UserCenter.class).getActiveUser() != null && User.USERTYPE_GUEST.equals(Module.of(UserCenter.class).getActiveUser().getUserType())) {
    //						activity.startActivity(new Intent(activity, GuestActivity.class));
    //						return;
    //					}
    //				}
    //
    //				@Override
    //				protected void onFailed(int errorCode, String msg) {
    //					Module.of(TGController.class).isAutoLogin = false;
    //					doLogin();
    //				}
    //			});
    //
    //		} else {
    //			Module.of(TGController.class).isAutoLogin = false;
    //			doLogin();
    //		}

    Module.of(TGController.class).isAutoLogin = false;
    doLogin();
  }

  @Override
  public void requestLogin(Activity activity, UserLoginHandler handler) {
    if (handler!=null) {
      handler.register();
    }
    requestAutoLogin(activity);
  }

  @Override
  public void TgAction(String strings, TGResultHandler handler) {

    HClient.of(UserApi.class).Action(strings, handler);
  }


  @Override
  public void requestLogout(UserLogoutHandler handler) {
    if (handler!=null) {
      handler.register();
    }
    Module.of(TGController.class).isAutoLogin = false;
    doLogout();
  }

  @Override
  public void requestSendGift(SendGiftHandler handler) {
    if (handler!=null) {
      handler.register();
    }
    doSend();
  }

  protected void notifyLoginCancel() {
    EventManager.instance.dispatch(new UserLoginEvent(UserLoginEvent.USER_CANCEL, null, false));
  }

  protected void notifyLoginFailed() {
    EventManager.instance.dispatch(new UserLoginEvent(UserLoginEvent.FAILED, null, false));

  }

  @Override
  public void setOnUserLogoutListener(OnUserLogoutListener listener) {
    this.userLogoutListener = listener;
  }

  @Override
  public void setOnUserBindListener(OnUserBindListener listener) {
    this.userBindListener = listener;
  }

  public OnUserLogoutListener getUserLogoutListener() {
    return userLogoutListener;
  }

  public OnUserBindListener getUserBindListener() {
    return userBindListener;
  }

  protected abstract void doLogin();

  protected abstract void doLogout();

  protected abstract void requestLoginInterface();

  protected abstract void doSend();

}
