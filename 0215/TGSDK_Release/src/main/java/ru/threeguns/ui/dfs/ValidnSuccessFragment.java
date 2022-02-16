package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import ru.threeguns.R;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.MD5Util;

public class ValidnSuccessFragment  extends DFragment {
    private EditTextEx passwordEditText;
    private EditTextEx passwordagainEditText;
    private String account = "";
    private String validn = "";
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_register_id = getActivity().getResources().getIdentifier("validn_success", "layout", getActivity().getPackageName());
        View view = inflater.inflate(tg_fragment_register_id, null);
        Activity activity = getActivity();
        if (activity instanceof LoginActivity){
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.setCenter();
        }
        Bundle bundle = getArguments();
        account = bundle.getString("account");
        validn = bundle.getString("validn");
        int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
        int tg_password_again_et_id = getActivity().getResources().getIdentifier("tg_password_et_again", "id", getActivity().getPackageName());
        passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);
        passwordagainEditText = (EditTextEx) view.findViewById(tg_password_again_et_id);

//        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
//        view.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                requestBack();
//            }
//        });
        int tg_back_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
        view.findViewById(tg_back_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBack();
            }
        });
        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
        view.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password = passwordEditText.getText().toString();
                if (!validPassword(password)) {
                    passwordEditText.alert();
                    return;
                }
                if (!passwordEditText.getText().toString().equals(passwordagainEditText.getText().toString())){
                    showErrorMessage(getResources().getString(R.string.tg_layout_password_notmatch));
                    return;
                }

                Log.e("TAG6622222", "onClick: " + account + ".." + MD5Util.md5(password) + ".." + validn);
                HClient.of(UserApi.class).apirestorepwd(account, MD5Util.md5(password), validn, new TGResultHandler() {
                    @Override
                    protected void onSuccess(final JSONObject json) throws JSONException {
                        showErrorMessage(getResources().getString(R.string.tg_layout_changepwd));
                        requestExit(false);
                    }

                    @Override
                    protected void onFailed(final int errorCode, final String msg) {
                        showErrorMessage(msg);
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onEnter() {
        super.onEnter();

        Module.of(UIHelper.class).post2MainThread(new Runnable() {
            @Override
            public void run() {
                passwordEditText.setText("");
                passwordagainEditText.setText("");
            }
        });
    }
}