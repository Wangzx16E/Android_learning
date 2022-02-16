package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import kh.hyper.utils.HUI;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.manager.UserInfoCache;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.entity.User;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.ui.views.EditTextEx;

public class LoginFragment extends DFragment {
  private static final String PW_BLANK = "4Kf9wQm0";
  private static final int TP_ICON_SIZE = 40;
  private static final int TP_ICON_PADDING = 8;


  private DropEditTextEx accountEditText;
  private EditTextEx passwordEditText;
  private String realPassword;
  private boolean userEditPassword = false;

  private ListView historyListView;
  private PopupWindow accountHistoryPopup;
  private List<InternalUser> userList;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = null;

    Activity activity = getActivity();
    Resources res = activity.getResources();
    String packageName = activity.getPackageName();

    int tg_fragment_login_id = res.getIdentifier("tg_fragment_login", "layout", packageName);
    view = inflater.inflate(tg_fragment_login_id, null);

    if (activity instanceof LoginActivity) {
      LoginActivity loginActivity = (LoginActivity) activity;
      loginActivity.setCenter();
    }
    int tg_back_btn_id = res.getIdentifier("tg_back_btn", "id", packageName);
    view.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requestExit(true);
      }
    });

    int tg_login_btn_id = res.getIdentifier("tg_login_btn", "id", packageName);
    view.findViewById(tg_login_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loginClick();
      }
    });

    int tg_fastregister_btn_id = res.getIdentifier("tg_fastregister_btn", "id", packageName);
    view.findViewById(tg_fastregister_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeFragment(FastRegisterFragment.class);
      }
    });

    int tg_register_btn_id = getActivity().getResources().getIdentifier("tvBtnRegister", "id", packageName);
    view.findViewById(tg_register_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeFragment(RegisterFragment.class);
      }
    });

    int tg_findpwd_btn_id = getActivity().getResources().getIdentifier("tg_findpwd_btn", "id", packageName);
    view.findViewById(tg_findpwd_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeFragment(RestorePasswordFragment.class);
      }
    });

    int tg_account_et_id = res.getIdentifier("tg_account_et", "id", packageName);
    int tg_password_et_id = res.getIdentifier("tg_password_et", "id", packageName);
    accountEditText = view.findViewById(tg_account_et_id);
    passwordEditText = view.findViewById(tg_password_et_id);

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

    initThirdplatform(view);
    initAccountHistory();

    int tg_debug_text_id = getActivity().getResources().getIdentifier("tg_debug_text", "id", packageName);
    if (Module.of(TGController.class).gameDebug) {
      TextView tv = view.findViewById(tg_debug_text_id);
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
    if (!userEditPassword) {
      if (!validAccount(username)) {
        accountEditText.alert();
        return;
      }
      //			HClient.of(UserApi.class).login(username, realPassword, new TGResultHandler() {
      //
      //				@Override
      //				protected void onSuccess(JSONObject json) throws JSONException {
      //					Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(realPassword));
      //					requestExit(false);
      //				}
      //
      //				@Override
      //				protected void onFailed(int errorCode, String msg) {
      //					showErrorMessage(msg);
      //				}
      //			});
    } else {
      if (!validAccount(username)) {
        accountEditText.alert();
        return;
      }
      if (!validPassword(password)) {
        passwordEditText.alert();
        return;
      }

      //			HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {
      //
      //				@Override
      //				protected void onSuccess(JSONObject json) throws JSONException {
      //					Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(MD5Util.md5(password)));
      //					requestExit(false);
      //				}
      //
      //				@Override
      //				protected void onFailed(int errorCode, String msg) {
      //					showErrorMessage(msg);
      //				}
      //			});
    }
  }

  private void initThirdplatform(View v) {
    int tg_tplogin_layout_id = getActivity().getResources().getIdentifier("tg_tplogin_layout", "id", getActivity().getPackageName());
    LinearLayout layout = v.findViewById(tg_tplogin_layout_id);

    List<ThirdPlatform> platforms = null;
    try {
      platforms = Module.of(ThirdPlatformManager.class).getThirdPlatforms();
    } catch (Exception e) {
      HL.e("initThirdplatform getThirdPlatforms Exception " + e.toString());
    }

    int tg_tplogin_divider_id = getActivity().getResources().getIdentifier("tg_tplogin_divider", "id", getActivity().getPackageName());
    int tg_tplogin_container_id = getActivity().getResources().getIdentifier("tg_tplogin_container", "id", getActivity().getPackageName());
    if (platforms == null || platforms.size() == 0) {
      v.findViewById(tg_tplogin_divider_id).setVisibility(View.GONE);
      v.findViewById(tg_tplogin_container_id).setVisibility(View.GONE);
    } else {
      v.findViewById(tg_tplogin_divider_id).setVisibility(View.VISIBLE);
      v.findViewById(tg_tplogin_container_id).setVisibility(View.VISIBLE);
      for (int i = 0; i < platforms.size(); i++) {
        final ThirdPlatform platform = platforms.get(i);
        if (platform.isLoginEnabled()) {
          ImageView imageView = new ImageView(v.getContext());
          imageView.setImageResource(platform.getIconResource());
          int size = HUI.dp2px(getActivity(), TP_ICON_SIZE);
          LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(size, size);
          param.leftMargin = HUI.dp2px(getActivity(), TP_ICON_PADDING);
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
      if (!User.USERTYPE_GUEST.equals(userType) && !User.USERTYPE_NORMAL.equals(userType)) {
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
    historyListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (accountHistoryPopup.isShowing()) {
          accountHistoryPopup.dismiss();
        }

        final InternalUser user = userList.get(position);
        realPassword = user.getPassword();
        Module.of(UIHelper.class).post2MainThread(() -> {
          accountEditText.setText(user.getUsername());

          if (!TextUtils.isEmpty(user.getPassword())) {
            passwordEditText.setText(PW_BLANK);
          } else {
            passwordEditText.setText("");
          }

          userEditPassword = false;
        });
      }

    });
    accountHistoryPopup = new PopupWindow(historyListView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    int tg_et_container_bg_id = getActivity().getResources().getIdentifier("tg_et_container_bg", "drawable", getActivity().getPackageName());
    accountHistoryPopup.setBackgroundDrawable(getResources().getDrawable(tg_et_container_bg_id));
    accountHistoryPopup.setOutsideTouchable(true);
    accountHistoryPopup.setFocusable(true);
    accountHistoryPopup.setOnDismissListener(() -> accountEditText.packupDropButton());
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
      if (User.USERTYPE_GUEST.equals(user.getUserType()) || User.USERTYPE_NORMAL.equals(user.getUserType())) {
        final String username = user.getUsername();
        final String password = user.getPassword();
        realPassword = user.getPassword();

        Module.of(UIHelper.class).post2MainThread(new Runnable() {
          @Override
          public void run() {
            accountEditText.setText(username);
            if (!TextUtils.isEmpty(password)) {
              passwordEditText.setText(PW_BLANK);
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
    requestExit(true);
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
      TextView tv = convertView.findViewById(tg_text_id);
      tv.setText(user.getUsername());
      return convertView;
    }

  }

}
