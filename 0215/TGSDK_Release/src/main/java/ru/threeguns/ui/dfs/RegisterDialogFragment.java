package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import ru.threeguns.R;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.CheckBoxWrapper;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.CountDownTimerUtils;
import ru.threeguns.utils.MD5Util;

public class RegisterDialogFragment extends DFragment {
    private EditTextEx accountEditText;
    private EditTextEx passwordEditText;
    private EditTextEx passwordagainEditText;
    private EditTextEx passwordvalidnEditText;
    private CheckBoxWrapper showRulesCheckBox;
    private TextView rulesTextView ,text_acc;
    private Button sendTextView;

    private int total = 60;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_register_id = getActivity().getResources().getIdentifier("tg_dialog_register", "layout", getActivity().getPackageName());
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
        int text_acc_id = getActivity().getResources().getIdentifier("text_acc", "id", getActivity().getPackageName());
        accountEditText = (EditTextEx) view.findViewById(tg_account_et_id);
        passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);
        passwordagainEditText = (EditTextEx) view.findViewById(tg_password_again_et_id);
        passwordvalidnEditText = (EditTextEx) view.findViewById(tg_password_validn_et_id);
        text_acc = (TextView) view.findViewById(text_acc_id);

        int tg_fastlogin_id = getActivity().getResources().getIdentifier("tg_fastlogin", "id", getActivity().getPackageName());
        view.findViewById(tg_fastlogin_id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestBack();
                changeFragment(FastRegisterDialogFragment.class);

            }
        });


        int tg_phonelogin_id = getActivity().getResources().getIdentifier("tg_phonelogin", "id", getActivity().getPackageName());
        view.findViewById(tg_phonelogin_id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestBack();
                changeFragment(ResgiterPhoneFragment.class);

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

        int tg_register_btn_id = getActivity().getResources().getIdentifier("tvBtnRegister", "id", getActivity().getPackageName());
        view.findViewById(tg_register_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (showRulesCheckBox.isChecked()) {
//                    final String account = accountEditText.getText().toString();
//                    final String password = passwordEditText.getText().toString();
//                    if (!validEmail(account)) {
//                        accountEditText.alert();
//                        return;
//                    }
//                    if (!validPassword(password)) {
//                        passwordEditText.alert();
//                        return;
//                    }
//                    if (!passwordEditText.getText().toString().equals(passwordagainEditText.getText().toString())){
//                        showErrorMessage(getResources().getString(R.string.tg_layout_password_notmatch));
//                        return;
//                    }
//                    Log.e("TAG", "onClick: "+ account +   MD5Util.md5(password) + passwordvalidnEditText.getText().toString());
//                    HClient.of(UserApi.class).apiregisteremail(account, MD5Util.md5(password), passwordvalidnEditText.getText().toString(), new TGResultHandler() {
//                        @Override
//                        protected void onSuccess(final JSONObject json) throws JSONException {
//                            Module.of(UserCenter.class).notifyRegisterSuccess(account, MD5Util.md5(password), User.USERTYPE_NORMAL);
//                            HClient.of(UserApi.class).login(account, MD5Util.md5(password), new TGResultHandler() {
//
//                                @Override
//                                protected void onSuccess(JSONObject json) throws JSONException {
//                                    Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password));
//                                    requestExit(false);
//                                }
//
//                                @Override
//                                protected void onFailed(int errorCode, String msg) {
//                                    showErrorMessage(msg);
//                                }
//                            });
//                        }
//
//                        @Override
//                        protected void onFailed(final int errorCode, final String msg) {
//                            Log.e("TAG", "onFailed: " + errorCode + msg );
//                            showErrorMessage(msg);
//                        }
//                    });
//
////                    HClient.of(UserApi.class).register(account, MD5Util.md5(password), new TGResultHandler() {
////
////                        @Override
////                        protected void onSuccess(JSONObject json) throws JSONException {
////                            Module.of(UserCenter.class).notifyRegisterSuccess(account, MD5Util.md5(password), User.USERTYPE_NORMAL);
////                            HClient.of(UserApi.class).login(account, MD5Util.md5(password), new TGResultHandler() {
////
////                                @Override
////                                protected void onSuccess(JSONObject json) throws JSONException {
////                                    Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(MD5Util.md5(password)));
////                                    requestExit(false);
////                                }
////
////                                @Override
////                                protected void onFailed(int errorCode, String msg) {
////                                    showErrorMessage(msg);
////                                }
////                            });
////                        }
////
////                        @Override
////                        protected void onFailed(int errorCode, String msg) {
////                            showErrorMessage(msg);
////                        }
////                    });
//                } else {
//                    Module.of(UIHelper.class).showToast(TGString.please_accept_agreement);
//                }
            }
        });

        int guest_register_rules_tv_id = getActivity().getResources().getIdentifier("guest_register_rules_tv", "id", getActivity().getPackageName());
        rulesTextView = (TextView) view.findViewById(guest_register_rules_tv_id);
        rulesTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        rulesTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changeFragment(RulesFragment.class);
            }
        });
        int guest_register_rules_layout_id = getActivity().getResources().getIdentifier("guest_register_rules_layout", "id", getActivity().getPackageName());
        int guest_register_rules_checkbox_id = getActivity().getResources().getIdentifier("guest_register_rules_checkbox", "id", getActivity().getPackageName());
        showRulesCheckBox = new CheckBoxWrapper(view.findViewById(guest_register_rules_layout_id),
                (ImageView) view.findViewById(guest_register_rules_checkbox_id), true);

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
                showRulesCheckBox.setChecked(true);
            }
        });
    }
}