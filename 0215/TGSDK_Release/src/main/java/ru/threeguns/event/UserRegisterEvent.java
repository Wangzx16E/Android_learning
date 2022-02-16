package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.User;

/**
 * 用户注册成功时调用该事件
 */
public final class UserRegisterEvent implements NotProguard  {
	private User user;

	public UserRegisterEvent(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
