package ru.threeguns.engine.tp;

import kh.hyper.core.Parameter;
import kh.hyper.utils.NotProguard;
import ru.threeguns.manager.ShareManager.ResultHandler;

public interface IThirdPlatform extends NotProguard {
	int getIconResource();

	int getLoginIcon();

	String getPlatformName();

	/**
	 * 唤起登录
	 */
	void requestLogin();

	void requestLogout();

	void requestShare(Parameter parameter);
	
	void requestPhotoShare(Parameter parameter);

	void requestInviteFriend(Parameter parameter);

	void requestFollow(Parameter parameter);

	void requestExtra(Parameter parameter, ResultHandler handler);

	boolean isLoginEnabled();

}