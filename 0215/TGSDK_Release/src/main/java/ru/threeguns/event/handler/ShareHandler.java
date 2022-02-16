package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.event.ShareEvent;

public abstract class ShareHandler extends TGHandler {
	@Handle()
	private void onUserShare(ShareEvent event) {
		switch (event.getResult()) {
		case ShareEvent.SUCCESS:
			onShareSuccess(event.getData());
			break;
		case ShareEvent.FAILED:
			onShareFailed();
			break;
		case ShareEvent.USER_CANCEL:
			onUserCancel();
			break;
		}
		unregister();
	}

	protected abstract void onShareSuccess(String data);

	protected abstract void onShareFailed();

	protected abstract void onUserCancel();

}
