package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.User;

/**
 * 发送礼物时调用该事件
 */
public final class SendGiftEvent implements NotProguard {
	public static final int SEND = 0;
	public static final int NOT_NORMAL = 1;
	private int result;
	private User user;

	public SendGiftEvent(int result, User user) {
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
