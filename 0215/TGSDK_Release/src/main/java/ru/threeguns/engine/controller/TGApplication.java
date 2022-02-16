package ru.threeguns.engine.controller;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.Map;

public class TGApplication extends Application {
  public static TGApplication tgApplication;


  @SuppressLint("NewApi")
  @Override
  public void onCreate() {
    super.onCreate();
    tgApplication = this;

    AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
      @Override
      public void onConversionDataSuccess(Map<String, Object> conversionData) {
        for (String attrName : conversionData.keySet()) {
          Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData.get(attrName));
        }
      }

      @Override
      public void onConversionDataFail(String errorMessage) {
        Log.d("LOG_TAG", "error getting conversion data: " + errorMessage);
      }

      @Override
      public void onAppOpenAttribution(Map<String, String> conversionData) {
        for (String attrName : conversionData.keySet()) {
          Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData.get(attrName));
        }
      }

      @Override
      public void onAttributionFailure(String errorMessage) {
        Log.d("LOG_TAG", "error onAttributionFailure : " + errorMessage);
      }
    };
    AppsFlyerLib.getInstance().init("tUQXf7ZVjzLju4LfHGrSYR", conversionListener, getApplicationContext());
    AppsFlyerLib.getInstance().start(this);
  }
}