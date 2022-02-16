package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.User;

/**
 * 用户登出时调用该事件
 */
public final class UserLogoutEvent  implements NotProguard   {
	public static final int SUCCESS = 0;
	public static final int NOT_LOGIN = 1;
	private int result;
	private User user;

	public UserLogoutEvent(int result, User user) {
		this.result = result;
		this.user = user;
	}

	public int getResult() {
		return result;
	}

	public User getUser() {
		return user;
	}

}
