package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.event.InviteFriendEvent;

public abstract class InviteFriendHandler extends TGHandler {
	@Handle()
	private void onInvite(InviteFriendEvent event) {
		switch (event.getResult()) {
		case InviteFriendEvent.SUCCESS:
			onInviteSuccess(event.getData());
			break;
		case InviteFriendEvent.USER_CANCEL:
			onUserCancel();
			break;
		case InviteFriendEvent.FAILED:
			onInviteFailed();
			break;
		}
		unregister();
	}

	protected abstract void onInviteSuccess(String data);

	protected abstract void onUserCancel();

	protected abstract void onInviteFailed();
}
