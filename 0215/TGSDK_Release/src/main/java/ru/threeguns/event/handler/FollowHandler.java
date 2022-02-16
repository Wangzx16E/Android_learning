package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.event.FollowEvent;

public abstract class FollowHandler extends TGHandler {
	@Handle()
	private void onFollow(FollowEvent event) {
		switch (event.getResult()) {
		case FollowEvent.SUCCESS:
			onFollowSuccess(event.getData());
			break;
		case FollowEvent.FAILED:
			onFollowFailed();
			break;
		case FollowEvent.USER_CANCEL:
			onUserCancel();
			break;
		}
		unregister();
	}

	protected abstract void onFollowSuccess(String data);

	protected abstract void onFollowFailed();

	protected abstract void onUserCancel();

}
