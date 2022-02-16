package ru.threeguns.engine.manager.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.SystemInfo;
import ru.threeguns.engine.pingback.StatisticManager;
import ru.threeguns.manager.ExceptionManager;

public class ExceptionManagerImpl extends Module implements ExceptionManager {
  private static final String TAG = ExceptionManagerImpl.class.getSimpleName();
  private UncaughtExceptionHandler exceptionHandler;

  public ExceptionManagerImpl() {
    exceptionHandler = new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(Thread thread, Throwable ex) {
        notifyException(thread, ex);
      }
    };
  }

  private void logException(final Thread thread, final Throwable exception) {
    StringBuilder builder = new StringBuilder();
    builder.append("Thread:\r\n");
    builder.append(thread.getName());
    builder.append("\r\n\r\nStackTrace:\r\n");
    builder.append(Log.getStackTraceString(exception));
    SharedPreferences.Editor editor = getContext().getSharedPreferences("DEBUG_EXCEPTION_CACHE", Context.MODE_PRIVATE).edit();
    editor.putBoolean("exception", true);
    editor.putString("msg", builder.toString());
    editor.commit();
    Log.e(TAG, builder.toString());
    System.exit(0);
  }

  private void pingbackException(Throwable exception) {
    try {
      Map<String, String> paramMap = new HashMap<String, String>();
      paramMap.put("name", exception.getClass().getName());
      paramMap.put("content", Log.getStackTraceString(exception));
      paramMap.put("app_version", Module.of(SystemInfo.class).app_version);
      paramMap.put("os_version", Module.of(SystemInfo.class).os_version);
      paramMap.put("model", Module.of(SystemInfo.class).model);
      paramMap.put("os_name", Module.of(SystemInfo.class).os_name);
      Module.of(StatisticManager.class).sendActionInfo(StatisticManager.ERROR, paramMap);
    } catch (Exception e) {
    } finally {
      System.exit(0);
    }
  }

  @Override
  public void enableAutoCatchException() {
    Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
  }

  @Override
  public void notifyException(Throwable exception) {
    notifyException(Thread.currentThread(), exception);
  }

  @Override
  public void notifyException(Thread thread, final Throwable exception) {
    HL.w("--------------------Exception--------------------");
    HL.w("Thread:" + thread.getName());
    HL.w(Log.getStackTraceString(exception));
    if (Constants.DEBUG) {
      logException(thread, exception);
    } else {
      pingbackException(exception);
    }
  }

  @Override
  protected void onLoad(Parameter parameter) {}

  @Override
  protected void onRelease() {}

}
