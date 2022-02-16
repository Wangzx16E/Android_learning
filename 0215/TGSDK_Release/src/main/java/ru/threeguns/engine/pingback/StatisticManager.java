package ru.threeguns.engine.pingback;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.Handle;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.SystemInfo;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.entity.User;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.event.UserRegisterEvent;
import ru.threeguns.event.handler.TGHandler;
import ru.threeguns.manager.TrackManager;
import ru.threeguns.network.NetworkCode;
import ru.threeguns.network.NetworkUtil;
import ru.threeguns.utils.TimeUnit;

//统计
public final class StatisticManager extends Module {
  public static final int LOGIN = 0;
  public static final int REGISTER = 1;
  public static final int OPEN = 2;
  public static final int PAY = 3;
  public static final int ERROR = 4;
  public static final int BEGIN_SESSION = 5;
  public static final int END_SESSION = 6;
  public static final int UPDATE_SESSION = 7;
  private static final String PINGBACK_BEHAVIOUR = "ping";
  private static final String REQUEST_BASE_URL = "/api/statistics/";
  private static final String[] extraActionStrArr = new String[]{"event", "event_name", "event_value"};
  private static final String[] actionTypeArr = new String[]{ //
      "login", "register", "open", "pay", "error_report", "begin_session", "end_session", "update_session" //
  };
  // putAllInfo flag
  private static final boolean[] actionTypeInfoFlag = new boolean[]{ //
      false, false, true, false, false, false, false, false //
  };

  // 存储统计事件的cache
  private PingBackEventCache pingbackCache;
  private SendActionThread sendActionThread;
  private OkHttpClient httpClient;
  private Object sendActionLock = new Object();

