package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.utils.HL;
import ru.threeguns.R;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.CheckBoxWrapper;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.DpUtil;
import ru.threeguns.utils.LocaleUtil;
import ru.threeguns.utils.MD5Util;

public class EmLoginDialogFragment extends DFragment {
  //    private static final String PW_BLANK = "4Kf9wQm0";
  private CheckBoxWrapper showRulesCheckBox;
  private DropEditTextEx accountEditText;
  private EditTextEx passwordEditText;
  private String realPassword;
  private boolean userEditPassword = false;
  private ListView historyListView;
  private PopupWindow accountHistoryPopup;
  private List<InternalUser> userList = new ArrayList<>();
  private TextView rulesTextView;
  private LoginActivity loginActivity;
  private boolean firstTimeSelectLocale = true;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view;
    Activity activity = getActivity();
    Resources res = activity.getResources();
    String packageName = activity.getPackageName();

    if (activity instanceof LoginActivity) {
      loginActivity = (LoginActivity) activity;
      loginActivity.setCenter();
    }
    int fragementEmLoginResId = res.getIdentifier("tg_dialog_emlogin", "layout", packageName);
    view = inflater.inflate(fragementEmLoginResId, null);

    int tg_login_btn_id = res.getIdentifier("tg_login_btn", "id", packageName);
    view.findViewById(tg_login_btn_id).setOnClickListener(v -> loginClick());

    int tg_register_btn_id = res.getIdentifier("tvBtnRegister", "id", packageName);
    view.findViewById(tg_register_btn_id).setOnClickListener(v -> changeFragment(AccountRegisterFragment.class));

    int tg_fast_register_btn_id = res.getIdentifier("tg_fast_register", "id", packageName);
    view.findViewById(tg_fast_register_btn_id).setOnClickListener(v -> changeFragment(FastRegisterDialogFragment.class));

    int tg_findpwd_btn_id = res.getIdentifier("tg_findpwd_btn", "id", packageName);
    view.findViewById(tg_findpwd_btn_id).setOnClickListener(v -> changeFragment(RestorePasswordDialogFragment.class));

    int ivBtnCloseResId = res.getIdentifier("ivBtnClose", "id", packageName);
    view.findViewById(ivBtnCloseResId).setOnClickListener(v ->getActivity().finish());

