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
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.utils.CountDownTimerUtils;

public class RestorePasswordDialogFragment extends DFragment {
    private DropEditTextEx accountEditText;
    private DropEditTextEx passwordvalidnEditText;
    private Button sendTextView;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_register_id = getActivity().getResources().getIdentifier("tg_dialog_restorepwd", "layout", getActivity().getPackageName());
        View view = inflater.inflate(tg_fragment_register_id, null);
        Activity activity = getActivity();
        if (activity instanceof LoginActivity){
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.setCenter();
        }

        int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
        int tg_password_validn_et_id = getActivity().getResources().getIdentifier("tg_account_et_v", "id", getActivity().getPackageName());
        int text_acc_id = getActivity().getResources().getIdentifier("text_acc", "id", getActivity().getPackageName());
        accountEditText = (DropEditTextEx) view.findViewById(tg_account_et_id);
        passwordvalidnEditText = (DropEditTextEx) view.findViewById(tg_password_validn_et_id);
        passwordvalidnEditText.hideDropButton();
        accountEditText.hideDropButton();
//        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
//        view.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                requestBack();
//            }
//        });

        int tg_phonelogin_id = getActivity().getResources().getIdentifier("tg_phonelogin", "id", getActivity().getPackageName());
        view.findViewById(tg_phonelogin_id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestBack();
                changeFragment(RestorePasswordPhoneFragment.class);
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
                    if (!validEmail(account)) {
                        accountEditText.alert();
                        return;
                    }
                    if (null == passwordvalidnEditText || "".equals(passwordvalidnEditText.getText().toString())){
                        showErrorMessage(getResources().getString(R.string.tg_layout_validn_null));
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("account",account);
                    bundle.putString("validn",passwordvalidnEditText.getText().toString());
                    changeFragment(new Demand(ValidnSuccessFragment.class).bundle(bundle));


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
                passwordvalidnEditText.setText("");
            }
        });
    }
}