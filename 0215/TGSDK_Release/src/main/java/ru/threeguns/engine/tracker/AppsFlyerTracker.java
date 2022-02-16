package ru.threeguns.engine.tracker;

import java.util.HashMap;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.manager.TrackManager.TrackEvent;
import ru.threeguns.utils.ActivityHolder;

public class AppsFlyerTracker extends Tracker {
	private String devKey;

	@Override
	protected void onLoad(Parameter parameter) {
		devKey = parameter.optString("dev_key");

		AppsFlyerLib.getInstance().setCollectIMEI(false);
		AppsFlyerLib.getInstance().setCollectAndroidID(false);

		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				AppsFlyerLib.getInstance().start(Module.of(ActivityHolder.class).getTopActivity().getApplication(), devKey);
			}
		});
	}

	@Override
	protected void onRelease() {
	}

	@Override
	public void trackEvent(TrackEvent event) {
		if (!TrackManager.OPEN.equals(event.getEventName())) {
			AppsFlyerLib.getInstance().logEvent(getContext(), event.getEventName(), null);
		}

		if (TrackManager.FINISH_ORDER.equals(event.getEventName())) {
			HashMap<String, Object> eventValue = new HashMap<String, Object>();
			eventValue.put(AFInAppEventParameterName.REVENUE, event.getExtraParam("amount"));
			eventValue.put(AFInAppEventParameterName.CURRENCY, event.getExtraParam("currency"));

			AppsFlyerLib.getInstance().logEvent(getContext(), AFInAppEventType.PURCHASE, eventValue);
		}
	}
}
