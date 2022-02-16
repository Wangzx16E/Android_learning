package ru.threeguns.engine.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import kh.hyper.utils.HL.Logger;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UIHelper;

public class DebugManager extends Module {
	private View debugView;
	private ListView logListView;
	private List<String> logList;
	private LogAdapter logAdapter;

	@Override
	protected void onLoad(Parameter parameter) {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int tg_debug_layout_id = getContext().getResources().getIdentifier("tg_debug_layout", "layout", getContext().getPackageName());
		debugView = View.inflate(getContext(), tg_debug_layout_id, null);

        int tt_debug_log_btn_id = getContext().getResources().getIdentifier("tt_debug_log_btn", "id", getContext().getPackageName());
		debugView.findViewById(tt_debug_log_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (logListView.getVisibility() == View.VISIBLE) {
					logListView.setVisibility(View.GONE);
				} else {
					logListView.setVisibility(View.VISIBLE);
				}
			}
		});

        int tt_debug_version_id = getContext().getResources().getIdentifier("tt_debug_version", "id", getContext().getPackageName());
		TextView versionText = (TextView) debugView.findViewById(tt_debug_version_id);
		versionText.setText("Version:" + TGController.SDK_VERSION);

		HL.addLogger(DebugLogger.getInstance());

        int tt_debug_loglist_id = getContext().getResources().getIdentifier("tt_debug_loglist", "id", getContext().getPackageName());
		logListView = (ListView) debugView.findViewById(tt_debug_loglist_id);
		logListView.setVisibility(View.GONE);
		logList = DebugLogger.getInstance().getLogList();
		logAdapter = new LogAdapter();
		logListView.setAdapter(logAdapter);
		DebugLogger.getInstance().setLogListener(new DebugLogger.LogListener() {
			@Override
			public void onLog(String level, String tag, String message) {
				Module.of(UIHelper.class).post2MainThread(new Runnable() {
					@Override
					public void run() {
						logAdapter.notifyDataSetChanged();
						logListView.setSelection(logAdapter.getCount() - 1);
					}
				});
			}
		});

		WindowManager.LayoutParams p = new WindowManager.LayoutParams();
		p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		p.format = PixelFormat.TRANSLUCENT;
		p.width = WindowManager.LayoutParams.MATCH_PARENT;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		p.gravity = Gravity.BOTTOM;
		wm.addView(debugView, p);
	}

	private class LogAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return logList.size();
		}

		@Override
		public Object getItem(int position) {
			return logList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String message = logList.get(position);
			if (convertView == null) {
		        int tg_item_loglist_id = getContext().getResources().getIdentifier("tg_item_loglist", "layout", getContext().getPackageName());
				convertView = View.inflate(debugView.getContext(),tg_item_loglist_id, null);
			}
	        int tt_debug_log_id = getContext().getResources().getIdentifier("tt_debug_log", "id", getContext().getPackageName());
			((TextView) convertView.findViewById(tt_debug_log_id)).setText(message);
			return convertView;
		}

	}

	@Override
	protected void onRelease() {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.removeView(debugView);
	}

	public static class DebugLogger implements Logger {
		private static final int MAX_SIZE = 500;
		private static DebugLogger instance;
		private List<String> logList;
		private LogListener logListener;

		private DebugLogger() {
			logList = new ArrayList<String>();
		}

		public static DebugLogger getInstance() {
			if (instance == null) {
				instance = new DebugLogger();
			}
			return instance;
		}

		public void setLogListener(LogListener logListener) {
			this.logListener = logListener;
		}

		public List<String> getLogList() {
			return logList;
		}

		public static interface LogListener {
			void onLog(String level, String tag, String message);
		}

		@Override
		public void log(int logLevel, String tag, String message) {
			synchronized (logList) {
				logList.add("L" + logLevel + " - " + message);
				if (logList.size() > MAX_SIZE) {
					logList.remove(0);
				}
			}
			if (logListener != null) {
				logListener.onLog("L" + logLevel, tag, message);
			}
		}

	}

}
