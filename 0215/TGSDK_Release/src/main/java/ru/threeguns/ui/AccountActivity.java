package ru.threeguns.ui;

import android.os.Bundle;

import ru.threeguns.ui.dfs.GuestDialogFragment;
import ru.threeguns.ui.fragments.AountDialogFragment;

public class AccountActivity extends TGFragmentActivity {
	public static final String KT_BIND_ACCOUNT_FRM = "KT_BIND_ACCOUNT_FRM";
	@SuppressWarnings("unused")
	private static final String TAG = AccountActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getBooleanExtra(KT_BIND_ACCOUNT_FRM, false)) {
			preloadFragment(GuestDialogFragment.class);
			changeFragment(GuestDialogFragment.class);
		} else {
			preloadFragment(AountDialogFragment.class);
			changeFragment(AountDialogFragment.class);
		}
	}

}
