package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.views.CheckBoxWrapper;
import ru.threeguns.ui.views.DropEditTextEx;
import ru.threeguns.utils.MD5Util;

public class FastRegisterDialogFragment extends DFragment {
  private static final int TP_ICON_SIZE = 40;
  private static final int TP_ICON_PADDING = 8;
  private final List<InternalUser> userList = new ArrayList<>();
  private DropEditTextEx accountEditText;
  private DropEditTextEx passwordEditText;
  private CheckBoxWrapper showRulesCheckBox;
  private ListView historyListView;
  private PopupWindow accountHistoryPopup;
  private String realPassword;
  private boolean userEditPassword = false;
  private TextView rulesTextView;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view;

    Activity activity = getActivity();
    Resources res = getActivity().getResources();
    String packageName = activity.getPackageName();

    if (activity instanceof LoginActivity) {
      LoginActivity loginActivity = (LoginActivity) activity;
      loginActivity.setCenter();
    }


    int tg_fragment_fast_register_id = res.getIdentifier("tg_fragment_fast_dialog", "layout", packageName);
    view = inflater.inflate(tg_fragment_fast_register_id, null);

    int tg_account_et_id = res.getIdentifier("tg_account_et", "id", packageName);
    int tg_password_et_id = res.getIdentifier("tg_password_et", "id", packageName);
    accountEditText = view.findViewById(tg_account_et_id);
    passwordEditText = view.findViewById(tg_password_et_id);

    //确认登录并保存截图
    int tg_confirm_btn_id = res.getIdentifier("tg_confirm_btn", "id", packageName);
    view.findViewById(tg_confirm_btn_id).setOnClickListener(v -> loginClick());

    //跳转到注册
    int tg_register_btn_id = res.getIdentifier("tvBtnRegister", "id", packageName);
    view.findViewById(tg_register_btn_id).setOnClickListener(v -> {
      requestBack();
      changeFragment(AccountRegisterFragment.class);
    });


    //跳转到已有账号登录
    int tg_to_login_btn_id = res.getIdentifier("tg_to_login_btn", "id", packageName);
    view.findViewById(tg_to_login_btn_id).setOnClickListener(v -> {
      requestBack();
      changeFragment(EmLoginDialogFragment.class);
    });

    //后退
    int tg_back_id = res.getIdentifier("tg_back", "id", packageName);
    view.findViewById(tg_back_id).setOnClickListener(v -> requestBack());

    passwordEditText.hideDropButton();
    accountEditText.hideDropButton();