  @Override
  protected void onLoad(Parameter parameter) {
    sendActionThread = new SendActionThread();
    pingbackCache = new PingBackEventCacheDBImpl(getContext().getApplicationContext());
    httpClient = new OkHttpClient();
    sendActionThread.start();
    new TGHandler() {
      @Handle()
      private void onLogin(UserLoginEvent event) {
        if (UserLoginEvent.SUCCESS == event.getResult()) {
          String userType = event.getUser().getUserType();
          String eventName = (!User.USERTYPE_GUEST.equals(userType) && !User.USERTYPE_NORMAL.equals(userType)) ? TrackManager.LOGIN_TP
              : TrackManager.LOGIN;
          Module.of(TrackManager.class).trackEvent(//
              new TrackManager.TrackEvent(eventName)//
                  .setExtraParams("userId", event.getUser().getUserId())//
          );
        }
      }

      @Handle()
      private void onRegister(UserRegisterEvent event) {
        String eventName = User.USERTYPE_GUEST.equals(event.getUser().getUserType()) ? TrackManager.FAST_REGISTER : TrackManager.REGISTER;
        Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent(eventName));
        Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent("total_register"));
      }
    }.register();
  }

  @Override
  protected void onRelease() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        sendActionThread.requestStop();
      }
    }).start();
  }

  public PingBackEventCache getPingBackCache() {
    return pingbackCache;
  }

  public void sendActionInfo(int actionType) {
    sendActionInfo(actionType, null);
  }

  public void sendActionInfo(int actionType, Map<String, String> paramMap) {
    sendActionInfo(actionTypeArr[actionType], paramMap, actionTypeInfoFlag[actionType]);
  }

  public void sendActionInfo(String actionType, Map<String, String> paramMap, boolean putAllInfo) {
    try {
      pingbackCache.offerEvent(new PingBackEvent(//
          makeRequestURL(actionType), //
          makeDataString(//
              paramMap, //
              putAllInfo//
          )//
      ));
    } catch (Exception e) {
      HL.w("sendActionInfo Failed:" + e.getLocalizedMessage());
    }
    notifySendAction();
  }

  private String makeRequestURL(String actionName) {
    return NetworkUtil.getHostAddress(PINGBACK_BEHAVIOUR) + REQUEST_BASE_URL + actionName;
  }

  private String makeDataString(Map<String, String> paramMap, boolean putAllInfo) {
    if (paramMap == null) {
      paramMap = new HashMap<String, String>();
    }
    if (!paramMap.containsKey("sdk_version")) {
      paramMap.put("sdk_version", TGController.SDK_VERSION);
    }
    if (!paramMap.containsKey("user_id")) {
      paramMap.put("user_id", Module.of(UserCenter.class).getUserId());
    }
    if (!paramMap.containsKey("lang")) {
      paramMap.put("sdklang", Module.of(TGController.class).appLanguage);
    }
    if (!paramMap.containsKey("channel_id")) {
      paramMap.put("channel_id", Module.of(TGController.class).channelId);
    }
    if (!paramMap.containsKey("timestamp")) {
      paramMap.put("timestamp", getUTCTime());
    }
    if (putAllInfo) {
      paramMap.putAll(Module.of(SystemInfo.class).getTotalSystemInfo());
    } else {
      paramMap.putAll(Module.of(SystemInfo.class).getSimpleSystemInfo());
    }
    // for(Map.Entry<String, String> e : paramMap.entrySet()){
    // HL.w("{} = {}",e.getKey(),e.getValue());
    // }
    return NetworkUtil.makeDataString(paramMap);
  }

  public void notifySendAction() {
    synchronized (sendActionLock) {
      sendActionLock.notify();
    }
  }

  private String getUTCTime() {
    return "" + (System.currentTimeMillis() / 1000);
  }

  public void sendActionInfo(String extraActionName, String extraActionValue) {
    sendActionInfo(extraActionName, extraActionValue, null);
  }

  public void sendActionInfo(String extraActionName, String extraActionValue, Map<String, String> paramMap) {
    try {
      if (paramMap == null) {
        paramMap = new HashMap<String, String>();
      }

      paramMap.put(extraActionStrArr[1], extraActionName);
      if (extraActionValue != null && !extraActionValue.trim().equals("")) {
        paramMap.put(extraActionStrArr[2], extraActionValue);
      }
      pingbackCache.offerEvent(new PingBackEvent(makeRequestURL(extraActionStrArr[0]), makeDataString(paramMap, false)));
    } catch (Exception e) {
      HL.w("sendActionInfo Failed:" + e.getLocalizedMessage());
    }
    notifySendAction();
  }

  private class SendActionThread extends Thread {
    // 网络请求失败的重试时间
    private static final long DEFAULT_WAIT_TIME = 15 * TimeUnit.MINUTE;
    // 网络请求超时的重试时间
    private static final long DEFAULT_RETRY_INTERVAL = 3 * TimeUnit.MINUTE;
    private long waitTime = DEFAULT_WAIT_TIME;
    private volatile boolean loop = true;

    @Override
    public void run() {
      while (loop) {
        try {
          final PingBackEvent event = pingbackCache.pollEvent();

          if (event != null) {

            final Request request = new Request.Builder()//
                .url(event.getRequestURL())//
                .post(RequestBody.create(null, event.getDataString()))//
                .build();

            HL.i("========Statistic : {} ========", event.getRequestURL());

            Call call = httpClient.newCall(request);
            Response response = null;
            try {
              response = call.execute();
              int httpCode = response.code();
              String data = response.body().string();
              HL.i("===Response code : {}", httpCode);
              HL.i("===Response body : {}", data);
              if (httpCode == 200) {
                JSONObject json = new JSONObject(data);
                int code = json.optInt("code");
                if (code == NetworkCode.NO_ERROR) {
                  pingbackCache.removeEvent(event);
                  waitTime = 0;
                } else if (code == NetworkCode.NETWORK_TIMED_OUT) {
                  waitTime = DEFAULT_RETRY_INTERVAL;
                } else {
                  if (code > 0) {
                    pingbackCache.removeEvent(event);
                    waitTime = 0;
                    HL.w("Send Pingback get response : " + code + " from server , remove it from DB.");
                  } else {
                    waitTime = DEFAULT_WAIT_TIME;
                  }
                }
              } else {
                waitTime = DEFAULT_RETRY_INTERVAL;
              }
            } catch (IOException e) {
              waitTime = DEFAULT_RETRY_INTERVAL;
            } catch (JSONException e) {
              waitTime = DEFAULT_RETRY_INTERVAL;
            }

          } else {
            // 没有事件,一直等待直至抛出事件并notify
            waitTime = -1;
          }
        } finally {
          synchronized (sendActionLock) {
            try {
              if (waitTime > 0) {
                sendActionLock.wait(waitTime);
              } else if (waitTime < 0) {
                sendActionLock.wait();
              } else {
                // continue loop
              }
            } catch (InterruptedException e) {
              // ignore
            }
          }
        }
      }
    }

    protected void requestStop() {
      loop = false;
      SendActionThread.this.interrupt();
      try {
        join(5000);
      } catch (InterruptedException e) {
        HL.w(e);
      } finally {
        pingbackCache.release();
      }
    }
  }
}
