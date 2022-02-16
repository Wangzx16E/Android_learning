package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.CheckBoxWrapper;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.LocaleUtil;
import ru.threeguns.utils.MD5Util;

public class AccountRegisterFragment  extends DFragment {
    private DropEditTextEx accountEditText;
    private DropEditTextEx passwordEditText;

    private static final int TP_ICON_SIZE = 40;
    private static final int TP_ICON_PADDING = 8;
    private ListView historyListView;
    private PopupWindow accountHistoryPopup;
    private List<InternalUser> userList = new ArrayList<>();
    private String realPassword;
    private boolean userEditPassword = false;
    private TextView rulesTextView;
    private CheckBoxWrapper showRulesCheckBox;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        Activity activity = getActivity();
        if (activity instanceof LoginActivity){
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.setCenter();
        }
        int tg_fragment_fast_register_id = getActivity().getResources().getIdentifier("tg_account_register", "layout", getActivity().getPackageName());
        view = inflater.inflate(tg_fragment_fast_register_id, null);

        int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
        int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
        accountEditText = (DropEditTextEx) view.findViewById(tg_account_et_id);
        passwordEditText = (DropEditTextEx) view.findViewById(tg_password_et_id);
        passwordEditText.hideDropButton();
        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_login_btn", "id", getActivity().getPackageName());
        view.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClick();
            }
        });

        int tg_to_login_btn_id = getActivity().getResources().getIdentifier("tg_to_login_btn", "id", getActivity().getPackageName());
        view.findViewById(tg_to_login_btn_id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestBack();
                changeFragment(EmLoginDialogFragment.class);
            }
        });
        int tg_fast_register_id = getActivity().getResources().getIdentifier("tg_fast_register", "id", getActivity().getPackageName());
        view.findViewById(tg_fast_register_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(FastRegisterDialogFragment.class);
            }
        });

        int tg_back_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
        view.findViewById(tg_back_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBack();
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

//        initAccountHistory();
//        initLocale(view);
        return view;
    }


    @SuppressWarnings("deprecation")
    private void initAccountHistory() {
        userList = Module.of(UserInfoCache.class).getAllUsers();
        List<InternalUser> tempList = new ArrayList<InternalUser>();
        for (InternalUser u : userList) {
            String userType = u.getUserType();
            if (!User.USERTYPE_NORMAL.equals(userType) && !User.USERTYPE_GUEST.equals(userType)) {
                tempList.add(u);
            }
        }
        userList.removeAll(tempList);

        historyListView = new ListView(getActivity()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int width = accountEditText.getMeasuredWidth() + MeasureSpec.EXACTLY;
                super.onMeasure(width, heightMeasureSpec);
            }
        };
        historyListView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        historyListView.setAdapter(new InternalAdapter());
        historyListView.setDivider(null);
        historyListView.setFocusableInTouchMode(true);
        int tg_whitebtn_id = getActivity().getResources().getIdentifier("tg_whitebtn", "drawable", getActivity().getPackageName());
        historyListView.setSelector(tg_whitebtn_id);
        historyListView.setFocusable(true);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (accountHistoryPopup.isShowing()) {
                    accountHistoryPopup.dismiss();
                }
                final InternalUser user = userList.get(position);
                realPassword = user.getPassword();
                Module.of(UIHelper.class).post2MainThread(new Runnable() {
                    @Override
                    public void run() {
                        accountEditText.setText(user.getUsername());
                        if (!TextUtils.isEmpty(user.getPassword())) {
                            passwordEditText.setText(user.getPassword());
                        } else {
                            passwordEditText.setText("");
                        }
                        userEditPassword = false;
                    }
                });
            }

        });
        accountHistoryPopup = new PopupWindow(historyListView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int tg_et_container_bg_id = getActivity().getResources().getIdentifier("tg_et_container_bg", "drawable", getActivity().getPackageName());
        accountHistoryPopup.setBackgroundDrawable(getResources().getDrawable(tg_et_container_bg_id));
        accountHistoryPopup.setOutsideTouchable(true);
        accountHistoryPopup.setFocusable(true);
        accountHistoryPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                accountEditText.packupDropButton();
            }
        });

    }

    private boolean firstTimeSelectLocale = true;
    private void initLocale(View view) {
        int tg_spinner_id = getActivity().getResources().getIdentifier("tg_spinner", "id", getActivity().getPackageName());
        Spinner localeSpinner = (Spinner) view.findViewById(tg_spinner_id);
        if (Constants.ENABLE_CUSTOM_LOCALE) {
            localeSpinner.setVisibility(View.VISIBLE);
            final List<String> list = new ArrayList<String>();
            list.add(LocaleUtil.FOLLOW_SYSTEM);
            list.add("th");
            list.add("zh-rCN");
            list.add("zh-rTW");

            int simple_spinner_item_id = getActivity().getResources().getIdentifier("simple_spinner_item", "layout", getActivity().getPackageName());
            int simple_spinner_dropdown_item_id = getActivity().getResources().getIdentifier("simple_spinner_dropdown_item", "layout", getActivity().getPackageName());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), simple_spinner_item_id, list);
            adapter.setDropDownViewResource(simple_spinner_dropdown_item_id);
            localeSpinner.setAdapter(adapter);
            localeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (firstTimeSelectLocale) {
                        firstTimeSelectLocale = false;
                        return;
                    }
                    String locale = list.get(position);
                    Module.of(SPCache.class).save(Constants.SPK_LOCALE, locale);
                    Locale l = LocaleUtil.fromString(locale);
                    Resources res = getResources();
                    Configuration config = res.getConfiguration();
                    config.locale = l;
                    res.updateConfiguration(config, res.getDisplayMetrics());

                    Module.of(TGString.class).refreshString();

                    requestExit(false);

                    Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), LoginActivity.class);
                    Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            String locale = Module.of(SPCache.class).load(Constants.SPK_LOCALE);
            if (!TextUtils.isEmpty(locale)) {
                int idx = list.indexOf(locale);
                localeSpinner.setSelection(idx, true);
            }
        } else {
            localeSpinner.setVisibility(View.GONE);
        }
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

    private void loginClick() {
        final String username = accountEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        if (!validAccount(username)) {
            accountEditText.alert();
            return;
        }
        if (!validPassword(password)) {
            passwordEditText.alert();
            return;
        }
        if (!showRulesCheckBox.isChecked()){
            Module.of(UIHelper.class).showToast(TGString.please_accept_agreement);
            return;
        }
        HClient.of(UserApi.class).fastRegister(username, MD5Util.md5(password), new TGResultHandler() {

            @Override
            protected void onSuccess(JSONObject json) throws JSONException {
                takeScreenShot(username);
                Module.of(UserCenter.class).notifyRegisterSuccess(username, MD5Util.md5(password), User.USERTYPE_GUEST);
                Module.of(SPCache.class).save("request_bind", "holder");
                Log.e("TAGfff", "onSuccess: kuaisucg" );
                HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {

                    @Override
                    protected void onSuccess(JSONObject json) throws JSONException {
                        Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password),true);
                        requestExit(false);
                        Log.e("TAGfff", "onSuccess: dengl" );
                    }

                    @Override
                    protected void onFailed(int errorCode, String msg) {
                        showErrorMessage(msg);
                    }
                });
            }

            @Override
            protected void onFailed(int errorCode, String msg) {
                Log.e("TAGfff", "onSuccess: kuaisusb" );
                if (30007 == errorCode){
                    showErrorMessage(msg);
                    return;
                }

                Log.e("tgsdk account register", "onFailed: " + errorCode );
                HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {

                    @Override
                    protected void onSuccess(JSONObject json) throws JSONException {
                        Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password),false);
                        requestExit(false);
                    }

                    @Override
                    protected void onFailed(int errorCode, String msg) {
                        showErrorMessage(msg);
                    }
                });
            }
        });
    }

    private String randomStr() {
        String src = new Random().nextFloat() + "" + System.currentTimeMillis();
        return MD5Util.md5(src).substring(0, 8);
    }

    @Override
    public void onEnter() {
        super.onEnter();

        userEditPassword = false;

        if (userList.size() > 0) {
            if (userList.size() == 1) {
                accountEditText.hideDropButton();
            } else {
                accountEditText.setOnDropClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (accountEditText.isDrop()) {
                            if (!accountHistoryPopup.isShowing()) {
                                Module.of(UIHelper.class).post2MainThread(new Runnable() {
                                    public void run() {
                                        accountHistoryPopup.showAsDropDown(accountEditText, 0, 5);
                                    }
                                });
                            }
                        } else {
                            // if (accountHistoryPopup.isShowing()) {
                            // accountHistoryPopup.dismiss();
                            // }
                        }
                    }
                });
            }
        } else {
            accountEditText.hideDropButton();
        }

        if (userList.size() > 0) {
            InternalUser user = userList.get(0);
            if (User.USERTYPE_GUEST.equals(user.getUserType()) || User.USERTYPE_GUEST.equals(user.getUserType())) {
                final String username = user.getUsername();
                final String password = user.getPassword();
                realPassword = user.getPassword();

                Module.of(UIHelper.class).post2MainThread(new Runnable() {
                    @Override
                    public void run() {
                        accountEditText.setText(username);
                        if (!TextUtils.isEmpty(password)) {
                            passwordEditText.setText(password);
                        } else {
                            passwordEditText.setText("");
                        }
                        userEditPassword = false;
                    }
                });
            }
        }
    }

    private class InternalAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                int tg_item_account_history_id = getActivity().getResources().getIdentifier("tg_item_account_history", "layout", getActivity().getPackageName());
                convertView = View.inflate(getActivity(), tg_item_account_history_id, null);
            }
            InternalUser user = userList.get(position);

            int tg_text_id = getActivity().getResources().getIdentifier("tg_text", "id", getActivity().getPackageName());
            TextView tv = (TextView) convertView.findViewById(tg_text_id);
            tv.setText(user.getUsername());
            return convertView;
        }

    }
}