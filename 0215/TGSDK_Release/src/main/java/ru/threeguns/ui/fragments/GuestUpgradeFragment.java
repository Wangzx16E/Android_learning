package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.ui.Demand;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.MD5Util;
import ru.threeguns.utils.VerifyUtil;

public class GuestUpgradeFragment extends HFragment {
	private EditTextEx usernameEditText;
	private EditTextEx passwordEditText;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_guestupgrade_id = getActivity().getResources().getIdentifier("tg_fragment_guestupgrade", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_guestupgrade_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
        int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
		usernameEditText = (EditTextEx) v.findViewById(tg_account_et_id);
		passwordEditText = (EditTextEx) v.findViewById(tg_password_et_id);

        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String username = usernameEditText.getText().toString();
				final String password = passwordEditText.getText().toString();

				String msg = VerifyUtil.verifyEmail(username);
				if (msg != null) {
					usernameEditText.alert();
					Module.of(UIHelper.class).showToast(msg);
				} else {
					msg = VerifyUtil.verifyPassword(password);
					if (msg != null) {
						passwordEditText.alert();
						Module.of(UIHelper.class).showToast(msg);
					} else {
						HClient.of(UserApi.class).upgrade(username, MD5Util.md5(password), new TGResultHandler() {

							@Override
							protected void onSuccess(JSONObject json) throws JSONException {
								Module.of(UserCenter.class).notifyGuestUpgradeSuccess(username, MD5Util.md5(password));
								changeFragment(new Demand(AccountFragment.class).clear(true).back(true));
							}

							@Override
							protected void onFailed(int errorCode, String msg) {
								Module.of(UIHelper.class).showToast(msg);
							}
						});
					}
				}
			}
		});

		return v;
	}

	@Override
	public void onEnter() {
		super.onEnter();

		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				usernameEditText.setText("");
				passwordEditText.setText("");
			}
		});
	}
}
