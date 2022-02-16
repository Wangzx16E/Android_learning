package ru.threeguns.event;

import kh.hyper.utils.NotProguard;

public final class InviteFriendEvent implements NotProguard {
	public static final int SUCCESS = 0;
	public static final int USER_CANCEL = 1;
	public static final int FAILED = 2;
	private int result;
	private String data;

	public InviteFriendEvent(int result, String data) {
		this.result = result;
		this.data = data;
	}

	public int getResult() {
		return result;
	}

	public String getData() {
		return data;
	}
}
