package ru.threeguns.engine.billing;

import android.content.Intent;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.network.StringController;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.fragments.Paypal2PaymentFragment;
import ru.threeguns.utils.ActivityHolder;

public final class Paypal2PaymentManagerImpl extends Module {

	public void doPay(String orderId) {
		Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), CommonWebActivity.class);
		intent.putExtra("fragment", Paypal2PaymentFragment.class);
		StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
		url.append("/api/web/?user_id=");
		url.append(Module.of(UserCenter.class).getUserId());
		url.append("&token=");
		url.append(Module.of(UserCenter.class).getToken());
		url.append("&order_id=");
		url.append(orderId);
		url.append("&sdklang=");
		url.append(Module.of(TGController.class).appLanguage);
		intent.putExtra("target_url", url.toString());
		Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
	}
	@Override
	protected void onLoad(Parameter parameter) {}


	@Override
	protected void onRelease() {}

}
