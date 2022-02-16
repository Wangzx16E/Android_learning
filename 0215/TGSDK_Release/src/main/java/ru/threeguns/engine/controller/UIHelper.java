package ru.threeguns.engine.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.VerifyUtil;

public class UIHelper extends Module {
	private Handler mainThreadHandler;
	private Toast commonToast;

	@Override
	protected void onLoad(Parameter parameter) {
		mainThreadHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onRelease() {

	}

	private static final int HIDE = 0;
	private static final int READY_SHOW = 1;
	private static final int SHOW = 2;
	private static final int READY_HIDDEN = 3;

	// Loading对话框
	private Dialog progressDialog;
	private TextView progressTextView;
	private int progressDialogState = HIDE;
	private boolean requestDismiss;

	public void showProgressDialog(String message) {
		Activity act = Module.of(ActivityHolder.class).getTopActivity();
		if (act != null) {
			showProgressDialog(act, message);
		} else {
			HL.w("Unexpected.Cannot get top Activity when showProgressDialog.");
		}
	}

	public void showProgressDialog(final Context context, String message) {
		if (progressDialogState == SHOW || progressDialogState == READY_SHOW) {
			return;
		}
		final String msg;
		if (!VerifyUtil.notEmpty(message)) {
			msg = TGString.network_loading_loading;
		} else {
			msg = message;
		}
		requestDismiss = false;
		progressDialogState = READY_SHOW;
		post2MainThread(new Runnable() {

			@Override
			public void run() {
				if (progressDialog == null) {
					createProgressDialog(context);
				}
				progressTextView.setText(msg);
				showDialogSafe();
			}
		});
	}

	public void closeProgressDialog() {
		switch (progressDialogState) {
		case SHOW:
			dismissDialogSafe();
			break;
		case READY_SHOW:
			requestDismiss = true;
			break;
		case HIDE:
			break;
		case READY_HIDDEN:
			break;
		}
	}

	private void createProgressDialog(Context context) {
		int tg_transparent_dialog_id = getContext().getResources().getIdentifier("tg_transparent_dialog", "style", getContext().getPackageName());
		int tg_progress_dialog_id = getContext().getResources().getIdentifier("tg_progress_dialog", "layout", getContext().getPackageName());
		int progress_dialog_text_id = getContext().getResources().getIdentifier("progress_dialog_text", "id", getContext().getPackageName());
		progressDialog = new Dialog(context, tg_transparent_dialog_id);
		progressDialog.setContentView(tg_progress_dialog_id);
		progressTextView = (TextView) progressDialog.findViewById(progress_dialog_text_id);
		progressDialog.setCancelable(false);
		progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				if (requestDismiss) {
					requestDismiss = false;
					dismissDialogSafe();
				} else {
					progressDialogState = SHOW;
				}
			}
		});
		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				progressDialogState = HIDE;
				progressDialog = null;
				progressTextView = null;
			}
		});
	}

	private void dismissDialogSafe() {
		post2MainThread(new Runnable() {
			@Override
			public void run() {
				try {
					progressDialogState = READY_HIDDEN;
					if (progressDialog != null) {
						progressDialog.dismiss();
					} else {
						progressDialogState = HIDE;
					}
				} catch (Exception e) {
					HL.w("Dismiss ProgressDialog throw Exception : ");
					HL.w(e);
				}
			}
		});
	}

	private void showDialogSafe() {
		post2MainThread(new Runnable() {
			@Override
			public void run() {
				try {
					progressDialogState = READY_SHOW;
					if (progressDialog != null) {
						progressDialog.show();
					} else {
						progressDialogState = HIDE;
					}
				} catch (Exception e) {
					HL.w("Show ProgressDialog throw Exception : ");
					HL.w(e);
					progressDialogState = HIDE;
				}
			}
		});
	}

	public void post2MainThread(Runnable r) {
		mainThreadHandler.post(r);
	}

	public void post2MainThreadDelayed(Runnable r, long delayMillis) {
		mainThreadHandler.postDelayed(r, delayMillis);
	}

	public void showToast(final String message) {
		post2MainThread(new Runnable() {
			@Override
			public void run() {
				if (commonToast == null) {
					commonToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
					commonToast.setGravity(Gravity.CENTER, 0, 0);
				}
				commonToast.setDuration(Toast.LENGTH_SHORT);
				commonToast.setText(message);
				commonToast.show();
			}
		});
	}

}
