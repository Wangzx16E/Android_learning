package ru.threeguns.event;

import kh.hyper.utils.NotProguard;

/**
 * 用户支付事件
 */
public final class PaymentEvent implements NotProguard {
	public static final int SUCCESS = 0;
	public static final int USER_CANCEL = 1;
	public static final int FAILED = 2;
	public static final int IN_PROCESS = 3;
	private int result;
	private String orderId;

	public PaymentEvent(int result, String orderId) {
		this.result = result;
		this.orderId = orderId;
	}

	public int getResult() {
		return result;
	}

	public String getOrderId() {
		return orderId;
	}

}
