package ru.threeguns.manager;

import java.util.HashMap;
import java.util.Map;

import kh.hyper.core.IM;
import kh.hyper.core.Module;
import ru.threeguns.engine.tracker.ITracker;
import ru.threeguns.engine.tracker.TrackManagerImpl;

@IM(TrackManagerImpl.class)
public abstract class TrackManager extends Module implements ITracker {
	public static final String LOGIN = "login";
	public static final String LOGIN_TP = "tplogin";
	public static final String REGISTER = "register";
	public static final String FAST_REGISTER = "fast_register";
	public static final String OPEN = "open";
	public static final String CREATE_ORDER = "createOrder";
	public static final String FINISH_ORDER = "completedOrder";
	public static final String VERIFIED_ORDER = "verifiedOrder";
	public static final String ANDROID_REFERRER = "androidReferrer";
	
	public final static class TrackEvent {
		private String eventName;
		private String eventTag;
		private double revenue;
		private Map<String, String> extraParams;

		public TrackEvent() {
			extraParams = new HashMap<String, String>();
		}

		public TrackEvent(String eventName) {
			this();
			this.eventName = eventName;
		}

		public TrackEvent setEventName(String eventName) {
			this.eventName = eventName;
			return this;
		}

		public TrackEvent setEventTag(String eventTag) {
			this.eventTag = eventTag;
			return this;
		}

		public TrackEvent setRevenue(double revenue) {
			this.revenue = revenue;
			return this;
		}

		public TrackEvent setExtraParams(String key, String value) {
			extraParams.put(key, value);
			return this;
		}

		public String getEventName() {
			return eventName;
		}

		public String getEventTag() {
			return eventTag;
		}

		public double getRevenue() {
			return revenue;
		}

		public String getExtraParam(String key) {
			return extraParams.get(key);
		}

		public Map<String, String> getExtraParams() {
			return extraParams;
		}

	}
}
