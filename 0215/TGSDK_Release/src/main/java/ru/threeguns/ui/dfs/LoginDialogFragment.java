package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.entity.User;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.dialog.TgCountryDialog;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.ui.views.EditTextEx;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.CountryBean;
import ru.threeguns.utils.DpUtil;
import ru.threeguns.utils.LocaleUtil;
import ru.threeguns.utils.TgCountryUtil;

public class LoginDialogFragment extends DFragment {
  public final String[] langs = {"en", "zh", "zh", "id", "ru", "es"};
  //    private static final String PW_BLANK = "4Kf9wQm0";
  private String appLanguage;
  private DropEditTextEx accountEditText;
  private EditTextEx passwordEditText;
  private String realPassword;
  private boolean userEditPassword = false;
  private TextView tg_choose_phone;
  private TextView emlogin;
  private ListView historyListView;
  private PopupWindow accountHistoryPopup;
  private List<InternalUser> userList = new ArrayList<>();
  private boolean firstTimeSelectLocale = true;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = null;
    Activity activity = getActivity();

    if (activity instanceof LoginActivity) {
      LoginActivity loginActivity = (LoginActivity) getActivity();
      loginActivity.setCenter();
    }


    int tg_fragment_login_id = getActivity().getResources().getIdentifier("tg_dialog_loginandregister", "layout", getActivity().getPackageName());
    view = inflater.inflate(tg_fragment_login_id, null);

    int emlogin_id = getActivity().getResources().getIdentifier("emlogin", "id", getActivity().getPackageName());
    view.findViewById(emlogin_id).setOnClickListener(v -> changeFragment(EmLoginDialogFragment.class));