    int tg_account_et_id = res.getIdentifier("tg_account_et", "id", packageName);
    int tg_password_et_id = res.getIdentifier("tg_password_et", "id", packageName);
    accountEditText = (DropEditTextEx) view.findViewById(tg_account_et_id);
    passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);

    passwordEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {
        userEditPassword = true;
      }
    });
    int guest_register_rules_tv_id = res.getIdentifier("guest_register_rules_tv", "id", packageName);
    rulesTextView = view.findViewById(guest_register_rules_tv_id);
    rulesTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
    rulesTextView.setOnClickListener(v -> changeFragment(RulesFragment.class));

    int guest_register_rules_layout_id = res.getIdentifier("guest_register_rules_layout", "id", packageName);
    int guest_register_rules_checkbox_id = res.getIdentifier("guest_register_rules_checkbox", "id", packageName);
    showRulesCheckBox = new CheckBoxWrapper(view.findViewById(guest_register_rules_layout_id), view.findViewById(guest_register_rules_checkbox_id), true);

    initThirdplatform(view);
    initAccountHistory();
    //initLocale(view);

    int tg_debug_text_id = res.getIdentifier("tg_debug_text", "id", getActivity().getPackageName());
    if (Module.of(TGController.class).gameDebug) {
      TextView tv = (TextView) view.findViewById(tg_debug_text_id);
      tv.setText("DebugMode - Version : " + TGController.SDK_VERSION);
      tv.setVisibility(View.VISIBLE);
    } else {
      view.findViewById(tg_debug_text_id).setVisibility(View.GONE);
    }

    return view;
  }

  private void loginClick() {
    final String username = accountEditText.getText().toString();
    final String password = passwordEditText.getText().toString();
    //        if (!userEditPassword) {
    //            if (!validAccount(username)) {
    //                accountEditText.alert();
    //                return;
    //            }
    //            String phone = tg_choose_phone.getText().toString()+" " + username;
    //            HClient.of(UserApi.class).login(phone, realPassword, new TGResultHandler() {
    //
    //                @Override
    //                protected void onSuccess(JSONObject json) throws JSONException {
    //                    Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(realPassword));
    //                    requestExit(false);
    //                }
    //
    //                @Override
    //                protected void onFailed(int errorCode, String msg) {
    //                    Log.e("TAG", "onFailed: " + errorCode + msg );
    //                    showErrorMessage(msg);
    //                }
    //            });
    //        } else {
    if (!validAccount(username)) {
      accountEditText.alert();
      return;
    }
    if (!validPassword(password)) {
      passwordEditText.alert();
      return;
    }

    if (!showRulesCheckBox.isChecked()) {
      Module.of(UIHelper.class).showToast(TGString.please_accept_agreement);
      return;
    }


    String phone = username;
    HClient.of(UserApi.class).login(phone, MD5Util.md5(password), new TGResultHandler() {
      @Override
      protected void onSuccess(JSONObject json) throws JSONException {
        Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password), false);
        requestExit(false);
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        showErrorMessage(msg);
        Log.e("TAG", "onFailed: " + errorCode + msg);
      }
    });
    //        }
  }

  private void initThirdplatform(View v) {
    LinearLayout layout = v.findViewById(R.id.tg_otherlogin);

    List<ThirdPlatform> platforms = null;
    try {
      platforms = Module.of(ThirdPlatformManager.class).getThirdPlatforms();
    } catch (Exception e) {
      HL.e("initThirdplatform getThirdPlatforms Exception " + e.toString());
    }

    if (platforms == null || platforms.size() == 0) {

    } else {
      for (int i = 0; i < platforms.size(); i++) {
        final ThirdPlatform platform = platforms.get(i);
        if (platform.isLoginEnabled()) {
          ImageView imageView = new ImageView(v.getContext());
          imageView.setImageResource(platform.getLoginIcon());
          imageView.setAdjustViewBounds(true);
          LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          param.leftMargin = DpUtil.dp2px(20);
          param.rightMargin = DpUtil.dp2px(20);
          imageView.setLayoutParams(param);
          imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              platform.requestLogin();
            }
          });
          layout.addView(imageView);
        }
        //                else if (platform.isLoginEnabled() && "Facebook".equals(platform.getPlatformName())){
        //                    LoginButton loginButton = new LoginButton(getContext());
        //                    callbackManager = CallbackManager.Factory.create();
        //                    loginButton.setReadPermissions("email");
        //                    // If using in a fragment
        //                    loginButton.setFragment(this);
        //                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //                    param.leftMargin = DpUtil.dp2px(20);
        //                    param.rightMargin = DpUtil.dp2px(20);
        //                    loginButton.setLayoutParams(param);
        //                    // Callback registration
        //                    loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
        //                        @Override
        //                        public void onSuccess(LoginResult loginResult) {
        //                            // App code
        //                            Log.e("TAGff", "onSuccess: " + loginResult );
        //                        }
        //
        //                        @Override
        //                        public void onCancel() {
        //                            // App code
        //                            Log.e("TAGff", "onCancel: "  );
        //                        }
        //
        //                        @Override
        //                        public void onError(FacebookException exception) {
        //                            // App code
        //                            Log.e("TAGff", "FacebookException: " +exception.toString() );
        //                        }
        //                    });
        //
        //                    layout.addView(loginButton);
        //                }
      }
    }

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
      if (User.USERTYPE_NORMAL.equals(user.getUserType()) || User.USERTYPE_GUEST.equals(user.getUserType())) {
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

  @Override
  protected void onBack() {
    //        changeFragment(LoginShowFragment.class);
    //        LoginActivity activity = (LoginActivity) getActivity();
    //        TgLoginDialog loginDialog  = new TgLoginDialog(getContext(),activity);
    //        loginDialog.show();
    return;
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
      Activity activity = getActivity();
      Resources res = activity.getResources();
      String packageName = activity.getPackageName();

      if (convertView == null) {
        int tg_item_account_history_id = res.getIdentifier("tg_item_account_history", "layout", packageName);
        convertView = View.inflate(getActivity(), tg_item_account_history_id, null);
      }
      InternalUser user = userList.get(position);

      int tg_text_id = res.getIdentifier("tg_text", "id", packageName);
      TextView tv = convertView.findViewById(tg_text_id);
      tv.setText(user.getUsername());
      return convertView;
    }

  }

}