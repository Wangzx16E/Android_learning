package ru.threeguns.engine.controller;

import org.json.JSONException;

import android.text.TextUtils;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import kh.hyper.utils.HL;
import ru.threeguns.engine.TGPlatform;
import ru.threeguns.engine.manager.AbstractUserManager;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.entity.User;
import ru.threeguns.event.SendGiftEvent;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.UserLogoutEvent;
import ru.threeguns.event.UserRegisterEvent;
import ru.threeguns.event.UserUpgradeEvent;

public class UserCenter extends Module {
	@SuppressWarnings("unused")
	private static final String TAG = UserCenter.class.getSimpleName();

	private InternalUser activeUser;

	public InternalUser getActiveUser() {
		return activeUser;
	}

	public String getPassword() {
		return activeUser == null ? null : activeUser.getPassword();
	}

	public String getUserId() {
		return activeUser == null ? "-1" : activeUser.getUserId();
	}

	public String getNickname() {
		return activeUser == null ? null : activeUser.getNickname();
	}

	public String getUserType() {
		return activeUser == null ? null : activeUser.getUserType();
	}

	public String getDisplayNickname() {
		String nickname = getNickname();
		return TextUtils.isEmpty(nickname) ? activeUser == null ? "" : activeUser.getUsername() : nickname;
	}

	public String getToken() {
		return activeUser == null ? null : activeUser.getToken();
	}

	public void notifyLogout() {
		if (activeUser != null) {
			User tempUser = activeUser;
			activeUser = null;
			EventManager.instance.dispatch(new UserLogoutEvent(UserLogoutEvent.SUCCESS, tempUser));
		} else {
			EventManager.instance.dispatch(new UserLogoutEvent(UserLogoutEvent.NOT_LOGIN, null));
		}
		Module.of(UserInfoCache.class).setActiveUserId(null);
	}
	
	public void notifySendGift(int result) {
		if (activeUser != null) {
			EventManager.instance.dispatch(new SendGiftEvent(result, activeUser));
		} else {
			EventManager.instance.dispatch(new SendGiftEvent(result, activeUser));
		}
	}

	public void notifyLogoutInternel() {
		Module.of(UserInfoCache.class).setActiveUserId(null);
		if (activeUser != null) {
			User tempUser = activeUser;
			activeUser = null;
			EventManager.instance.dispatch(new UserLogoutEvent(UserLogoutEvent.SUCCESS, tempUser));
			AbstractUserManager manager = (AbstractUserManager) TGPlatform.getInstance().getUserManager();
			if (manager.getUserLogoutListener() != null) {
				manager.getUserLogoutListener().onUserLogoutInternel(tempUser);
			} else {
				Module.of(UIHelper.class).showToast("Cannot find UserLogoutListener , check the develop document.");
			}
		} else {
			EventManager.instance.dispatch(new UserLogoutEvent(UserLogoutEvent.NOT_LOGIN, null));
		}
	}

	public void notifyRegisterSuccess(String username, String password, String userType) {
		InternalUser user = new InternalUser();
		user.setUsername(username).setUserType(userType);
		EventManager.instance.dispatch(new UserRegisterEvent(user));
	}

	public void notifySetNickname(String nickname) {
		activeUser.setNickname(nickname);
	}

	public void notifyUserInfo(InternalUser user,boolean tgregister) throws JSONException {
		if (TextUtils.isEmpty(user.getPassword())) {
			InternalUser oriUser = Module.of(UserInfoCache.class).getUserById(user.getUserId());
			if (oriUser != null) {
				user.setPassword(oriUser.getPassword());
			}
		}
		Module.of(UserInfoCache.class).updateUser(user);
		Module.of(UserInfoCache.class).setActiveUserId(user.getUserId());

		if (activeUser == null) {
			activeUser = user;

			EventManager.instance.dispatch(new UserLoginEvent(UserLoginEvent.SUCCESS, user,tgregister));
		} else {
			User tempUser = activeUser;
			activeUser = user;
			EventManager.instance.dispatch(new UserLogoutEvent(UserLogoutEvent.SUCCESS, tempUser));
			EventManager.instance.dispatch(new UserLoginEvent(UserLoginEvent.SUCCESS, user,tgregister));
		}
	}

	public void notifyGuestUpgradeSuccess(String username, String password) {
		if (activeUser != null) {
			activeUser.setUserType(User.USERTYPE_NORMAL);
			activeUser.setUsername(username);
			activeUser.setPassword(password);
			Module.of(UserInfoCache.class).updateUser(activeUser);

			AbstractUserManager manager = (AbstractUserManager) TGPlatform.getInstance().getUserManager();
			if (manager.getUserBindListener() != null) {
				manager.getUserBindListener().onUserBindSuccess(activeUser);
			} else {
				HL.w("Cannot find UserBindListener , check the develop document.");
				// Module.of(UIHelper.class).showToast("Cannot find
				// UserBindListener , check the develop document.");
			}
		}
		EventManager.instance.dispatch(new UserUpgradeEvent(UserUpgradeEvent.SUCCESS, Module.of(UserInfoCache.class).getActiveUser()));
	}

	public void notifyChangePassword(String newPassword) {
		if (activeUser != null) {
			activeUser.setPassword(newPassword);
			Module.of(UserInfoCache.class).updateUser(activeUser);
		}
	}

	@Override
	protected void onLoad(Parameter parameter) {}

	@Override
	protected void onRelease() {}
}
