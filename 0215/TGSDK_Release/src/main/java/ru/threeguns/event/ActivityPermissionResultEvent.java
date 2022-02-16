package ru.threeguns.event;

import kh.hyper.utils.NotProguard;

public class ActivityPermissionResultEvent implements NotProguard {
	private int requestCode;
	private String[] permissions;
	private int[] grantResults;

	public ActivityPermissionResultEvent(int requestCode, String[] permissions, int[] grantResults) {
		this.requestCode = requestCode;
		this.permissions = permissions;
		this.grantResults = grantResults;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public int[] getGrantResults() {
		return grantResults;
	}

}
