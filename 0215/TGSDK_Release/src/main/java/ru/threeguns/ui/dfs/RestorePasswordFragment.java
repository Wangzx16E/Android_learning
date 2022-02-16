package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.ui.Demand;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.EditTextEx;

public class RestorePasswordFragment extends DFragment {
	private EditTextEx accountEditText;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_restorepwd_id = getActivity().getResources().getIdentifier("tg_fragment_restorepwd", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_restorepwd_id, null);
		Activity activity = getActivity();
		if (activity instanceof LoginActivity){
			LoginActivity loginActivity = (LoginActivity) getActivity();
			loginActivity.setCenter();
		}
        int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
		accountEditText = (EditTextEx) v.findViewById(tg_account_et_id);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_exit_btn_id = getActivity().getResources().getIdentifier("tg_exit_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestExit(true);
			}
		});

        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String account = accountEditText.getText().toString();
				if (!validEmail(account)) {
					return;
				}

				HClient.of(UserApi.class).restorePassword(account, new TGResultHandler() {
					@Override
					protected void onSuccess(JSONObject json) throws JSONException {
						Bundle bundle = new Bundle();
						bundle.putString("email", account);
						changeFragment(new Demand(RestorePasswordSuccessFragment.class).bundle(bundle));
					}

					@Override
					protected void onFailed(int errorCode, String msg) {
						Module.of(UIHelper.class).showToast(msg);
					}
				});

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
				accountEditText.setText("");
			}
		});
	}
}
