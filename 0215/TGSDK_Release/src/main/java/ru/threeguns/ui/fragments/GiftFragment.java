package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import kh.hyper.core.Module;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.NetworkUtil;
import ru.threeguns.ui.views.TGWebView;

public class GiftFragment extends HFragment {
	private TGWebView webView;

	private boolean inGiftPage = true;
	private TextView giftButton;
	private TextView giftTitle;

	private String buildUrl(String path) {
		StringBuilder url = new StringBuilder();
		url.append(NetworkUtil.getHostAddress(null))//
				.append(path)//
				.append("/?")//
				.append("user_id=")//
				.append(Module.of(UserCenter.class).getUserId())//
				.append("&token=")//
				.append(Module.of(UserCenter.class).getToken())//
				.append("&appid=")//
				.append(Module.of(TGController.class).appId)//
				.append("&sdklang=")//
				.append(Module.of(TGController.class).appLanguage);
		return url.toString();
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_gift_id = getActivity().getResources().getIdentifier("tg_fragment_gift", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_gift_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_gift_btn_id = getActivity().getResources().getIdentifier("tg_gift_btn", "id", getActivity().getPackageName());
        int tg_title_gift_id = getActivity().getResources().getIdentifier("tg_title_gift", "id", getActivity().getPackageName());
		giftButton = (TextView) v.findViewById(tg_gift_btn_id);
		giftTitle = (TextView) v.findViewById(tg_title_gift_id);

		giftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int tg_layout_gift_id = getActivity().getResources().getIdentifier("tg_layout_gift", "string", getActivity().getPackageName());
		        int tg_layout_mygift_id = getActivity().getResources().getIdentifier("tg_layout_mygift", "string", getActivity().getPackageName());
				if (inGiftPage) {
					inGiftPage = false;
					giftButton.setText(getActivity().getResources().getString(tg_layout_gift_id));
					giftTitle.setText(getActivity().getResources().getString(tg_layout_mygift_id));
					webView.loadUrl(buildUrl("/api/myGiftsPacks"));
				} else {
					inGiftPage = true;
					giftButton.setText(getActivity().getResources().getString(tg_layout_mygift_id));
					giftTitle.setText(getActivity().getResources().getString(tg_layout_gift_id));
					webView.loadUrl(buildUrl("/api/giftsPacks"));
				}
			}
		});

		int tg_webview_id = getActivity().getResources().getIdentifier("tg_webview", "id", getActivity().getPackageName());
		webView = (TGWebView) v.findViewById(tg_webview_id);

		WebSettings ws = webView.getSettings();
		ws.setDefaultTextEncodingName("UTF-8");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});

		webView.loadUrl(buildUrl("/api/giftsPacks"));

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.destroy();
	}

}
