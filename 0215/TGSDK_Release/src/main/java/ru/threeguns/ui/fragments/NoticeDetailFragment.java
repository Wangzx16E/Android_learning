package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kh.hyper.core.Module;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.ui.views.TGWebView;

public class NoticeDetailFragment extends HFragment {
	private TGWebView webView;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_noticedetail_id = getActivity().getResources().getIdentifier("tg_fragment_noticedetail", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_noticedetail_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_webview_id = getActivity().getResources().getIdentifier("tg_webview", "id", getActivity().getPackageName());
		webView = (TGWebView) v.findViewById(tg_webview_id);

		String url = getArguments().getString("target_url");

		if (url.indexOf("?") != -1) {
			url += "&user_id=";
			url += Module.of(UserCenter.class).getUserId();
			url += "&token=";
			url += Module.of(UserCenter.class).getToken();
			url += "&app_id=";
			url += Module.of(TGController.class).appId;
			url += "&sdklang=";
			url += Module.of(TGController.class).appLanguage;
		} else {
			url += "?user_id=";
			url += Module.of(UserCenter.class).getUserId();
			url += "&token=";
			url += Module.of(UserCenter.class).getToken();
			url += "&app_id=";
			url += Module.of(TGController.class).appId;
			url += "&sdklang=";
			url += Module.of(TGController.class).appLanguage;
		}

		WebSettings ws = webView.getSettings();
		ws.setDefaultTextEncodingName("UTF-8");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});

		webView.loadUrl(url);

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.destroy();
	}

}
