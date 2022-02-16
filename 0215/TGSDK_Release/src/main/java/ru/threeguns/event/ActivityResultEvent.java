package ru.threeguns.event;

import android.content.Intent;
import kh.hyper.utils.NotProguard;

public class ActivityResultEvent implements NotProguard {
	private int requestCode;
	private int resultCode;
	private Intent data;

	public ActivityResultEvent(int requestCode, int responseCode, Intent data) {
		this.requestCode = requestCode;
		this.resultCode = responseCode;
		this.data = data;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public int getResultCode() {
		return resultCode;
	}

	public Intent getData() {
		return data;
	}

}
