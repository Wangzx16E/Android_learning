package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kh.hyper.core.Module;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.network.NetworkUtil;
import ru.threeguns.ui.LoginActivity;

public class RulesFragment extends DFragment {

  private WebView webView;

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    int tg_fragment_rules_id = getActivity().getResources().getIdentifier("tg_fragment_rules", "layout", getActivity().getPackageName());
    View v = View.inflate(getActivity(), tg_fragment_rules_id, null);
    Activity activity = getActivity();
    if (activity instanceof LoginActivity) {
      LoginActivity loginActivity = (LoginActivity) getActivity();
      loginActivity.setCenter();
    }

    int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requestBack();
      }
    });

    int tg_exit_btn_id = getActivity().getResources().getIdentifier("tg_exit_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requestBack();
      }
    });

    int tg_webview_id = getActivity().getResources().getIdentifier("tg_webview", "id", getActivity().getPackageName());
    webView = (WebView) v.findViewById(tg_webview_id);
    WebSettings ws = webView.getSettings();

    ws.setJavaScriptEnabled(true);
    ws.setDefaultTextEncodingName("UTF-8");
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
      }
    });

    //		InputStream is = getResources().openRawResource(R.raw.tg_rules);
    //
    //		StringBuilder builder = new StringBuilder();
    //		byte[] buffer = new byte[1024 * 128];
    //		int len = -1;
    //		try {
    //			while ((len = is.read(buffer)) != -1) {
    //				builder.append(new String(buffer, 0, len));
    //			}
    //		} catch (IOException e) {
    //			HL.w(e);
    //		} finally {
    //			try {
    //				if (is != null) {
    //					is.close();
    //				}
    //			} catch (IOException e) {
    //				HL.w(e);
    //			}
    //		}
    //
    //		webView.loadDataWithBaseURL(null, builder.toString(), "text/html", "utf-8", null);

    webView.loadUrl(buildUrl("/api/rules"));

    return v;
  }

  private String buildUrl(String path) {
    StringBuilder url = new StringBuilder();
    url.append(NetworkUtil.getHostAddress(null))//
        .append(path)//
        .append("/?")//
        .append("&sdklang=")//
        .append(Module.of(TGController.class).appLanguage);
    return url.toString();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    webView.destroy();
  }

}