    int tg_register_btn_id = getActivity().getResources().getIdentifier("tvBtnRegister", "id", getActivity().getPackageName());
    view.findViewById(tg_register_btn_id).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        changeFragment(ResgiterPhoneFragment.class);
      }
    });

    int tg_findpwd_btn_id = getActivity().getResources().getIdentifier("tg_findpwd_btn", "id", getActivity().getPackageName());
    view.findViewById(tg_findpwd_btn_id).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        changeFragment(RestorePasswordDialogFragment.class);
      }
    });

    int tg_account_et_id = getActivity().getResources().getIdentifier("tg_account_et", "id", getActivity().getPackageName());
    int tg_password_et_id = getActivity().getResources().getIdentifier("tg_password_et", "id", getActivity().getPackageName());
    accountEditText = (DropEditTextEx) view.findViewById(tg_account_et_id);
    passwordEditText = (EditTextEx) view.findViewById(tg_password_et_id);

    passwordEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        userEditPassword = true;
      }
    });

    int tg_choose_phone_id = getActivity().getResources().getIdentifier("tg_choose_phone", "id", getActivity().getPackageName());
    tg_choose_phone = (TextView) view.findViewById(tg_choose_phone_id);
    tg_choose_phone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        try {
          JSONObject country = new JSONObject(TgCountryUtil.Allcountry);
          JSONObject hotcountry = new JSONObject(TgCountryUtil.hotcountry);
          final JSONArray countrylist = country.getJSONArray("country");
          final JSONArray hotcountrylist = hotcountry.getJSONArray("hotcountry");
          final TgCountryDialog dialog = new TgCountryDialog(getActivity(), countrylist, hotcountrylist);
          dialog.setOnItemClick(new TgCountryDialog.OnItemClick() {
            @Override
            public void Onclick(View v, int pos) {
              try {
                tg_choose_phone.setText(countrylist.getJSONObject(pos).getString("region"));
              } catch (JSONException e) {
                e.printStackTrace();
              }
              dialog.dismiss();
            }
          });

          dialog.setOnhotItemClick(new TgCountryDialog.OnhotItemClick() {
            @Override
            public void Onclick(View v, int pos) {
              try {
                tg_choose_phone.setText(hotcountrylist.getJSONObject(pos).getString("region"));
              } catch (JSONException e) {
                e.printStackTrace();
              }
              dialog.dismiss();
            }
          });

          dialog.setOnsearchItemClick(new TgCountryDialog.OnsearchItemClick() {
            @Override
            public void Onclick(View v, int pos, List<CountryBean> searchlist) {
              tg_choose_phone.setText(searchlist.get(pos).getRegion());
              dialog.dismiss();
            }
          });

          dialog.show();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
    initThirdplatform(view);
    initAccountHistory();
    initLocale(view);

    int tg_debug_text_id = getActivity().getResources().getIdentifier("tg_debug_text", "id", getActivity().getPackageName());
    if (Module.of(TGController.class).gameDebug) {
      TextView tv = (TextView) view.findViewById(tg_debug_text_id);
      tv.setText("DebugMode - Version : " + TGController.SDK_VERSION);
      tv.setVisibility(View.VISIBLE);
    } else {
      view.findViewById(tg_debug_text_id).setVisibility(View.GONE);
    }

    return view;
  }

  private void initThirdplatform(View v) {
    int tg_tplogin_layout_id = getActivity().getResources().getIdentifier("tg_otherlogin", "id", getActivity().getPackageName());
    LinearLayout layout = (LinearLayout) v.findViewById(tg_tplogin_layout_id);

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
      }
    }

  }

  @SuppressWarnings("deprecation")
  private void initAccountHistory() {
    userList = Module.of(UserInfoCache.class).getAllUsers();
    List<InternalUser> tempList = new ArrayList<InternalUser>();
    for (InternalUser u : userList) {
      String userType = u.getUserType();
      if (!User.USERTYPE_PHONE.equals(userType) && !User.USERTYPE_PHONE.equals(userType)) {
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
  //
  //    private void loginClick() {
  //        final String username = accountEditText.getText().toString();
  //        final String password = passwordEditText.getText().toString();
  ////        if (!userEditPassword) {
  ////            if (!validAccount(username)) {
  ////                accountEditText.alert();
  ////                return;
  ////            }
  ////            String phone = tg_choose_phone.getText().toString()+" " + username;
  ////            HClient.of(UserApi.class).login(phone, realPassword, new TGResultHandler() {
  ////
  ////                @Override
  ////                protected void onSuccess(JSONObject json) throws JSONException {
  ////                    Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(realPassword));
  ////                    requestExit(false);
  ////                }
  ////
  ////                @Override
  ////                protected void onFailed(int errorCode, String msg) {
  ////                    Log.e("TAG", "onFailed: " + errorCode + msg );
  ////                    showErrorMessage(msg);
  ////                }
  ////            });
  ////        } else {
  //            if (!validAccount(username)) {
  //                accountEditText.alert();
  //                return;
  //            }
  //            if (!validPassword(password)) {
  //                passwordEditText.alert();
  //                return;
  //            }
  //            String phone = tg_choose_phone.getText().toString() + " " + username;
  //            HClient.of(UserApi.class).login(phone, MD5Util.md5(password), new TGResultHandler() {
  //
  //                @Override
  //                protected void onSuccess(JSONObject json) throws JSONException {
  //                    Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(MD5Util.md5(password)));
  //                    requestExit(false);
  //                }
  //
  //                @Override
  //                protected void onFailed(int errorCode, String msg) {
  //                    showErrorMessage(msg);
  //                    Log.e("TAG", "onFailed: " + errorCode + msg );
  //                }
  //            });
  ////        }
  //    }

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

    String lang = "1";
    if (!TextUtils.isEmpty(lang)) {
      Locale locale = getContext().getResources().getConfiguration().locale;
      appLanguage = locale.getLanguage();
      HL.w(">>>>>>>>>>>>>>>>>>>>>>  Language" + appLanguage);
      for (int i = 1; i <= langs.length; i++) {
        if (appLanguage.startsWith(langs[i - 1])) {
          lang = String.valueOf(i);
          if (langs[i - 1].equals("zh")) {
            HL.w(">>>>>>>>>>>>>>>>>>>>>>   Country" + locale.getCountry());
            if (locale.getCountry().indexOf("cn") == 0 || locale.getCountry().indexOf("CN") == 0) {
              lang = "2";
            } else {
              lang = "3";
            }
            break;
          }
        }
      }
      if (TextUtils.isEmpty(lang)) {
        lang = "1";
      }
    } else {
      lang = "2";
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
      if (User.USERTYPE_PHONE.equals(user.getUserType()) || User.USERTYPE_PHONE.equals(user.getUserType())) {
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
    //        requestBack();
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