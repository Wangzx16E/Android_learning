package ru.threeguns.engine.controller;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import kh.hyper.utils.NotProguard;
import ru.threeguns.utils.MD5Util;

public class SystemInfo extends Module implements NotProguard {

  private static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
  private static final Uri ATTRIBUTION_ID_CONTENT_URI = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
  public String app_version;
  public String os_name;
  public String os_version;
  public String os_lang;
  public String ip;
  public String mac;
  public String connection_type;
  public String screen_resolution;
  public String mobile_operator = "none";
  public String carrier = "none";
  public String brand;
  public String manufacturer;
  public String model;
  public String imei = "none";
  public String imsi = "none";
  public String android_id;
  public String device_serial;
  public String country = "none";
  public String deviceNo;
  public String deviceType;

  @Override
  protected void onLoad(Parameter parameter) {
    init(getContext());
  }

  @Override
  protected void onRelease() {}

  public Map<String, String> getSimpleSystemInfo() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("appid", Module.of(TGController.class).appId);
    map.put("deviceNo", deviceNo);
    map.put("deviceType", "Android");
    map.put("connection_type", getConnectionType());
    return map;
  }

  public Map<String, String> getTotalSystemInfo() {
    Map<String, String> map = new HashMap<String, String>();
    try {
      for (Field field : this.getClass().getFields()) {
        Object obj = field.get(this);
        if (obj instanceof String) {
          String value = (String) obj;
          map.put(field.getName(), value==null ? "":value);
        }
      }
      map.put("appid", Module.of(TGController.class).appId);
      map.put("facebook_aid", getFacebookAttributionId());
    } catch (Exception e) {
      HL.w("SystemInfo", "Make SystemInfoMap Failed.");
      HL.w(e);
    }
    return map;
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("deprecation")
  private void init(Context context) {
    //		TelephonyManager tm = (TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE));
    WifiManager wifiManager = (WifiManager) (context.getSystemService(Context.WIFI_SERVICE));
    NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    DisplayMetrics dm = new DisplayMetrics();
    WindowManager windowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
    windowManager.getDefaultDisplay().getMetrics(dm);

    os_name = "Android";
    os_version = Build.VERSION.RELEASE;
    os_lang = Locale.getDefault().getLanguage();
    ip = getLocalIpAddress();
    mac = wifiManager.getConnectionInfo().getMacAddress();
    connection_type = networkInfo==null ? "NO_NETWORK":networkInfo.getTypeName();
    screen_resolution = windowManager.getDefaultDisplay().getHeight() + "x" + windowManager.getDefaultDisplay().getWidth() + "x" + dm.densityDpi;
    //		mobile_operator = tm.getSimOperatorName();
    //		carrier = tm.getNetworkOperatorName();
    brand = Build.BRAND;
    manufacturer = Build.MANUFACTURER;
    model = Build.MODEL;
    //		imei = tm.getDeviceId();
    //		imsi = tm.getSubscriberId();
    android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    device_serial = Build.VERSION.SDK_INT >= 9 ? Build.SERIAL:"NO_SERIAL";
    //		country = tm.getSimCountryIso();
    deviceNo = generateUDID();
    deviceType = "Android";

    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      app_version = packageInfo.versionName + "_" + packageInfo.versionCode;
    } catch (NameNotFoundException e) {
      HL.w(e);
    }
  }

  private String getConnectionType() {
    NetworkInfo networkInfo = ((ConnectivityManager) (getContext()).getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    return networkInfo==null ? "NO_NETWORK":networkInfo.getTypeName();
  }

  private String getLocalIpAddress() {
    try {
      ArrayList<NetworkInterface> list = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface network : list) {
        ArrayList<InetAddress> addressList = Collections.list(network.getInetAddresses());
        for (InetAddress address : addressList) {
          if (!address.isLoopbackAddress()) {
            return address.getHostAddress();
          }
        }
      }
    } catch (SocketException ex) {
    }
    return "UNKNOWN_IP";
  }

  @SuppressLint("NewApi")
  protected String generateUDID() {
    StringBuilder builder = new StringBuilder();
    builder.append(imei);
    builder.append(android_id);
    builder.append(Build.VERSION.SDK_INT >= 9 ? Build.SERIAL:"NO_SERIAL");
    return MD5Util.md5(builder.toString());
  }

  private String getFacebookAttributionId() {
    ContentResolver contentResolver = getContext().getContentResolver();
    Cursor c = contentResolver.query(ATTRIBUTION_ID_CONTENT_URI, new String[]{ATTRIBUTION_ID_COLUMN_NAME}, null, null, null);
    if (c==null || !c.moveToFirst()) {
      return "";
    }
    String attributionId = c.getString(c.getColumnIndex(ATTRIBUTION_ID_COLUMN_NAME));
    c.close();
    return attributionId;
  }

}
