package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.entity.User;
import ru.threeguns.event.UserLogoutEvent;

public abstract class UserLogoutHandler extends TGHandler {
	@Handle
	protected void onEvent(UserLogoutEvent event) {
		switch (event.getResult()) {
		case UserLogoutEvent.SUCCESS:
			onLogoutSuccess(event.getUser());
			break;
		case UserLogoutEvent.NOT_LOGIN:
			onUserNotLogin();
			break;
		}
		unregister();
	}

	protected abstract void onUserNotLogin();

	protected abstract void onLogoutSuccess(User user);
}
