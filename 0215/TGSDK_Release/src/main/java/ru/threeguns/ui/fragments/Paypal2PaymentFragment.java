package ru.threeguns.ui.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kh.hyper.event.EventManager;
import ru.threeguns.event.PaymentEvent;

public class Paypal2PaymentFragment extends CommonWebFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		View v = super.onCreateView(inflater, container, savedInstanceState);
		return v;
	}
	@Override
	protected void onExit() {
		super.onExit();
		EventManager.instance.dispatch(new PaymentEvent(PaymentEvent.USER_CANCEL, null));
	}
}
