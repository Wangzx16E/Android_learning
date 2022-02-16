package ru.threeguns.manager;

import android.app.Activity;
import kh.hyper.core.IM;
import kh.hyper.utils.NotProguard;
import ru.threeguns.engine.manager.impl.UserManagerImpl;
import ru.threeguns.entity.User;
import ru.threeguns.event.handler.SendGiftHandler;
import ru.threeguns.event.handler.UserLoginHandler;
import ru.threeguns.event.handler.UserLogoutHandler;
import ru.threeguns.network.TGResultHandler;

/**
 * 控制用户的Manager
 */
@IM(UserManagerImpl.class)
public interface UserManager extends NotProguard {
	/**
	 * 获取当前登录的用户
	 * 
	 * @return 当前登录的用户
	 */
	User getActiveUser();

	/**
	 * 请求登录,调用该方法会弹出登录的Activity
	 */
	void requestLogin(Activity activity, UserLoginHandler handler);

	/**
	 * 上报角色
	 */
	void TgAction(String  strings, TGResultHandler handler);


	/**
	 * 请求登出
	 */
	void requestLogout(UserLogoutHandler handler);

	/**
	 * 请求发送礼物
	 */
	void requestSendGift(SendGiftHandler handler);

	/**
	 * 请求绑定正式账号
	 */
	void requestBindAccount();

	/**
	 * 设置用户登出回调
	 */
	void setOnUserLogoutListener(OnUserLogoutListener listener);

	/**
	 * 设置用户绑定回调
	 */
	void setOnUserBindListener(OnUserBindListener listener);

	/**
	 * 打开用户中心
	 */
	void requestUserCenter();

	/**
	 * 打开客服界面
	 */
	void requestFeedback();


	public static interface OnUserLogoutListener extends NotProguard {
		void onUserLogoutInternel(User user);
	}

	public static interface OnUserBindListener extends NotProguard {
		void onUserBindSuccess(User user);
	}

}
