package ru.threeguns.engine.tracker;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.manager.TrackManager.TrackEvent;

public class QLReferrerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			String referrer = intent.getStringExtra("referrer");
			if (referrer == null || referrer.length() == 0) {
				HL.i("Have No Referrer.");
			} else {
				HL.i("Read Referrer:" + referrer);
				referrer = URLDecoder.decode(referrer);
			}
			context = context.getApplicationContext() == null ? context : context.getApplicationContext();
			try {
				Module.of(TrackManager.class).trackEvent(new TrackEvent(TrackManager.ANDROID_REFERRER).setExtraParams("referrer", referrer));
			} catch (Exception e) {
				HL.w("Send Referrer throw Exceptions.Try save it.");
				HL.w(e);
				SharedPreferences sp = context.getSharedPreferences(Constants.SPN_REFERRER, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(Constants.SPK_REFERRER, referrer);
				editor.commit();
			}

		}

	}
}
