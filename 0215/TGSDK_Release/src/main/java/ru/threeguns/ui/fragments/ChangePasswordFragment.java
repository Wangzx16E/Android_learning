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
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.MD5Util;
import ru.threeguns.utils.VerifyUtil;

public class ChangePasswordFragment extends HFragment {
	private EditTextEx oriPasswordEditText;
	private EditTextEx newPasswordEditText;
	private EditTextEx newPasswordEditText2;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_changepwd_id = getActivity().getResources().getIdentifier("tg_fragment_changepwd", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_changepwd_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
        int tg_newpw_et_id = getActivity().getResources().getIdentifier("tg_newpw_et", "id", getActivity().getPackageName());
        int tg_newpw_et2_id = getActivity().getResources().getIdentifier("tg_newpw_et2", "id", getActivity().getPackageName());
		oriPasswordEditText = (EditTextEx) v.findViewById(tg_password_et_id);
		newPasswordEditText = (EditTextEx) v.findViewById(tg_newpw_et_id);
		newPasswordEditText2 = (EditTextEx) v.findViewById(tg_newpw_et2_id);

        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String oriPassword = oriPasswordEditText.getText().toString();
				String newPassword = newPasswordEditText.getText().toString();
				String newPassword2 = newPasswordEditText2.getText().toString();

				String msg = null;
				msg = VerifyUtil.verifyPassword(oriPassword);
				if (msg != null) {
					oriPasswordEditText.alert();
					Module.of(UIHelper.class).showToast(msg);
					return;
				}
				msg = VerifyUtil.verifyPassword(newPassword);
				if (msg != null) {
					newPasswordEditText.alert();
					Module.of(UIHelper.class).showToast(msg);
					return;
				}
				msg = VerifyUtil.verifyPassword(newPassword2);
				if (msg != null) {
					newPasswordEditText2.alert();
					Module.of(UIHelper.class).showToast(msg);
					return;
				}
				if (!newPassword.equals(newPassword2)) {
					newPasswordEditText.alert();
					newPasswordEditText2.alert();
					Module.of(UIHelper.class).showToast(TGString.new_password_unmatch);
					return;
				}

				if (newPassword.equals(oriPassword)) {
					newPasswordEditText.alert();
					newPasswordEditText2.alert();
					Module.of(UIHelper.class).showToast(TGString.new_password_not_change);
					return;
				}

				final String pass = newPassword;

				HClient.of(UserApi.class).changePassword(//
						Module.of(UserCenter.class).getActiveUser().getUsername(), //
						MD5Util.md5(oriPassword), //
						MD5Util.md5(newPassword), //
						new TGResultHandler() {

					@Override
					protected void onSuccess(JSONObject json) throws JSONException {
						Module.of(UserCenter.class).notifyChangePassword(MD5Util.md5(pass));
						changeFragment(new Demand(AccountFragment.class).clear(true).back(true));
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
				oriPasswordEditText.setText("");
				newPasswordEditText.setText("");
				newPasswordEditText2.setText("");
			}
		});

	}
}
