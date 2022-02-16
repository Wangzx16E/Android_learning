package ru.threeguns.engine.tp;

import android.text.TextUtils;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import ru.threeguns.manager.ShareManager.ResultHandler;

public abstract class ThirdPlatform extends Module implements IThirdPlatform {
	protected boolean loginEnabled = true;

	@Override
	protected void onLoad(Parameter parameter) {
		String s = parameter.optString("login_enabled");
		if (!TextUtils.isEmpty(s) && s.equals("false")) {
			loginEnabled = false;
		}
	}

	@Override
	public void requestExtra(Parameter parameter, ResultHandler handler) {
		handler.onFailed("NotSupport");
	}

	public String __getExtraConfig(String key) {
		return null;
	}

	@Override
	public boolean isLoginEnabled() {
		return loginEnabled;
	}

}
