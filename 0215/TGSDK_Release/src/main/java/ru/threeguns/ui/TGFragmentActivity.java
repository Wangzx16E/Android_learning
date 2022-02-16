package ru.threeguns.ui;

import android.annotation.TargetApi;
import android.content.Intent;

import kh.hyper.ui.HFragmentActivity;
import ru.threeguns.engine.TGPlatform;

public class TGFragmentActivity extends HFragmentActivity {

  @Override
  protected void initView() {
    int activityHfragmentId = this.getResources().getIdentifier("tg_activity_hfragment", "layout", this.getPackageName());
    setContentView(activityHfragmentId);
    fragmentContainerId = this.getResources().getIdentifier("tg_hfragmentact_container", "id", this.getPackageName());
    containerView = findViewById(fragmentContainerId);
  }

  protected void onExit() {
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    TGPlatform.getInstance().onActivityResult(this, requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onPause() {
    TGPlatform.getInstance().onActivityPause(this);
    super.onPause();
  }

  @Override
  protected void onResume() {
    TGPlatform.getInstance().onActivityResume(this);
    super.onResume();
  }

  @Override
  protected void onStart() {
    TGPlatform.getInstance().onActivityStart(this);
    super.onStart();
  }

  @Override
  protected void onStop() {
    TGPlatform.getInstance().onActivityStop(this);
    super.onStop();
  }

  @TargetApi(23)
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    TGPlatform.getInstance().onActivityPermissionResult(this, requestCode, permissions, grantResults);
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

}
