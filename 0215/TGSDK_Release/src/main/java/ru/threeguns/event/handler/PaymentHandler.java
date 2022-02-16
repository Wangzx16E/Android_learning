package ru.threeguns.event.handler;

import kh.hyper.core.Module;
import kh.hyper.event.Handle;
import kh.hyper.utils.HL;
import ru.threeguns.event.PaymentEvent;
import ru.threeguns.manager.TrackManager;

public abstract class PaymentHandler extends TGHandler {
	@Handle
	protected void onEvent(PaymentEvent event) {
		switch (event.getResult()) {
		case PaymentEvent.SUCCESS:
			onPaymentSuccess(event);
//			Module.of(TrackManager.class).trackEvent(new TrackManager.TrackEvent("pay_success"));
			break;
		case PaymentEvent.USER_CANCEL:
			onUserCancel();
			break;
		case PaymentEvent.FAILED:
			HL.w(">>>>>>>>>>>>>>>>>>PaymentEvent.FAILED:"+event.getResult());
			onPaymentFailed();
			break;
		case PaymentEvent.IN_PROCESS:
			onPaymentInProcess();
			break;
		}
		unregister();
	}

	protected abstract void onPaymentSuccess(PaymentEvent event);

	protected abstract void onPaymentFailed();

	protected abstract void onPaymentInProcess();

	protected abstract void onUserCancel();

}
