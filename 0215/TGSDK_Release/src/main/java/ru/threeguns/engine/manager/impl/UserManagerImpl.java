package ru.threeguns.engine.manager.impl;

import android.app.Activity;
import android.content.Intent;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.AbstractUserManager;
import ru.threeguns.entity.User;
import ru.threeguns.event.SendGiftEvent;
import ru.threeguns.event.handler.SendGiftHandler;
import ru.threeguns.event.handler.UserLoginHandler;
import ru.threeguns.ui.AccountActivity;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.fragments.FeedbackFragment;
import ru.threeguns.utils.ActivityHolder;

public class UserManagerImpl extends AbstractUserManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManagerImpl";

	@Override
	public void requestLogin(Activity activity, UserLoginHandler handler) {
		// if (Module.of(ActivityHolder.class).getTopActivity() instanceof
		// LoginActivity) {
		// return;
		// }
		super.requestLogin(activity, handler);
	}

	@Override
	public void requestSendGift(SendGiftHandler handler) {
		super.requestSendGift(handler);
	}

	@Override
	protected void doLogin() {
		requestLoginInterface();
	}

	@Override
	protected void doLogout() {
		Module.of(UserCenter.class).notifyLogout();
	}

	@Override
	protected void requestLoginInterface() {
		Activity mainActivity = Module.of(ActivityHolder.class).getTopActivity();
		Intent intent = new Intent(mainActivity, LoginActivity.class);
		mainActivity.startActivity(intent);
	}

	@Override
	protected void onLoad(Parameter parameter) {

	}

	@Override
	protected void onRelease() {

	}

	@Override
	public void requestUserCenter() {
		Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), AccountActivity.class);
		Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
	}

	@Override
	public void requestFeedback() {
		Intent intent = new Intent(getContext(), CommonWebActivity.class);
		intent.putExtra("fragment", FeedbackFragment.class);

		getContext().startActivity(intent);
	}

	@Override
	public void requestBindAccount() {
		User activeUser = Module.of(UserCenter.class).getActiveUser();
		if (activeUser != null && !activeUser.getUserType().equals("1")) {
			Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), AccountActivity.class);
			intent.putExtra(AccountActivity.KT_BIND_ACCOUNT_FRM, true);
			Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
		} else {
			Module.of(UIHelper.class).showToast("You're already formal user.");
		}
	}

	@Override
	protected void doSend() {
		User activeUser = Module.of(UserCenter.class).getActiveUser();
		if (activeUser != null && activeUser.getUserType().equals("1")) {
			Module.of(UserCenter.class).notifySendGift(SendGiftEvent.SEND);
		} else {
			Module.of(UserCenter.class).notifySendGift(SendGiftEvent.NOT_NORMAL);
		}
	}

}
