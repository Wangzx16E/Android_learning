package ru.threeguns.event;

import kh.hyper.utils.NotProguard;

public final class FollowEvent implements NotProguard {
	public static final int SUCCESS = 0;
	public static final int FAILED = 1;
	public static final int USER_CANCEL = 2;
	private int result;
	private String data;

	public FollowEvent(int result, String data) {
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
