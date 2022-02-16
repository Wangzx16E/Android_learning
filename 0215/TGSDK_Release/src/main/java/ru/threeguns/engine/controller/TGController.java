package ru.threeguns.engine.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.leon.channel.helper.ChannelReaderUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.Handle;
import kh.hyper.event.HandleThreadType;
import kh.hyper.network.HClient;
import kh.hyper.network.StringController;
import kh.hyper.utils.HL;
import ru.threeguns.engine.TGPlatform.InitializeCallback;
import ru.threeguns.engine.manager.AbstractPaymentManager;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.engine.manager.ToolbarManager;
import ru.threeguns.engine.pingback.StatisticManager;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.entity.User;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.manager.PaymentManager;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.manager.TrackManager.TrackEvent;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.ConfigApi;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.LocaleUtil;

public final class TGController extends Module {
  public static final String SDK_VERSION = "1.5.6";
  public static int HF_LOAD_FLAG = 0;
  private static boolean READ_SERVER_CONFIG = false;
  public final String[] langs = {"en", "zh", "zh", "id", "ru", "es"};
  public JSONObject configJson;
  public boolean gameDebug;
  public String appId;
  public String appKey;
  public String channelId;
  public String appLanguage;
  public String packageName;
  public int screenOrientation;
  public String serverLanguage;
  public PaymentManager paymentManager;
  public boolean isAutoLogin = false;

