package ru.threeguns.ui;

import android.os.Bundle;
import android.util.Log;

import ru.threeguns.ui.dfs.EmLoginDialogFragment;
import ru.threeguns.ui.dfs.FastRegisterDialogFragment;
import ru.threeguns.ui.dfs.GuestDialogFragment;
import ru.threeguns.ui.dfs.RegisterDialogFragment;
import ru.threeguns.ui.dfs.RestorePasswordDialogFragment;
import ru.threeguns.ui.dialog.TgLoginDialog;

public class LoginActivity extends TGFragmentActivity {
  private TgLoginDialog dialog;
  private String TAG = "LoginActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //preloadFragment(LoginDialogFragment.class);
    preloadFragment(RegisterDialogFragment.class);
    preloadFragment(FastRegisterDialogFragment.class);
    preloadFragment(RestorePasswordDialogFragment.class);
    preloadFragment(GuestDialogFragment.class);

    preloadFragment(EmLoginDialogFragment.class);
    changeFragment(EmLoginDialogFragment.class);
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Log.e(TAG, "onRestart: ");
  }

  @Override
  protected void initView() {
    int activityLoginResId = this.getResources().getIdentifier("tg_activity_login", "layout", this.getPackageName());
    setContentView(activityLoginResId);
    fragmentContainerId = this.getResources().getIdentifier("tg_fragment_container", "id", this.getPackageName());
    containerView = findViewById(fragmentContainerId);
    setBottom();
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.e("TAG", "onResume: ");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.e(TAG, "onStart: ");
  }

  public void setBottom() {
    //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //params.gravity = Gravity.BOTTOM;
    //containerView.setLayoutParams(params);
  }


  public void setCenter() {
    //		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //		params.gravity = CENTER;
    //		params.leftMargin = 40;
    //		params.rightMargin = 40;
    //		containerView.setLayoutParams(params);
    //		containerView = findViewById(fragmentContainerId);
  }

}

