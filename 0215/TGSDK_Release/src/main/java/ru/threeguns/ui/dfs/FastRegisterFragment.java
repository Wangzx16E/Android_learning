package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.MD5Util;

public class FastRegisterFragment extends DFragment {
	private EditTextEx accountEditText;
	private EditTextEx passwordEditText;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = null;
		Activity activity = getActivity();
		if (activity instanceof LoginActivity){
			LoginActivity loginActivity = (LoginActivity) getActivity();
			loginActivity.setCenter();
		}
		int tg_fragment_fast_register_id = getActivity().getResources().getIdentifier("tg_fragment_fast_register", "layout", getActivity().getPackageName());
		view = inflater.inflate(tg_fragment_fast_register_id, null);

		int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
		int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
		accountEditText = (EditTextEx) view.findViewById(tg_account_et_id);
		passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);

		accountEditText.setKeyListener(null);

		int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		view.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

		int tg_exit_btn_id = getActivity().getResources().getIdentifier("tg_exit_btn", "id", getActivity().getPackageName());
		view.findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				requestExit(true);
			}
		});

		int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
		view.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				loginClick();
			}
		});

		return view;
	}

	private void takeScreenShot(final String username) {
		final View view = getView().getRootView();
		view.post(new Runnable() {
			public void run() {
				view.setDrawingCacheEnabled(true);
				view.buildDrawingCache();

				Bitmap bitmap = view.getDrawingCache();

				if (bitmap != null) {
					try {
						String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/3guns";
						File folderFile = new File(folderPath);
						if (!folderFile.exists()) {
							folderFile.mkdirs();
						}
						String path = folderPath + "/account_" + username + ".png";
						FileOutputStream out = new FileOutputStream(path);
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
						out.close();
						view.destroyDrawingCache();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

//	private void loginClick() {
//		final String username = accountEditText.getText().toString();
//		final String password = passwordEditText.getText().toString();
//		if (!validAccount(username)) {
//			accountEditText.alert();
//			return;
//		}
//		if (!validPassword(password)) {
//			passwordEditText.alert();
//			return;
//		}
//		HClient.of(UserApi.class).fastRegister(username, MD5Util.md5(password), new TGResultHandler() {
//
//			@Override
//			protected void onSuccess(JSONObject json) throws JSONException {
//				takeScreenShot(username);
//				Module.of(UserCenter.class).notifyRegisterSuccess(username, MD5Util.md5(password), User.USERTYPE_GUEST);
//				Module.of(SPCache.class).save("request_bind", "holder");
//				HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {
//
//					@Override
//					protected void onSuccess(JSONObject json) throws JSONException {
//						Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(MD5Util.md5(password)));
//						requestExit(false);
//					}
//
//					@Override
//					protected void onFailed(int errorCode, String msg) {
//						showErrorMessage(msg);
//					}
//				});
//			}
//
//			@Override
//			protected void onFailed(int errorCode, String msg) {
//				showErrorMessage(msg);
//			}
//		});
//	}

	private String randomStr() {
		String src = new Random().nextFloat() + "" + System.currentTimeMillis();
		return MD5Util.md5(src).substring(0, 8);
	}

	@Override
	public void onEnter() {
		super.onEnter();
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				accountEditText.setText(randomStr());
				passwordEditText.setText(randomStr());
			}
		});
	}
}