  @Override
  protected void onLoad(Parameter parameter) {
    HF_LOAD_FLAG = 1;
    configJson = parameter.opt("config", JSONObject.class);
    //		Log.e("TAG", "onLoad: " + configJson );
    //		Log.e("TAG", "onLoad: " + parameter );
    JSONObject runConfig = configJson.optJSONObject("run_config");

    gameDebug = Boolean.parseBoolean(runConfig.optString("debug"));

    HL.LOG_LEVEL = gameDebug ? HL.LOG_LEVEL_INFO : HL.LOG_LEVEL_WARNING;
    if (Constants.DEBUG) {
      HL.LOG_LEVEL = HL.LOG_LEVEL_INFO;
    }

    READ_SERVER_CONFIG = Boolean.parseBoolean(runConfig.optString("server_config", "false"));

    Module.of(StringController.class).save("TG_HOST_ADDRESS",
        Constants.DEBUG ? Constants.DEBUG_HOST_ADDRESS : gameDebug ? Constants.GAMEDEBUG_HOST_ADDRESS : Constants.HOST_ADDRESS);

    appId = runConfig.optString("app_id");
    appKey = runConfig.optString("app_key");
    channelId = runConfig.optString("channel_id");
    String channel = ChannelReaderUtil.getChannel(context);
    if (channel != null && channel.length() != 0) {
      channelId = channel;
    }

    String so = runConfig.optString("screen_orientation");
    if ("landscape".equalsIgnoreCase(so) || "land".equalsIgnoreCase(so)) {
      screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    } else if ("portrait".equalsIgnoreCase(so) || "port".equalsIgnoreCase(so)) {
      screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    } else {
      screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    //		1:英文       2:中文       3:繁体中文       4:印尼文        5:俄罗斯        6:西班牙
    String lang = "1";
    if (TextUtils.isEmpty(lang)) {
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
    }

    appLanguage = lang;
    if (Constants.ENABLE_CUSTOM_LOCALE) {
      String locale = Module.of(SPCache.class).load(Constants.SPK_LOCALE);
      if (!TextUtils.isEmpty(locale)) {
        Locale l = LocaleUtil.fromString(locale);
        Resources res = getContext().getResources();
        Configuration configuration = res.getConfiguration();
        configuration.locale = l;
        res.updateConfiguration(configuration, res.getDisplayMetrics());
      }
    }

    packageName = runConfig.optString("packageName");//packageName = getContext().getPackageName();

    loadResources();

    // init TGString here , cause the config request need progress string.
    Module.of(TGString.class);
    Module.of(ToolbarManager.class);

    fetchConfig(parameter, runConfig);
  }

  @Override
  protected void onRelease() {
    paymentManager = null;
  }

  protected void checkUpdate() {
    int currentVersion = 0;
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      currentVersion = info.versionCode;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }


    HClient.of(ConfigApi.class).checkUpdate(currentVersion + "", new TGResultHandler() {
      @Override
      protected void onSuccess(JSONObject json) throws JSONException {
        // fetchConfig(parameter, runConfig);
      }

      @Override
      protected void onFailed(int errorCode, String msg) {
        // ignore
      }

      @Override
      protected void onFailedExtra(int errorCode, String msg, JSONObject json) {
        if (errorCode == 80001) {
          final String updateUrl = json.optString("update_url");
          final String updateDesc = json.optString("update_desc");
          Module.of(UIHelper.class).post2MainThread(new Runnable() {
            public void run() {
              AlertDialog.Builder builder = new AlertDialog.Builder(Module.of(ActivityHolder.class).getTopActivity());
              builder.setTitle(TGString.checkupdate_title).setMessage(updateDesc).setPositiveButton(TGString.checkupdate_btn, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  Intent intent = new Intent();
                  intent.setAction("android.intent.action.VIEW");
                  Uri uri = Uri.parse(updateUrl);
                  intent.setData(uri);
                  Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);

                  System.exit(0);
                  android.os.Process.killProcess(android.os.Process.myPid());
                }
              }).setCancelable(false).create().show();
            }
          });
        } else {
          Module.of(UIHelper.class).showToast("Initialize Failed - (check_update failed)");
        }
      }
    });
  }

  protected void fetchConfig(final Parameter parameter, final JSONObject runConfig) {
    final InitializeCallback callback = parameter.opt("callback", InitializeCallback.class);

    if (READ_SERVER_CONFIG) {
      HClient.of(ConfigApi.class).config(new TGResultHandler() {
        @Override
        protected void onSuccess(JSONObject json) throws JSONException {
          configJson = json;
          String paymentImpl = json.getJSONObject("billing").getString("method");
          initPaymentManager(paymentImpl);
          Module.of(ThirdPlatformManager.class);
          callback.onInitializeComplete(InitializeCallback.SUCCESS);

          if (json.optBoolean("checkupdate")) {
            checkUpdate();
          }

          registerBindDialog();
          initTinker(json);
          initLogger(json);
          initLocale(json);

          sendOpenAction();
        }

        @Override
        protected void onFailed(int errorCode, String msg) {
          Log.e("TGCtroller", "onFailed: " + errorCode + "  " + msg);
          Module.of(UIHelper.class).showToast("Initialize Failed - (fetch_config failed)");
          initPaymentManager(null);
          callback.onInitializeComplete(InitializeCallback.NETWORK_ERROR);

          sendOpenAction();

          //					killGame();
          READ_SERVER_CONFIG = false;
          fetchConfig(parameter, runConfig);
        }
      });
    } else {
      String billingModule = "Google";//runConfig.optString("billing_module");

      initPaymentManager(billingModule);
      Module.of(ThirdPlatformManager.class);
      callback.onInitializeComplete(InitializeCallback.SUCCESS);

      registerBindDialog();

      if (runConfig.optBoolean("checkupdate")) {
        checkUpdate();
      }

      sendOpenAction();
    }

  }

  protected void registerBindDialog() {
    if (configJson.optBoolean("request_bind", false)) {
      new TGHandler() {
        @Handle(HandleThreadType.MAIN)
        public void onLogin(UserLoginEvent e) {
          if (e.getResult() == UserLoginEvent.SUCCESS && !User.USERTYPE_NORMAL.equals(e.getUser().getUserType())) {
            if (Module.of(SPCache.class).load("request_bind") != null) {
              Module.of(SPCache.class).save("request_bind", null);
            } else {
              Module.of(UIHelper.class).post2MainThread(new Runnable() {
                public void run() {
                  Activity act = Module.of(ActivityHolder.class).getTopActivity();
                  Intent t = new Intent(act, LoginActivity.class);
                  t.putExtra("guest_upgrade", true);
                  act.startActivity(t);
                }
              });
            }
          }
        }
      }.register();
    }
  }

  protected void initPaymentManager(String paymentImpl) {
    if (Constants.DEBUG) {
      paymentImpl = Constants.DEBUG_PAYMENT_MANAGER;
    }

    if (TextUtils.isEmpty(paymentImpl)) {
      paymentImpl = "Google";
    }

    // TODO: 2021/10/10 temp test
    //paymentImpl = "Alphapay";
    try {
      Class<?> clazz = Class.forName("ru.threeguns.engine.billing." + paymentImpl + "PaymentManagerImpl");
      paymentManager = (PaymentManager) clazz.newInstance();
    } catch (ClassNotFoundException e) {
      HL.w(e);
      HL.w("Cannot find billing module : " + paymentImpl);
    } catch (InstantiationException e) {
      HL.w(e);
    } catch (IllegalAccessException e) {
      HL.w(e);
    }
    if (paymentManager != null) {
      try {
        load((AbstractPaymentManager) paymentManager, Parameter.parseFromJSON(configJson.optJSONObject("billing")));
        Log.e("TAG", "initPaymentManager: " + configJson.optJSONObject("billing"));
      } catch (Exception e) {
        HL.w(">>>读取billing异常");
        e.printStackTrace();
        killGame();
      }
    } else {
      HL.w("PaymentManager Init Failed.");
    }
  }

  private void killGame() {
    Module.of(UIHelper.class).post2MainThread(new Runnable() {

      @Override
      public void run() {
        int tg_network_not_available_id = getContext().getResources().getIdentifier("tg_network_not_available", "string", getContext().getPackageName());
        Module.of(UIHelper.class).showToast(getContext().getResources().getString(tg_network_not_available_id));
        //				Module.of(UIHelper.class).post2MainThreadDelayed(new Runnable() {
        //
        //					@Override
        //					public void run() {
        //
        //						onRelease();
        //						TGController.kernel().release();
        //						System.exit(0);
        //						Process.killProcess(Process.myPid());
        //					}
        //				}, 2000);
      }
    });
  }

  protected void initTinker(JSONObject json) throws JSONException {
    //		boolean tinker = json.optBoolean("tinker");
    //		if (tinker) {
    //			try {
    //				Class<?> clazz = Class.forName("ru.threeguns.engine.manager.HotfixManager");
    //				Object manager = Module.of(clazz);
    //				Method method = clazz.getMethod("requestHotfix");
    //				method.invoke(manager, new Object[] {});
    //			} catch (ClassNotFoundException e) {
    //				e.printStackTrace();
    //			} catch (NoSuchMethodException e) {
    //				e.printStackTrace();
    //			} catch (IllegalAccessException e) {
    //				e.printStackTrace();
    //			} catch (IllegalArgumentException e) {
    //				e.printStackTrace();
    //			} catch (InvocationTargetException e) {
    //				e.printStackTrace();
    //			}
    //			// Module.of(Class.forName("ru.threeguns.engine.manager.HotfixManager")).requestHotfix();
    //		}
  }

  protected void initLogger(JSONObject json) throws JSONException {
    JSONObject logger = json.optJSONObject("logger");
    if (logger != null) {
      String filter = logger.optString("filter");
      if ("all".equals(filter)) {
        HL.LOG_LEVEL = HL.LOG_LEVEL_INFO;
      } else if ("udid".equals(filter)) {
        String udid = Module.of(SystemInfo.class).deviceNo;
        if (udid.equals(json.optString("data"))) {
          HL.LOG_LEVEL = HL.LOG_LEVEL_INFO;
        }
      }
    }
  }

  protected void initLocale(JSONObject json) throws JSONException {
    serverLanguage = json.optString("language");
    if (!TextUtils.isEmpty(serverLanguage)) {
      Locale l = LocaleUtil.fromString(serverLanguage);
      Resources res = getContext().getResources();
      Configuration config = res.getConfiguration();
      config.locale = l;
      res.updateConfiguration(config, res.getDisplayMetrics());

      Module.of(TGString.class).refreshString();
      Module.of(ToolbarManager.class).reloadToolbar();
    }
  }

  public void loadResources() {
    long c = System.currentTimeMillis();
    Resources res = getContext().getResources();
    String packName = getContext().getPackageName();
    Class<?> internalRClass;
    try {
      internalRClass = Class.forName("ru.threeguns.internal.R");
    } catch (ClassNotFoundException e) {
      HL.w("Cannot find internal R class , skip loadResources.");
      return;
    }
    Class<?>[] clazzArr = internalRClass.getDeclaredClasses();
    for (Class<?> clazz : clazzArr) {
      String type = clazz.getSimpleName();
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (!"styleable".equals(type)) {
          int value = res.getIdentifier(field.getName(), type, packName);
          try {
            field.set(null, value);
          } catch (IllegalAccessException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalAccessException : ");
            HL.w(e);
          } catch (IllegalArgumentException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalArgumentException : ");
            HL.w(e);
          }
        } else {
          if (Modifier.isFinal(field.getModifiers())) {
            continue;
          }
          try {
            String rClassName = getContext().getPackageName() + ".R$" + type;
            Class<?> rClass = Class.forName(rClassName);
            Field f = rClass.getField(field.getName());
            Object value = f.get(null);
            field.set(null, value);
          } catch (IllegalAccessException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalAccessException : ");
            HL.w(e);
          } catch (IllegalArgumentException e) {
            HL.w("init res '" + field.getName() + "' throws IllegalArgumentException : ");
            HL.w(e);
          } catch (ClassNotFoundException e) {
            HL.w("init res '" + field.getName() + "' throws ClassNotFoundException : ");
            HL.w(e);
          } catch (NoSuchFieldException e) {
            HL.w("init res '" + field.getName() + "' throws NoSuchFieldException : ");
            HL.w(e);
          }
        }
      }
    }
    c = System.currentTimeMillis() - c;
    HL.w("loadResources cost : " + c + "ms");
  }

  public void sendOpenAction() {
    new Thread() {
      public void run() {
        Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent(TrackManager.OPEN));
        Module.of(StatisticManager.class).sendActionInfo(StatisticManager.OPEN);

        // read cached referrer
        SharedPreferences sp = context.getSharedPreferences(Constants.SPN_REFERRER, Context.MODE_PRIVATE);
        String referrer = sp.getString(Constants.SPK_REFERRER, null);
        // referrer =
        // "utm_source%3DFB%26utm_medium%3Dcpc%26utm_content%3Dtest_content%26utm_campaign%3Dtest_name%26anid%3Dadmob";
        if (!TextUtils.isEmpty(referrer)) {
          Module.of(TrackManager.class).trackEvent(new TrackEvent(TrackManager.ANDROID_REFERRER).setExtraParams("referrer", referrer));
          sp.edit().putString(Constants.SPK_REFERRER, null).commit();
        }

      }
    }.start();
  }
}
