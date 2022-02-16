package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.network.StringController;
import kh.hyper.ui.Demand;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.PaymentApi;

public class WalletFragment extends HFragment {
	private TextView balanceTextView;
	private String balanceText;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_wallet_id = getActivity().getResources().getIdentifier("tg_fragment_wallet", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_wallet_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_balance_id = getActivity().getResources().getIdentifier("tg_balance", "id", getActivity().getPackageName());
		balanceTextView = (TextView) v.findViewById(tg_balance_id);
		balanceText = balanceTextView.getText().toString();

        int tg_recharge_btn_id = getActivity().getResources().getIdentifier("tg_recharge_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_recharge_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
				url.append("/api/kb_recharge/");
				url.append("?user_id=");
				url.append(Module.of(UserCenter.class).getUserId());
				url.append("&token=");
				url.append(Module.of(UserCenter.class).getToken());
				url.append("&appid=");
				url.append(Module.of(TGController.class).appId);
				url.append("&sdklang=");
				url.append(Module.of(TGController.class).appLanguage);
				if (!TextUtils.isEmpty(Module.of(TGController.class).serverLanguage)) {
					url.append("&language=");
					url.append(Module.of(TGController.class).serverLanguage);
				}
				Bundle b = new Bundle();
				b.putString("target_url", url.toString());
				changeFragment(new Demand(CommonWebFragment.class).bundle(b));
			}
		});

		return v;
	}

	@Override
	protected void onEnter() {
		super.onEnter();

		HClient.of(PaymentApi.class).getUserBalance("", new TGResultHandler() {

			@Override
			protected void onSuccess(JSONObject json) throws JSONException {
				final int balance = json.getInt("balance");

				balanceTextView.post(new Runnable() {
					public void run() {
						balanceTextView.setText(String.format(balanceText, balance));
					}
				});
			}

			@Override
			protected void onFailed(int errorCode, String msg) {

			}
		});
	}

}
