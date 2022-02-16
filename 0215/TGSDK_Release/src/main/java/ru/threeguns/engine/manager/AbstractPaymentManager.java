package ru.threeguns.engine.manager;

import android.app.Activity;
import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.event.handler.PaymentHandler;
import ru.threeguns.manager.PaymentManager;
import ru.threeguns.utils.TimeUnit;
import ru.threeguns.utils.VerifyUtil;

public abstract class AbstractPaymentManager extends Module implements PaymentManager {
	private static final long PAYMENT_INTERVAL = 1 * TimeUnit.SECOND;
	private long lastPaymentTime;
	private PaymentHandler paymentHandler;

	@Override
	public void requestPay(Activity activity, PaymentRequest request, PaymentHandler handler) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPaymentTime <= PAYMENT_INTERVAL) {
			return;
		}
		lastPaymentTime = currentTime;

		paymentHandler = handler;
		if (handler != null) {
			handler.register();
		}
		if (Module.of(UserCenter.class).getActiveUser() == null || !VerifyUtil.notEmpty(Module.of(UserCenter.class).getToken())) {
			notifyPayFailed();
			Module.of(UIHelper.class).showToast(TGString.network_token_invalid);
		} else {
			doPay(request);
		}
	}

	protected void notifyCancelPay() {
		EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.USER_CANCEL, null));
	}

	protected void notifyPayFailed() {
		EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.FAILED, null));
	}

	protected void notifyPaySuccess(String orderId) {
		EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.SUCCESS, orderId));
	}

	protected abstract void doPay(PaymentRequest request);

	@Override
	public String getPriceByProductId(String productId) {
		return null;
	}

}
