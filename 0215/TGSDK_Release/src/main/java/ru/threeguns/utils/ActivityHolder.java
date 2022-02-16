package ru.threeguns.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import ru.threeguns.engine.controller.TGApplication;

public final class ActivityHolder {
	private WeakReference<Activity> topActivity;
	public Activity getTopActivity() {
		return topActivity == null ? null : topActivity.get();
	}

	public void setTopActivity(Activity act) {
		topActivity = new WeakReference<Activity>(act);
	}
}
