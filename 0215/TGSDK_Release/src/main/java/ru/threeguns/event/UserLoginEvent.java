package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.User;

/**
 * 用户登录成功时调用该事件
 */
public final class UserLoginEvent implements NotProguard   {
	public static final int SUCCESS = 0;
	public static final int USER_CANCEL = 1;
	public static final int FAILED = 2;
	public static final int REGISTER = 3;
	private int result;
	private boolean tgregister;
	private User user;

	public UserLoginEvent(int result, User user,boolean tgregister) {
		this.result = result;
		this.user = user;
		this.tgregister = tgregister;
	}

	public int getResult() {
		return result;
	}

	public User getUser() {
		return user;
	}

	public boolean getTgregister() {
		return tgregister;
	}
}
