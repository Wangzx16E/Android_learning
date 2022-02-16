package ru.threeguns.ui.fragments;

import kh.hyper.event.EventManager;
import ru.threeguns.event.PaymentEvent;

public class WebPaymentFragment extends CommonWebFragment {
	@Override
	protected void onExit() {
		super.onExit();
		EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.USER_CANCEL, null));
	}
}
