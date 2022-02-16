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

public class FeedbackFragment extends HFragment {
	private TGWebView webView;

	private boolean inFeedbackPage = false;
	private TextView feedbackButton;
	private TextView feedbackTitle;

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
        int tg_fragment_feedback_id = getActivity().getResources().getIdentifier("tg_fragment_feedback", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_feedback_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});
		
        int tg_feedback_btn_id = getActivity().getResources().getIdentifier("tg_feedback_btn", "id", getActivity().getPackageName());
        int tg_title_feedback_id = getActivity().getResources().getIdentifier("tg_title_feedback", "id", getActivity().getPackageName());
		feedbackButton = (TextView) v.findViewById(tg_feedback_btn_id);
		feedbackTitle = (TextView) v.findViewById(tg_title_feedback_id);

		feedbackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        int tg_layout_feedback_id = getActivity().getResources().getIdentifier("tg_layout_feedback", "string", getActivity().getPackageName());
		        int tg_layout_myfeedback_id = getActivity().getResources().getIdentifier("tg_layout_myfeedback", "string", getActivity().getPackageName());

				if (inFeedbackPage) {
					inFeedbackPage = false;
					feedbackButton.setText(getActivity().getResources().getString(tg_layout_feedback_id));
					feedbackTitle.setText(getActivity().getResources().getString(tg_layout_myfeedback_id));
					webView.loadUrl(buildUrl("/api/contact"));
				} else {
					inFeedbackPage = true;
					feedbackButton.setText(getActivity().getResources().getString(tg_layout_myfeedback_id));
					feedbackTitle.setText(getActivity().getResources().getString(tg_layout_feedback_id));
					webView.loadUrl(buildUrl("/api/question"));
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

		webView.loadUrl(buildUrl("/api/contact"));

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.destroy();
	}

}
