package ru.threeguns.engine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import kh.hyper.utils.HL;
import kh.hyper.utils.NotProguard;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.TGApplication;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.manager.ToolbarManager;
import ru.threeguns.engine.pingback.StatisticManager;
import ru.threeguns.event.ActivityPauseEvent;
import ru.threeguns.event.ActivityPermissionResultEvent;
import ru.threeguns.event.ActivityResultEvent;
import ru.threeguns.event.ActivityResumeEvent;
import ru.threeguns.event.ActivityStartEvent;
import ru.threeguns.event.ActivityStopEvent;
import ru.threeguns.manager.ExceptionManager;
import ru.threeguns.manager.PaymentManager;
import ru.threeguns.manager.ShareManager;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.manager.UserManager;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.fragments.RequestPermissionFragment;
import ru.threeguns.utils.ActivityHolder;

/**
 * 3guns平台SDK
 */
public final class TGPlatform implements NotProguard {

  private TGPlatform() {}

  public static final TGPlatform getInstance() {
    return TGPlatformHolder.instance;
  }

  public void init(Context context, InitializeCallback callback) {
    Module.kernel().initialize(context);

    // config request need Activity to show progress dialog
    if (context instanceof Activity) {
      Module.of(ActivityHolder.class).setTopActivity((Activity) context);
    }

    Parameter parameter = new Parameter();
    JSONObject config = parseConfig(context);
    if (config!=null) {
      parameter.put("config", config);
      parameter.put("callback", callback);

      if (Constants.REQUEST_PERMISSION && Build.VERSION.SDK_INT >= 23) {
        int hasPermission = ContextCompat.checkSelfPermission(TGApplication.tgApplication, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasPermission2 = ContextCompat.checkSelfPermission(TGApplication.tgApplication, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission==PackageManager.PERMISSION_GRANTED && hasPermission2==PackageManager.PERMISSION_GRANTED) {
          //已获取权限
          Module.of(TGController.class, parameter);
        } else {
          //未获取权限
          HL.w("on Android 6.0 , try request permission.");
          RequestPermissionFragment.parameterHolder = parameter;
          Intent i = new Intent(context, CommonWebActivity.class);
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          i.putExtra("fragment", RequestPermissionFragment.class);
          context.startActivity(i);
        }
      } else {
        HL.w("not on Android 6.0 , initialize now.");

        // FIX_IT
        TGController.HF_LOAD_FLAG = 0;
        Module.of(TGController.class, parameter);
        if (TGController.HF_LOAD_FLAG==0) {
          // onLoad is not call
          if (Module.of(TGController.class).getContext()!=null) {
            Module.of(TGController.class).loadResources();
          } else {
            Module.kernel().release();
            Module.kernel().initialize(context);
            if (context instanceof Activity) {
              Module.of(ActivityHolder.class).setTopActivity((Activity) context);
            }
            Module.of(TGController.class, parameter);
          }
        }
      }
    } else {
      callback.onInitializeComplete(InitializeCallback.CONFIG_ERROR);
    }
  }

  private JSONObject parseConfig(Context context) {
    JSONObject configJson = null;
    try {
      StringBuilder json = new StringBuilder();
      InputStream is = context.getAssets().open("TGSDK.json");
      byte[] buffer = new byte[1024 * 20];
      int len = -1;
      while ((len = (is.read(buffer)))!=-1) {
        json.append(new String(buffer, 0, len));
      }
      configJson = new JSONObject(json.toString());
    } catch (IOException e) {
      HL.w(e);
    } catch (JSONException e) {
      HL.w(e);
    }
    return configJson;
  }

  /**
   * release方法需在应用onDestroy时调用
   */
  public void release() {
    Module.kernel().release();
  }

  /**
   * onActivityStart需在Activity的onStart方法中调用
   */
  public void onActivityStart(Activity activity) {
    Module.of(ActivityHolder.class).setTopActivity(activity);
    EventManager.instance.dispatch(new ActivityStartEvent());
  }

  /**
   * onActivityStop需在Activity的onStop方法中调用
   */
  public void onActivityStop(Activity activity) {
    EventManager.instance.dispatch(new ActivityStopEvent());
  }

  /**
   * onActivityPause需在Activity的onPause方法中调用
   */
  public void onActivityPause(Activity activity) {
    EventManager.instance.dispatch(new ActivityPauseEvent());
  }

  /**
   * onActivityResume需在Activity的onResume方法中调用
   */
  public void onActivityResume(Activity activity) {
    Module.of(ActivityHolder.class).setTopActivity(activity);
    EventManager.instance.dispatch(new ActivityResumeEvent());
  }

  /**
   * onActivityStop需在Activity的onActivityResult方法中调用
   */
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    // onActivityResult -> onActivityStart -> onActivityResume
    // so if don't set top Activity here, showProgressDialog will get incorrect Activity instance when request network in onActivityResult.
    Module.of(ActivityHolder.class).setTopActivity(activity);
    EventManager.instance.dispatch(new ActivityResultEvent(requestCode, resultCode, data));
  }

  public void onActivityPermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
    EventManager.instance.dispatch(new ActivityPermissionResultEvent(requestCode, permissions, grantResults));
  }

  /**
   * 发送自定义统计事件
   *
   * @param eventName  事件名称
   * @param eventValue 事件值
   * @param paramMap   额外参数Map
   */
  public void sendActionInfo(String eventName, String eventValue, Map<String, String> paramMap) {
    JSONObject json = new JSONObject(paramMap);
    Map<String, String> map = new HashMap<String, String>();
    map.put("event_detail", json.toString());
    Module.of(StatisticManager.class).sendActionInfo(eventName, eventValue, map);
  }

  /**
   * @return UserManager的实例
   */
  public UserManager getUserManager() {
    return Module.of(UserManager.class);
  }

  /**
   * @return ExceptionManager的实例
   */
  public ExceptionManager getExceptionManager() {
    return Module.of(ExceptionManager.class);
  }

  /**
   * @return PaymentManager的实例
   */
  public PaymentManager getPaymentManager() {
    return Module.of(TGController.class).paymentManager;
  }

  /**
   * @return ShareManager的实例
   */
  public ShareManager getShareManager() {
    return Module.of(ShareManager.class);
  }

  /**
   * @return TrackManager的实例
   */
  public TrackManager getTrackManager() {
    return Module.of(TrackManager.class);
  }

  public void createToolbar(Activity activity) {
    Module.of(ToolbarManager.class).createToolbar(activity);
  }

  public void releaseToolbar(Activity activity) {
    Module.of(ToolbarManager.class).releaseToolbar(activity);
  }

  public void requestExit(ExitCallback callback) {
    callback.onExit();
  }

  public static interface ExitCallback extends NotProguard {
    void onExit();

    void onUserCancel();
  }

  public static interface InitializeCallback extends NotProguard {
    int SUCCESS = 0;
    int CONFIG_ERROR = -1;
    int NETWORK_ERROR = -2;
    int UNKNOWN_ERROR = -3;

    void onInitializeComplete(int code);
  }

  private static class TGPlatformHolder {
    private static final TGPlatform instance = new TGPlatform();
  }

}
