package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.net.URLDecoder;

import kh.hyper.ui.HFragment;
import kh.hyper.utils.HL;
import ru.threeguns.ui.views.TGWebView;

public class CommonWebFragment extends HFragment {
	private TGWebView webView;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Activity activity = getActivity();
		Resources res = activity.getResources();
		String packageName = activity.getPackageName();
		int tg_fragment_commonweb_id = res.getIdentifier("tg_fragment_commonweb", "layout", packageName);
		View v = inflater.inflate(tg_fragment_commonweb_id, null);

		int tg_back_btn_id = res.getIdentifier("tg_back_btn", "id", packageName);
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

		webView = v.findViewById(res.getIdentifier("tg_webview", "id", packageName));

		WebSettings ws = webView.getSettings();
		ws.setDefaultTextEncodingName("UTF-8");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				HL.w("shouldOverrideUrlLoading = {}", url);
				// 获取上下文, H5PayDemoActivity为当前页面
				final Activity context = getActivity();

				//无法唤起支付宝APP的处理
				url = URLDecoder.decode(url);
				url = url.replace("https://render.alipay.com/p/s/i?scheme=","");

				// ------  对alipays:相关的scheme处理 -------
				if (url.startsWith("alipays:") || url.startsWith("alipay")) {
					try {
						context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
					} catch (Exception e) {
						new AlertDialog.Builder(context).setMessage("未检测到支付宝客户端，请安装后重试。").setPositiveButton("立即安装", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Uri alipayUrl = Uri.parse("https://d.alipay.com");
								context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
							}
						}).setNegativeButton("取消", null).show();
					}
					return true;
				}
				// ------- 处理结束 -------

				if (!(url.startsWith("http") || url.startsWith("https"))) {
					return true;
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				HL.w("onPageFinished = {}", url);
				super.onPageFinished(view, url);
			}
		});

		int tg_title_id = res.getIdentifier("tg_title", "id", packageName);
		TextView tv = v.findViewById(tg_title_id);
		String title = getArguments().getString("title");
		if (!TextUtils.isEmpty(title)) {
			tv.setText(title);
		}

		webView.loadUrl(getArguments().getString("target_url"));

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.destroy();
	}
}