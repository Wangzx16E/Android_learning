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
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.NetworkUtil;
import ru.threeguns.ui.views.TGWebView;

public class MyFeedbackFragment extends HFragment {
	private TGWebView webView;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_myfeedback_id = getActivity().getResources().getIdentifier("tg_fragment_myfeedback", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_myfeedback_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_webview_id = getActivity().getResources().getIdentifier("tg_webview", "id", getActivity().getPackageName());
		webView = (TGWebView) v.findViewById(tg_webview_id);

		StringBuilder url = new StringBuilder();
		url.append(NetworkUtil.getHostAddress(null))//
				.append("/api/contact/?")//
				.append("user_id=")//
				.append(Module.of(UserCenter.class).getUserId())//
				.append("&token=")//
				.append(Module.of(UserCenter.class).getToken())//
				.append("&appid=")//
				.append(Module.of(TGController.class).appId)//
				.append("&sdklang=")//
				.append(Module.of(TGController.class).appLanguage);
		WebSettings ws = webView.getSettings();
		ws.setDefaultTextEncodingName("UTF-8");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});
		
		HL.i("myfeedback url : {}",url);
		
		webView.loadUrl(url.toString());

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.destroy();
	}

}
