package ru.threeguns.engine.tracker;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.manager.TrackManager;

public class TrackManagerImpl extends TrackManager {
	private List<Tracker> trackerList;

	@Override
	protected void onLoad(Parameter parameter) {
		trackerList = new ArrayList<Tracker>();

		try {
			JSONArray config = Module.of(TGController.class).configJson.getJSONArray("trackers");
			for (int i = config.length() - 1; i >= 0; i--) {
				JSONObject trackerConfig = config.getJSONObject(i);
				Parameter p = Parameter.parseFromJSON(trackerConfig);
				String enabled = p.optString("enabled");
				String name = p.optString("name");
				if ("true".equals(enabled)) {
					try {
						Class<?> clazz = Class.forName("ru.threeguns.engine.tracker." + name);
						Tracker tracker = (Tracker) clazz.getDeclaredConstructor().newInstance();
						trackerList.add(tracker);
						load(tracker, p);
					} catch (Exception e) {
						HL.w(e);
						HL.w("Cannot init this Tracker : " + p.optString("name"));
					}
				}
			}
		} catch (JSONException e) {
			HL.w(e);
		}
	}

	@Override
	protected void onRelease() {
	}

	@Override
	public void trackEvent(TrackEvent event) {
		for (ITracker tracker : trackerList) {
			tracker.trackEvent(event);
		}
	}

}
