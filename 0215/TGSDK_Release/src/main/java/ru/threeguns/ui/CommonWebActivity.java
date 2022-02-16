package ru.threeguns.ui;

import android.os.Bundle;

import kh.hyper.ui.Demand;
import kh.hyper.ui.HFragment;
import ru.threeguns.ui.fragments.CommonWebFragment;

public class CommonWebActivity extends TGFragmentActivity {
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Class<? extends HFragment> fragmentClass = (Class<? extends HFragment>) getIntent().getSerializableExtra("fragment");
		if (fragmentClass == null) {
			fragmentClass = CommonWebFragment.class;
		}
		Demand d = new Demand(fragmentClass);
		d.bundle(getIntent().getExtras());
		changeFragment(d);
	}

//	@TargetApi(23)
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//		if (requestCode == 11727) {
//			checkSelfPermission(Manifest.permission_group.PHONE);
//			shouldShowRequestPermissionRationale("");
//		} else {
//			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//		}
//	}
}
