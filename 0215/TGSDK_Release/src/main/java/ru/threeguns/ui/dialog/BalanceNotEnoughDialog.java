package ru.threeguns.ui.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import kh.hyper.core.Module;
import kh.hyper.network.StringController;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.utils.ActivityHolder;

public class BalanceNotEnoughDialog extends TGDialog {
	private int requestKB;
	private int currentKB;

	public BalanceNotEnoughDialog(Context context, int requestKB, int currentKB) {
		super(context);
		this.requestKB = requestKB;
		this.currentKB = currentKB;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        int tg_dialog_balance_not_enough_id = getContext().getResources().getIdentifier("tg_dialog_balance_not_enough", "layout", getContext().getPackageName());
		setContentView(tg_dialog_balance_not_enough_id);

        int tg_amount_id = getContext().getResources().getIdentifier("tg_amount", "id", getContext().getPackageName());
        int tg_balance_id = getContext().getResources().getIdentifier("tg_balance", "id", getContext().getPackageName());
		TextView amount = (TextView) findViewById(tg_amount_id);
		amount.setText(String.valueOf(requestKB));
		TextView balance = (TextView) findViewById(tg_balance_id);
		balance.setText(String.valueOf(currentKB));

        int tg_kb_recharge_id = getContext().getResources().getIdentifier("tg_kb_recharge", "id", getContext().getPackageName());
		findViewById(tg_kb_recharge_id).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), CommonWebActivity.class);
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
				intent.putExtra("target_url", url.toString());
				Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
			}
		});

        int tg_exit_btn_id = getContext().getResources().getIdentifier("tg_exit_btn", "id", getContext().getPackageName());
		findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

	}

}
