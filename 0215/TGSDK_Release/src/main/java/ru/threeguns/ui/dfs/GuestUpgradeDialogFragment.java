package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.ui.Demand;
import ru.threeguns.R;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.fragments.AountDialogFragment;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.CountDownTimerUtils;
import ru.threeguns.utils.MD5Util;

public class GuestUpgradeDialogFragment extends DFragment {
    private DropEditTextEx accountEditText;
    private EditTextEx passwordEditText;
    private EditTextEx passwordagainEditText;
    private DropEditTextEx passwordvalidnEditText;
    private Button sendTextView;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_register_id = getActivity().getResources().getIdentifier("tg_dialog_guestupgrade", "layout", getActivity().getPackageName());
        View view = inflater.inflate(tg_fragment_register_id, null);
        Activity activity = getActivity();
        if (activity instanceof LoginActivity){
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.setCenter();
        }
        int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
        int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
        int tg_password_again_et_id = getActivity().getResources().getIdentifier("tg_password_et_again", "id", getActivity().getPackageName());
        int tg_password_validn_et_id = getActivity().getResources().getIdentifier("tg_account_et_v", "id", getActivity().getPackageName());
        accountEditText = (DropEditTextEx) view.findViewById(tg_account_et_id);
        passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);
        passwordagainEditText = (EditTextEx) view.findViewById(tg_password_again_et_id);
        passwordvalidnEditText = (DropEditTextEx) view.findViewById(tg_password_validn_et_id);
        accountEditText.hideDropButton();
        passwordvalidnEditText.hideDropButton();
        int tg_phonelogin_id = getActivity().getResources().getIdentifier("tg_phonelogin", "id", getActivity().getPackageName());
        view.findViewById(tg_phonelogin_id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestBack();
                changeFragment(GuestPhoneDialogFragment.class);
            }
        });

        int tg_back_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
        view.findViewById(tg_back_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBack();
            }
        });

        int tg_send_id = getActivity().getResources().getIdentifier("tg_send", "id", getActivity().getPackageName());
        sendTextView = (Button) view.findViewById(tg_send_id);
        sendTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String account = accountEditText.getText().toString();

                Log.e("TAG", "onClick: " + "aaa" );
                if (!validEmail(account)) {
                    accountEditText.alert();
                    return;
                }

                HClient.of(UserApi.class).sendTgEmail(account, new TGResultHandler() {
                    @Override
                    protected void onSuccess(JSONObject json) throws JSONException {
                        Log.e("TAG", "onSuccess: " + json );
                    }

                    @Override
                    protected void onFailed(int errorCode, String msg) {

                    }
                });

                CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(sendTextView, 60000, 1000,getContext()); //倒计时1分钟
                mCountDownTimerUtils.start();

            }
        });

        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
        view.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String account = accountEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                if (!validEmail(account)) {
                    accountEditText.alert();
                    return;
                }
                if (!validPassword(password)) {
                    passwordEditText.alert();
                    return;
                }
                if (!passwordEditText.getText().toString().equals(passwordagainEditText.getText().toString())){
                    showErrorMessage(getResources().getString(R.string.tg_layout_password_notmatch));
                    return;
                }
                User activeUser = Module.of(UserInfoCache.class).getActiveUser();
                HClient.of(UserApi.class).apibingaccount(account, MD5Util.md5(password), passwordvalidnEditText.getText().toString(),activeUser.getToken(),activeUser.getUserId(), new TGResultHandler() {
                    @Override
                    protected void onSuccess(final JSONObject json) throws JSONException {
                        Module.of(UserCenter.class).notifyGuestUpgradeSuccess(account, MD5Util.md5(password));
                        changeFragment(new Demand(AountDialogFragment.class).clear(true).back(true));

                    }

                    @Override
                    protected void onFailed(final int errorCode, final String msg) {
                        Module.of(UIHelper.class).showToast(msg);
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
                accountEditText.setText("");
                passwordEditText.setText("");
                passwordagainEditText.setText("");
                passwordvalidnEditText.setText("");
            }
        });
    }
}