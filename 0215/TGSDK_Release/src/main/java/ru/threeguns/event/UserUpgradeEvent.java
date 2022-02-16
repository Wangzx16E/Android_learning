package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.User;

/**
 * 用户升级成功时调用该事件
 */
public final class UserUpgradeEvent implements NotProguard {
	public static final int SUCCESS = 0;
	public static final int USER_CANCEL = 1;
	public static final int FAILED = 2;
	private int result;
	private User user;

	public UserUpgradeEvent(int result, User user) {
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
