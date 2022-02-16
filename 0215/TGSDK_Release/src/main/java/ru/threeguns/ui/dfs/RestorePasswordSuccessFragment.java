package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kh.hyper.core.Module;
import kh.hyper.ui.Demand;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.ui.LoginActivity;

public class RestorePasswordSuccessFragment extends DFragment {
	private TextView emailText;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_restorepwd_success_id = getActivity().getResources().getIdentifier("tg_fragment_restorepwd_success", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_restorepwd_success_id, null);
		Activity activity = getActivity();
		if (activity instanceof LoginActivity){
			LoginActivity loginActivity = (LoginActivity) getActivity();
			loginActivity.setCenter();
		}
        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeFragment(new Demand(LoginFragment.class).back(true).clear(true));
			}
		});

        int tg_exit_btn_id = getActivity().getResources().getIdentifier("tg_exit_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestExit(true);
			}
		});

        int tg_email_text_id = getActivity().getResources().getIdentifier("tg_email_text", "id", getActivity().getPackageName());
		emailText = (TextView) v.findViewById(tg_email_text_id);

		return v;
	}

	@Override
	public void onEnter() {
		super.onEnter();

		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			@Override
			public void run() {
				Bundle bundle = getArguments();
				emailText.setText(bundle.getString("email"));
			}
		});
	}
}
