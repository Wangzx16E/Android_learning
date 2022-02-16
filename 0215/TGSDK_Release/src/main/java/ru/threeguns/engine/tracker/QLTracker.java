package ru.threeguns.engine.tracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.SystemInfo;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.manager.TrackManager.TrackEvent;
import ru.threeguns.network.NetworkCode;
import ru.threeguns.network.NetworkUtil;
import ru.threeguns.utils.TimeUnit;

public class QLTracker extends Tracker {
	private static final String EVENT_BASE_URL = "/statistics/event/";
	private static final String ANDROID_REFERRER_URL = "/statistics/android_referrer/";

	private EventCache eventCache;
	private SendEventThread sendEventThread;
	private OkHttpClient httpClient;
	private Object sendEventLock = new Object();

	@Override
	protected void onLoad(Parameter parameter) {
		eventCache = new EventCache(getContext());
		httpClient = new OkHttpClient();
		sendEventThread = new SendEventThread();
		sendEventThread.start();
	}

	@Override
	protected void onRelease() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendEventThread.requestStop();
			}
		}).start();
	}

	@Override
	public void trackEvent(TrackEvent event) {
		if (TrackManager.OPEN.equals(event.getEventName())) {
			String url = generateEventUrl("open");
			String dataString = NetworkUtil.makeDataString(generateBaseParamMap());

			eventCache.offerEvent(new Event(url, dataString));
			notifySendEvent();
		} else if (TrackManager.ANDROID_REFERRER.equals(event.getEventName())) {
			Map<String, String> map = generateBaseParamMap();
			map.put("referrer", event.getExtraParam("referrer"));
			String url = (Module.of(TGController.class).gameDebug ? Constants.DEBUG_STATISTIC_HOST_ADDRESS : Constants.STATISTIC_HOST_ADDRESS)
					+ ANDROID_REFERRER_URL;
			String dataString = NetworkUtil.makeDataString(map);

			eventCache.offerEvent(new Event(url, dataString));
			notifySendEvent();
		}
	}

	private Map<String, String> generateBaseParamMap() {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appid", Module.of(TGController.class).appId);
		paramMap.put("channel_id", Module.of(TGController.class).channelId);
		paramMap.put("deviceNo", Module.of(SystemInfo.class).deviceNo);
		paramMap.put("deviceType", Module.of(SystemInfo.class).deviceType);
		paramMap.put("platform", Constants.PLATFORM_NAME);
		paramMap.put("sdk_version", TGController.SDK_VERSION);
		paramMap.put("package_name", Module.of(TGController.class).packageName);
		paramMap.put("imei", Module.of(SystemInfo.class).imei);
		return paramMap;
	}

	private String generateEventUrl(String eventName) {
		StringBuilder url = new StringBuilder(
				Module.of(TGController.class).gameDebug ? Constants.DEBUG_STATISTIC_HOST_ADDRESS : Constants.STATISTIC_HOST_ADDRESS);
		url.append(EVENT_BASE_URL);
		url.append(eventName);
		url.append("/");
		return url.toString();
	}

	private void notifySendEvent() {
		synchronized (sendEventLock) {
			sendEventLock.notify();
		}
	}

	private static class EventCache extends SQLiteOpenHelper {
		private static final int VERSION = 1;
		private static final String DB_NAME = "__QL_Statistic__.db";

		private SQLiteDatabase dataBase;

		public EventCache(Context context) {
			super(context, DB_NAME, null, VERSION);
			init(context);
		}

		// id:唯一标示
		// request_url:请求地址
		// data_string:加密后的Data字符串
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS events" + //
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
					" request_url TEXT NOT NULL," + //
					" data_string TEXT NOT NULL)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			HL.w("DataBase onUpgrade : from  " + oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE events");
			db.execSQL("CREATE TABLE IF NOT EXISTS events" + //
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
					" request_url TEXT NOT NULL," + //
					" data_string TEXT NOT NULL)");
		}

		public void init(Context context) {
			dataBase = getWritableDatabase();
		}

		public void removeEvent(Event event) {
			try {
				dataBase.delete("events", "id=?", new String[] { event.getId() + "" });
			} catch (Exception e) {
				HL.w("Remove Event Failed.");
				HL.w(e);
			}
		}

		public Event pollEvent() {
			try {
				Cursor c = dataBase.rawQuery(//
						"SELECT id,request_url,data_string FROM events WHERE id=(SELECT MIN(id) FROM events)", //
						null);
				if (c.moveToNext()) {
					int id = c.getInt(c.getColumnIndex("id"));
					String requestURL = c.getString(c.getColumnIndex("request_url"));
					String dataString = c.getString(c.getColumnIndex("data_string"));
					return new Event(id, requestURL, dataString);
				}
			} catch (Exception e) {
				HL.w("Poll Event Failed.");
				HL.w(e);
			}
			return null;
		}

		public void offerEvent(Event event) {
			try {
				dataBase.execSQL("INSERT INTO events VALUES(null, ?, ?)", new Object[] { event.getRequestURL(), event.getDataString() });
			} catch (Exception e) {
				HL.w("Offer Event Failed.");
				HL.w(e);
			}
		}

		public void release() {
			dataBase.close();
		}

	}

	private static class Event {
		private int id;
		private String requestURL;
		private String dataString;

		public Event(String requestURL, String dataString) {
			this.requestURL = requestURL;
			this.dataString = dataString;
		}

		public Event(int id, String requestURL, String dataString) {
			this.id = id;
			this.requestURL = requestURL;
			this.dataString = dataString;
		}

		public int getId() {
			return id;
		}

		public String getRequestURL() {
			return requestURL;
		}

		public String getDataString() {
			return dataString;
		}

	}

	private class SendEventThread extends Thread {
		// 网络请求失败的重试时间
		private static final long DEFAULT_WAIT_TIME = 15 * TimeUnit.MINUTE;
		// 网络请求超时的重试时间
		private static final long DEFAULT_RETRY_INTERVAL = 3 * TimeUnit.MINUTE;
		private long waitTime = DEFAULT_WAIT_TIME;
		private volatile boolean loop = true;

		@Override
		public void run() {
			while (loop) {
				try {
					final Event event = eventCache.pollEvent();

					if (event != null) {

						final Request request = new Request.Builder()//
								.url(event.getRequestURL())//
								.post(RequestBody.create(null, event.getDataString()))//
								.build();

						HL.i("========Statistic : {} ========", event.getRequestURL());

						Call call = httpClient.newCall(request);
						Response response = null;
						try {
							response = call.execute();
							int httpCode = response.code();
							String data = response.body().string();
							HL.i("===Response code : {}", httpCode);
							HL.i("===Response body : {}", data);
							if (httpCode == 200) {
								JSONObject json = new JSONObject(data);
								int code = json.optInt("code");
								if (code == NetworkCode.NO_ERROR) {
									eventCache.removeEvent(event);
									waitTime = 0;
								} else if (code == NetworkCode.NETWORK_TIMED_OUT) {
									waitTime = DEFAULT_RETRY_INTERVAL;
								} else {
									if (code > 0) {
										eventCache.removeEvent(event);
										waitTime = 0;
										HL.w("Send Pingback get response : " + code + " from server , remove it from DB.");
									} else {
										waitTime = DEFAULT_WAIT_TIME;
									}
								}
							} else {
								waitTime = DEFAULT_RETRY_INTERVAL;
							}
						} catch (IOException e) {
							waitTime = DEFAULT_RETRY_INTERVAL;
						} catch (JSONException e) {
							waitTime = DEFAULT_RETRY_INTERVAL;
						}

					} else {
						// 没有事件,一直等待直至抛出事件并notify
						waitTime = -1;
					}
				} finally {
					synchronized (sendEventLock) {
						try {
							if (waitTime > 0) {
								sendEventLock.wait(waitTime);
							} else if (waitTime < 0) {
								sendEventLock.wait();
							} else {
								// continue loop
							}
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}
			}
		}

		protected void requestStop() {
			loop = false;
			SendEventThread.this.interrupt();
			try {
				join(5000);
			} catch (InterruptedException e) {
				HL.w(e);
			} finally {
				eventCache.release();
			}
		}
	}

}
