package ru.threeguns.engine.billing;

import android.content.Intent;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.network.StringController;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.AbstractPaymentManager;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.fragments.WebPaymentFragment;
import ru.threeguns.utils.ActivityHolder;

public class WebPaymentManagerImpl extends AbstractPaymentManager {
	public static PaymentRequest paymentRequestHolder;

	@Override
	protected void doPay(final PaymentRequest request) {
		paymentRequestHolder = request;
		Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), CommonWebActivity.class);
		intent.putExtra("fragment", WebPaymentFragment.class);
		StringBuilder url = new StringBuilder(Module.of(StringController.class).load("TG_HOST_ADDRESS"));
		url.append("/api/pay/");
		url.append("?user_id=");
		url.append(Module.of(UserCenter.class).getUserId());
		url.append("&token=");
		url.append(Module.of(UserCenter.class).getToken());
		url.append("&appid=");
		url.append(Module.of(TGController.class).appId);
		url.append("&server_id=");
		url.append(request.getServerId());
		intent.putExtra("target_url", url.toString());
		Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);

	}

	@Override
	protected void onLoad(Parameter parameter) {

	}

	@Override
	protected void onRelease() {

	}

}