    int guest_register_rules_tv_id = res.getIdentifier("guest_register_rules_tv", "id", packageName);
    rulesTextView = view.findViewById(guest_register_rules_tv_id);
    rulesTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
    rulesTextView.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        changeFragment(RulesFragment.class);
      }
    });
    int guest_register_rules_layout_id = res.getIdentifier("guest_register_rules_layout", "id", packageName);
    int guest_register_rules_checkbox_id = res.getIdentifier("guest_register_rules_checkbox", "id", packageName);
    showRulesCheckBox = new CheckBoxWrapper(view.findViewById(guest_register_rules_layout_id),
        (ImageView) view.findViewById(guest_register_rules_checkbox_id), true);

    return view;
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
    if (!showRulesCheckBox.isChecked()) {
      Module.of(UIHelper.class).showToast(TGString.please_accept_agreement);
      return;
    }

    HClient.of(UserApi.class).fastRegister(username, MD5Util.md5(password), new TGResultHandler() {
      @Override
      protected void onSuccess(JSONObject json) throws JSONException {
        takeScreenShot(username);
        Module.of(UserCenter.class).notifyRegisterSuccess(username, MD5Util.md5(password), User.USERTYPE_GUEST);
        Module.of(SPCache.class).save("request_bind", "holder");
        Log.e("TAGfff", "onSuccess: kuaisucg");
        HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {

          @Override
          protected void onSuccess(JSONObject json) throws JSONException {
            Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password), true);
            requestExit(false);
            Log.e("TAGfff", "onSuccess: dengl");
          }

          @Override
          protected void onFailed(int errorCode, String msg) {
            showErrorMessage(msg);
          }
        });
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        Log.e("TAGfff", "onSuccess: kuaisusb");
        if (30007 == errorCode) {
          showErrorMessage(msg);
          return;
        }

        Log.e("TAG", "onFailed: " + errorCode);
        HClient.of(UserApi.class).login(username, MD5Util.md5(password), new TGResultHandler() {

          @Override
          protected void onSuccess(JSONObject json) throws JSONException {
            Module.of(UserCenter.class).notifyUserInfo(InternalUser.parseFromJSON(json).setPassword(password), false);
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

  private void takeScreenShot(final String username) {
    final View view = getView().getRootView();
    view.post(new Runnable() {
      public void run() {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap bitmap = view.getDrawingCache();
        Context context = getContext();

        if (bitmap != null) {
          String fileName = "account_" + username + ".png";
          String mimeType = "image/png";

          try {
            File folderFile;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
              String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/3guns";
              folderFile = new File(folderPath);
              if (!folderFile.exists()) {
                folderFile.mkdirs();
              }
              File file = new File(folderPath+"/" + fileName);
              FileOutputStream out = new FileOutputStream(file);
              if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
              {
                out.flush();
                out.close();
              }

              MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{mimeType}, (p, uri) -> {});
              //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), bitName, null);
            } else {
              ContentValues values = new ContentValues();
              values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
              values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
              values.put(MediaStore.Images.Media.TITLE, fileName);
              values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

              ContentResolver contentResolver = context.getContentResolver();
              Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
              OutputStream out = contentResolver.openOutputStream(uri);
              //FileOutputStream out = new FileOutputStream(path);
              bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
              out.close();
            }

            view.destroyDrawingCache();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
  }


  @Override
  public void onEnter() {
    super.onEnter();
    accountEditText.setText(randomStr());
    passwordEditText.setText(randomStr());
    accountEditText.setKeyListener(null);
    passwordEditText.setKeyListener(null);
    userEditPassword = false;

    //        if (userList.size() > 0) {
    //            if (userList.size() == 1) {
    //                accountEditText.hideDropButton();
    //            } else {
    //                accountEditText.setOnDropClickListener(new View.OnClickListener() {
    //                    @Override
    //                    public void onClick(View v) {
    //                        if (accountEditText.isDrop()) {
    //                            if (!accountHistoryPopup.isShowing()) {
    //                                Module.of(UIHelper.class).post2MainThread(new Runnable() {
    //                                    public void run() {
    //                                        accountHistoryPopup.showAsDropDown(accountEditText, 0, 5);
    //                                    }
    //                                });
    //                            }
    //                        } else {
    //                            // if (accountHistoryPopup.isShowing()) {
    //                            // accountHistoryPopup.dismiss();
    //                            // }
    //                        }
    //                    }
    //                });
    //            }
    //        } else {
    //            accountEditText.hideDropButton();
    //        }
    //
    //        if (userList.size() > 0) {
    //            InternalUser user = userList.get(0);
    //            if (User.USERTYPE_GUEST.equals(user.getUserType()) || User.USERTYPE_GUEST.equals(user.getUserType())) {
    //                final String username = user.getUsername();
    //                final String password = user.getPassword();
    //                realPassword = user.getPassword();
    //
    //                Module.of(UIHelper.class).post2MainThread(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        accountEditText.setText(username);
    //                        if (!TextUtils.isEmpty(password)) {
    //                            passwordEditText.setText(password);
    //                        } else {
    //                            passwordEditText.setText("");
    //                        }
    //                        userEditPassword = false;
    //                    }
    //                });
    //            }
    //        }
  }

  private String randomStr() {
    String src = new Random().nextFloat() + "" + System.currentTimeMillis();
    return MD5Util.md5(src).substring(0, 8);
  }

}