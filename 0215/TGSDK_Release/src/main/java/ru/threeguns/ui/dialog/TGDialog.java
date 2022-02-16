package ru.threeguns.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

public class TGDialog extends Dialog {
	protected DialogListener listener;

	public TGDialog(Context context) {
		super(context);
	}

	public TGDialog listener(DialogListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);
	}

}
