package ru.threeguns.engine.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.network.HClient;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.entity.Notice;
import ru.threeguns.entity.User;
import ru.threeguns.event.NewNoticeEvent;
import ru.threeguns.event.ReadNoticeEvent;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.NoticeApi;
import ru.threeguns.ui.dialog.NoticeDialog;

//@Load
public final class NoticeManager extends Module {

	private static final String KT_NEWNOTICE_FLAG = "KT_NEWNOTICE_FLAG";
	private static final String KT_LAST_USER_ID = "KT_LAST_USER_ID";
	public static final String KT_NOTIFY_NOTICE_ID = "KT_NOTIFY_NOTICE_ID";

	private static final int MAX_NOTICE_SIZE = 10;
	private static final long DEFAULT_UPDATE_INTERVAL = 60 * 1000;
	private static final int MSG_FETCH_NOTICE = 777;

	private long updateInterval;

	private List<Notice> noticeList;
	private NoticeCache noticeCache;
	private NoticeHandler handler;
	private boolean running = false;
	private boolean firstFetchNotice = true;

	public boolean haveNewNotice() {
		return Boolean.parseBoolean(Module.of(SPCache.class).load(KT_NEWNOTICE_FLAG));
	}

	public void notifyReadNotice() {
		Module.of(SPCache.class).save(KT_NEWNOTICE_FLAG, "false");
		EventManager.instance.dispatch(new ReadNoticeEvent());
	}

	public List<Notice> getAllNotices() {
		return noticeList;
	}

	public List<Notice> queryAllNotices() {
		return noticeCache.queryAllNotices(Module.of(UserCenter.class).getUserId());
	}

	@Override
	protected void onLoad(Parameter parameter) {
		if (!Constants.NOTICE_ENABLED) {
			return;
		}
		updateInterval = DEFAULT_UPDATE_INTERVAL;
		handler = new NoticeHandler(this);
		noticeCache = new NoticeCache(getContext());
		// noticeList = noticeCache.queryAllNotices();
		noticeList = new ArrayList<Notice>();

		new TGHandler() {
			@Handle
			private void onUserLogin(UserLoginEvent e) {
				if (e.getResult() == UserLoginEvent.SUCCESS) {
					User u = e.getUser();
					String lastUserId = Module.of(SPCache.class).load(KT_LAST_USER_ID);
					if (!TextUtils.isEmpty(lastUserId) && !lastUserId.equals(u.getUserId())) {
						Module.of(SPCache.class).save(KT_NEWNOTICE_FLAG, "false");
						noticeList = noticeCache.queryAllNotices(u.getUserId());
					}
					Module.of(SPCache.class).save(KT_LAST_USER_ID, u.getUserId());

					firstFetchNotice = true;
					if (running) {
						handler.removeMessages(MSG_FETCH_NOTICE);
					} else {
						running = true;
						noticeList = noticeCache.queryAllNotices(u.getUserId());
					}
					fetchNotice();
					// if (!running) {
					// noticeList = noticeCache.queryAllNotices(u.getUserId());
					// running = true;
					// fetchNotice();
					// }
				}
			}
		}.register();
	}

	protected void fetchNotice() {
		User u = Module.of(UserCenter.class).getActiveUser();
		if (u != null) {
			int lastestNoticeId = noticeCache.queryLastestNoticeId(u.getUserId());
			HClient.of(NoticeApi.class).get(lastestNoticeId, new TGResultHandler() {

				@Override
				protected void onSuccess(JSONObject json) throws JSONException {
					String userId = Module.of(UserCenter.class).getUserId();

					EventManager.instance.dispatch(new ReadNoticeEvent());

					updateInterval = json.optLong("interval", DEFAULT_UPDATE_INTERVAL);
					JSONArray noticeArr = json.getJSONArray("notices");
					for (int i = 0, sz = noticeArr.length(); i < sz; i++) {
						JSONObject s = noticeArr.getJSONObject(i);
						Notice notice = Notice.fromJSON(s);
						noticeList.add(0, notice);
						noticeCache.pushNotice(userId, notice);
					}
					while (noticeList.size() > MAX_NOTICE_SIZE) {
						noticeList.remove(noticeList.size() - 1);
					}
					Collections.sort(noticeList, new Comparator<Notice>() {
						@Override
						public int compare(Notice lhs, Notice rhs) {
							return lhs.getId() < rhs.getId() ? 1 : -1;
						}
					});
					handler.sendEmptyMessageDelayed(MSG_FETCH_NOTICE, updateInterval);
					if (noticeArr.length() > 0) {
						Module.of(SPCache.class).save(KT_NEWNOTICE_FLAG, "true");
						EventManager.instance.dispatch(new NewNoticeEvent(noticeList.get(0)));
					}

					if (Constants.NOTICE_DIALOG_ENABLED && firstFetchNotice) {
						firstFetchNotice = false;
						// if (noticeArr.length() > 0) {
						// Module.of(SPCache.class).save(KT_NOTIFY_NOTICE_ID,
						// String.valueOf(noticeList.get(0).getId()));
						// }
						if (noticeList.size() > 0) {
							final Notice notice = noticeList.get(0);
							int readNoticeId = Module.of(SPCache.class).loadInt(KT_NOTIFY_NOTICE_ID + userId);
							if (readNoticeId != notice.getId()) {
								Module.of(UIHelper.class).post2MainThread(new Runnable() {
									public void run() {
										new NoticeDialog(context, notice).show();
									}
								});
							}
						}
					}
				}

				@Override
				protected void onFailed(int errorCode, String msg) {
					HL.e("Notice onFailed errorCode = " + errorCode + " msg = " + msg);
					handler.sendEmptyMessageDelayed(MSG_FETCH_NOTICE, updateInterval);
				}
			});
		} else {
			handler.sendEmptyMessageDelayed(MSG_FETCH_NOTICE, updateInterval);
		}
	}

	@Override
	protected void onRelease() {
		if (!Constants.NOTICE_ENABLED) {
			return;
		}
		handler.removeMessages(MSG_FETCH_NOTICE);
		noticeCache.release();
	}

	private static class NoticeHandler extends Handler {
		private WeakReference<NoticeManager> ref;

		private NoticeHandler(NoticeManager manager) {
			ref = new WeakReference<NoticeManager>(manager);
		}

		@Override
		public void handleMessage(Message msg) {
			if (ref.get() != null) {
				ref.get().fetchNotice();
			}
		}
	}
}
