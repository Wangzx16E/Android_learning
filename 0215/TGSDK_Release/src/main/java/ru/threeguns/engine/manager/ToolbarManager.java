package ru.threeguns.engine.manager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import kh.hyper.core.Load;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.Handle;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGApplication;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.event.ActivityStartEvent;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.UserLogoutEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.ui.toolbar.ToolbarView;

@Load
public class ToolbarManager extends Module {
	private static final int TOOLBAR_VIEW_ID = 0x1177;
	private List<ToolbarView> toolbarList;

	private boolean isToolbarShow = false;

	private boolean directUp = true;
	private SensorEventListener sensorListener;

	@Override
	protected void onLoad(Parameter parameter) {
		toolbarList = new ArrayList<ToolbarView>();
		new TGHandler() {
			@Handle()
			private void onLogin(UserLoginEvent event) {
				if (UserLoginEvent.SUCCESS == event.getResult()) {
					showLoginHint();
					showToolbar();
				}
			}

			@Handle()
			private void onLogout(UserLogoutEvent event) {
				if (event.getResult() == UserLogoutEvent.SUCCESS) {
					hideToolbar();
				}
			}

			@Handle()
			private void onActivityStart(ActivityStartEvent event) {
				updateUnreadMessageCount();
			}

		}.register();

		SensorManager sm = (SensorManager) TGApplication.tgApplication.getSystemService(Service.SENSOR_SERVICE);
		Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				// float x = event.values[0];
				// float y = event.values[1];
				float z = event.values[2];
				if (z > 0) {
					directUp = true;
				} else {
					if (directUp) {
						onPhoneFlip();
						directUp = false;
					} else {
						// do nothing
					}
				}

			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		sm.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void turnOnFlipFunction() {
		Module.of(SPCache.class).save("SPK_FLIP_FUNCTION", "true");
	}

	protected boolean isFlipFunctionEnabled() {
		return !TextUtils.isEmpty(Module.of(SPCache.class).load("SPK_FLIP_FUNCTION"));
	}

	protected void onPhoneFlip() {
		if (isFlipFunctionEnabled() && Module.of(UserCenter.class).getActiveUser() != null) {
			if (isToolbarShow) {
				hideToolbar();
			} else {
				showToolbar();
			}
		}
	}

	@Override
	protected void onRelease() {
		if (getContext() != null) {
			SensorManager sm = (SensorManager) getContext().getSystemService(Service.SENSOR_SERVICE);
			sm.unregisterListener(sensorListener);
			toolbarList.clear();
			Log.e("TAG", "run: onRelease"   );
		}
	}

	public void showLoginHint() {
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
		        int tg_loginhint_toast_id = getContext().getResources().getIdentifier("tg_loginhint_toast", "layout", getContext().getPackageName());
		        int tg_login_hint_id = getContext().getResources().getIdentifier("tg_login_hint", "id", getContext().getPackageName());

				View layout = View.inflate(getContext(), tg_loginhint_toast_id, null);
				TextView hint = (TextView) layout.findViewById(tg_login_hint_id);
				hint.setText(TGString.layout_loginhint + "\n" + Module.of(UserCenter.class).getDisplayNickname());
				Toast toast = new Toast(getContext());
				toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, 0);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
			}
		});
	}

	public void resetUnreadNoticeCount() {
		// ToolbarService.unreadNoticeCount = 0;
		updateUnreadMessageCount();
	}

	public void updateUnreadMessageCount() {
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				for (ToolbarView view : toolbarList) {
					if (view != null) {
						// view.setUnreadByTag(ToolbarView.ITEMTAG_CHAT,
						// TTController.hxManager.getUnreadMsgCountTotal());
						// view.setUnreadByTag(ToolbarView.ITEMTAG_NOTICE,
						// ToolbarService.unreadNoticeCount);
						// view.setUnreadByTag(ToolbarView.ITEMTAG_ACTIVITY,
						// ToolbarService.unreadActivityCount);
					}
				}
			}
		});
	}

	public void createToolbar(Activity activity) {
		ToolbarView view = new ToolbarView(activity, activity.getWindow());
		view.setId(TOOLBAR_VIEW_ID);

		if (!isToolbarShow) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
		activity.getWindow().addContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		toolbarList.add(view);
		Log.e("TAG", "run: createToolbar"   );
	}

	public void releaseToolbar(Activity activity) {
		ToolbarView view = (ToolbarView) activity.getWindow().findViewById(TOOLBAR_VIEW_ID);
		if (view != null && toolbarList.contains(view)) {
			toolbarList.remove(view);
		} else {
			HL.w("releaseToolbar Failed.");
		}
	}

	public void reloadToolbar() {
		HL.w("reloadToolbar");
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				List<Window> windowList = new ArrayList<Window>();
				for (ToolbarView view : toolbarList) {
					try {
						windowList.add(view.getWindow());
						((ViewGroup) view.getParent()).removeView(view);
					} catch (Exception e) {
						HL.w(e);
					}
				}
				toolbarList.clear();
				for (Window window : windowList) {
					try {
						ToolbarView view = new ToolbarView(window.getContext(), window);
						view.setId(TOOLBAR_VIEW_ID);

						if (!isToolbarShow) {
							view.setVisibility(View.GONE);
						} else {
							view.setVisibility(View.VISIBLE);
						}
						window.addContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
						toolbarList.add(view);
					} catch (Exception e) {
						HL.w(e);
					}
				}
			}

		});
	}

	public void hideToolbar() {
		isToolbarShow = false;
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				for (ToolbarView view : toolbarList) {
					try {
						view.setVisibility(View.GONE);
						Log.e("TAG", "run: toolbarGone"   );
					} catch (Exception e) {
						HL.w(e);
					}
				}
			}

		});
	}

	public void showToolbar() {
		isToolbarShow = true;
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				for (ToolbarView view : toolbarList) {
					try {
						view.setVisibility(View.VISIBLE);
						view.notifyShow();
						Log.e("TAG", "run: toolbarShow"   );
					} catch (Exception e) {
						HL.w(e);
					}
				}
			}

		});
	}

}
