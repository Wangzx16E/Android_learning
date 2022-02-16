package ru.threeguns.ui.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.network.HClient;
import kh.hyper.network.StringController;
import kh.hyper.utils.HL;
import ru.threeguns.engine.billing.WebPaymentManagerImpl;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.event.ActivityResultEvent;
import ru.threeguns.event.FollowEvent;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.PaymentApi;
import ru.threeguns.ui.AccountActivity;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.utils.ActivityHolder;
import ru.threeguns.utils.MediaUtil;

public class TGWebView extends RelativeLayout {
	private static final long FILE_LIMIT = 1024 * 1024 * 3;
	private static final int REQUEST_FILE_PICKER = 12717;
	private ValueCallback<Uri> mFilePathCallback4;
	private ValueCallback<Uri[]> mFilePathCallback5;

	private ProgressBar pb1;
	private ProgressBar pb2;
	private WebView wb;

	public TGWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();

		EventManager.instance.register(this);
	}

	public WebView getWebView() {
		return wb;
	}

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void initView() {
        int tg_webview_layout_id = getContext().getResources().getIdentifier("tg_webview_layout", "layout", getContext().getPackageName());
        int __pb1_id = getContext().getResources().getIdentifier("__pb1", "id", getContext().getPackageName());
        int __pb2_id = getContext().getResources().getIdentifier("__pb2", "id", getContext().getPackageName());
        int __webview_id = getContext().getResources().getIdentifier("__webview", "id", getContext().getPackageName());

        View view = View.inflate(getContext(), tg_webview_layout_id, this);
		pb1 = (ProgressBar) view.findViewById(__pb1_id);
		pb2 = (ProgressBar) view.findViewById(__pb2_id);
		wb = (WebView) view.findViewById(__webview_id);
		WebSettings settings = wb.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("utf-8");

		// settings.setJavaScriptCanOpenWindowsAutomatically(flag);

		if (Build.VERSION.SDK_INT >= 21) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}

		wb.setWebChromeClient(new CustomWebChromeClient());

		wb.addJavascriptInterface(new Object() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@JavascriptInterface
			public void setClipboard(String s) {
				if (android.os.Build.VERSION.SDK_INT > 11) {
					android.content.ClipboardManager c = (android.content.ClipboardManager) getContext().getSystemService(Activity.CLIPBOARD_SERVICE);
					c.setPrimaryClip(ClipData.newPlainText("text", s));
				} else {
					android.text.ClipboardManager c = (android.text.ClipboardManager) getContext().getSystemService(Activity.CLIPBOARD_SERVICE);
					c.setText(s);
				}
			}

			@JavascriptInterface
			public void showToast(String s) {
				Module.of(UIHelper.class).showToast(s);
			}

			@JavascriptInterface
			public void finishActivity() {
				HL.i("finishActivity");
				Activity act = Module.of(ActivityHolder.class).getTopActivity();
				if (act != null) {
					if (act instanceof CommonWebActivity) {
						act.finish();
					} else if (act instanceof AccountActivity) {
						((AccountActivity) act).requestBack();
					}
				}
			}

			@JavascriptInterface
			public void webPay(String payment_type, float amount) {
				Map<String, String> paymentParams = WebPaymentManagerImpl.paymentRequestHolder.toParams();
				paymentParams.put("amount", String.valueOf(amount));
				HClient.of(PaymentApi.class).createOrder(payment_type, paymentParams, new TGResultHandler() {

					@Override
					protected void onSuccess(JSONObject json) throws JSONException {
						String orderId = json.optString("order_id");
						final StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
						url.append("/api/platformTopup/");
						url.append("?user_id=");
						url.append(Module.of(UserCenter.class).getUserId());
						url.append("&token=");
						url.append(Module.of(UserCenter.class).getToken());
						url.append("&order_id=");
						url.append(orderId);

						Module.of(UIHelper.class).post2MainThread(new Runnable() {
							public void run() {
								loadUrl(url.toString());
							}
						});
					}

					@Override
					protected void onFailed(int errorCode, String msg) {
						notifyPaymentFailed();
					}
				});
			}

			@JavascriptInterface
			public void kbRecharge(String payment_type, int kbCount) {
				HClient.of(PaymentApi.class).createKBOrder(payment_type, kbCount, new TGResultHandler() {

					@Override
					protected void onSuccess(JSONObject json) throws JSONException {
						String orderId = json.optString("order_id");
						final StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
						url.append("/api/kb_recharge_pay/");
						url.append("?user_id=");
						url.append(Module.of(UserCenter.class).getUserId());
						url.append("&token=");
						url.append(Module.of(UserCenter.class).getToken());
						url.append("&order_id=");
						url.append(orderId);
						url.append("&appid=");
						url.append(Module.of(TGController.class).appId);
						url.append("&sdklang=");
						url.append(Module.of(TGController.class).appLanguage);

						Module.of(UIHelper.class).post2MainThread(new Runnable() {
							public void run() {
								loadUrl(url.toString());
							}
						});
					}

					@Override
					protected void onFailed(int errorCode, String msg) {
						notifyPaymentFailed();
					}
				});
			}

			@JavascriptInterface
			public void gbRecharge(String payment_type, int kbCount, final String PIN) {
				HClient.of(PaymentApi.class).createKBOrder(payment_type, kbCount, new TGResultHandler() {

					@Override
					protected void onSuccess(JSONObject json) throws JSONException {
						String orderId = json.optString("order_id");
						final StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
						url.append("/api/kb_recharge_pay/");
						url.append("?user_id=");
						url.append(Module.of(UserCenter.class).getUserId());
						url.append("&token=");
						url.append(Module.of(UserCenter.class).getToken());
						url.append("&order_id=");
						url.append(orderId);
						url.append("&PIN=");
						url.append(PIN);
						url.append("&appid=");
						url.append(Module.of(TGController.class).appId);
						url.append("&sdklang=");
						url.append(Module.of(TGController.class).appLanguage);

						Module.of(UIHelper.class).post2MainThread(new Runnable() {
							public void run() {
								loadUrl(url.toString());
							}
						});
					}

					@Override
					protected void onFailed(int errorCode, String msg) {
						notifyPaymentFailed();
					}
				});
			}

			@JavascriptInterface
			public void notifyFollowSuccess(String result) {
				HL.i("notifyFollowSuccess");
				EventManager.instance.dispatch(new FollowEvent(FollowEvent.SUCCESS, result));
				finishActivity();
			}

			@JavascriptInterface
			public void notifyPaymentSuccess(String orderId) {
				HL.i("notifyPaymentSuccess");
				EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.SUCCESS, orderId));
				finishActivity();
			}

			@JavascriptInterface
			public void notifyPaymentFailed() {
				HL.i("notifyPaymentFailed");
				EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.FAILED, null));
				finishActivity();
			}

			@JavascriptInterface
			public void notifyKBRechargeSuccess() {
				Module.of(UIHelper.class).showToast(TGString.recharge_kb_success);
				finishActivity();
			}

			@JavascriptInterface
			public void notifyKBRechargeFailed() {
				Module.of(UIHelper.class).showToast(TGString.recharge_kb_failed);
				finishActivity();
			}

		}, "Android");
	}

	private void checkCallback() {
		if (mFilePathCallback4 != null) {
			mFilePathCallback4.onReceiveValue(null);
			mFilePathCallback4 = null;
		}
		if (mFilePathCallback5 != null) {
			mFilePathCallback5.onReceiveValue(null);
			mFilePathCallback5 = null;
		}
	}

	private class CustomWebChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			pb1.setProgress(newProgress);
			if (newProgress == 100) {
				pb1.setVisibility(GONE);
				pb2.setVisibility(GONE);
			} else {
				if (pb1.getVisibility() == GONE) {
					pb1.setVisibility(VISIBLE);
					pb2.setVisibility(VISIBLE);
				}
			}
			super.onProgressChanged(view, newProgress);
		}

		@SuppressWarnings("unused")
		public void openFileChooser(ValueCallback<Uri> filePathCallback) {
			checkCallback();
			mFilePathCallback4 = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			Module.of(ActivityHolder.class).getTopActivity().startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
		}

		@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
		public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
			checkCallback();
			mFilePathCallback4 = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			Module.of(ActivityHolder.class).getTopActivity().startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
		}

		@SuppressWarnings("unused")
		public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
			checkCallback();
			mFilePathCallback4 = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			Module.of(ActivityHolder.class).getTopActivity().startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
		}

		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
			checkCallback();
			mFilePathCallback5 = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			Module.of(ActivityHolder.class).getTopActivity().startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			return true;
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			// 弹窗处理
			AlertDialog.Builder b2 = new AlertDialog.Builder(Module.of(ActivityHolder.class).getTopActivity()).setMessage(message).setPositiveButton("ok",
					new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							result.confirm();
						}
					});

			b2.setCancelable(false);
			b2.create();
			b2.show();

			return true;
		}

	}

	@Handle
	public void onActivityResult(ActivityResultEvent e) {
		int requestCode = e.getRequestCode();
		int resultCode = e.getResultCode();
		Intent intent = e.getData();

		if (requestCode == REQUEST_FILE_PICKER) {
			Uri data = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
			Uri result = null;
			if (data != null) {
				String path = MediaUtil.getPath(getContext(), data);
				File f = new File(path);
				if (f.exists()) {
					if (f.length() <= FILE_LIMIT) {
						result = Uri.fromFile(f);
					} else {
						Module.of(UIHelper.class).showToast(TGString.chooseimg_sizeerror);
					}
				} else {
					Module.of(UIHelper.class).showToast(TGString.chooseimg_failed);
				}
			}
			if (mFilePathCallback4 != null) {
				if (result != null) {
					mFilePathCallback4.onReceiveValue(result);
				} else {
					mFilePathCallback4.onReceiveValue(null);
				}
			}
			if (mFilePathCallback5 != null) {
				if (result != null) {
					mFilePathCallback5.onReceiveValue(new Uri[] { result });
				} else {
					mFilePathCallback5.onReceiveValue(null);
				}
			}

			mFilePathCallback4 = null;
			mFilePathCallback5 = null;
		}
	}

	public WebSettings getSettings() {
		return wb.getSettings();
	}

	public void addJavascriptInterface(Object object, String name) {
		wb.addJavascriptInterface(object, name);
	}

	public void setWebViewClient(WebViewClient client) {
		wb.setWebViewClient(client);
	}

	public void loadUrl(String url) {
		wb.loadUrl(url);
	}

	public boolean canGoBack() {
		return wb.canGoBack();
	}

	public void goBack() {
		wb.goBack();
	}

	public void destroy() {
		removeAllViews();
		wb.destroy();

		EventManager.instance.unregister(this);
	}
}
