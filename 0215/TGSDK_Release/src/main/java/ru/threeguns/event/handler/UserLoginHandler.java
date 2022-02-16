package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.entity.User;
import ru.threeguns.event.UserLoginEvent;

public abstract class UserLoginHandler extends TGHandler {
	@Handle
	protected void onEvent(UserLoginEvent event) {
		switch (event.getResult()) {
			case UserLoginEvent.SUCCESS:
				onLoginSuccess(event.getUser(),event.getTgregister());
				break;
			case UserLoginEvent.USER_CANCEL:
				onUserCancel();
				break;
			case UserLoginEvent.FAILED:
				onLoginFailed();
				break;
		}
		unregister();
	}

	protected abstract void onLoginSuccess(User user,boolean tgregister);

	protected abstract void onUserCancel();

	protected abstract void onLoginFailed();


}
