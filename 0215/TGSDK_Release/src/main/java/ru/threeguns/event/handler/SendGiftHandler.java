package ru.threeguns.event.handler;

import kh.hyper.event.Handle;
import ru.threeguns.entity.User;
import ru.threeguns.event.SendGiftEvent;

public abstract class SendGiftHandler extends TGHandler {
	@Handle
	protected void onEvent(SendGiftEvent event) {
		switch (event.getResult()) {
		case SendGiftEvent.SEND:
			onSendGift(event.getUser());
			break;
		case SendGiftEvent.NOT_NORMAL:
			onUserNotNormal();
			break;
		}
		unregister();
	}

	protected abstract void onUserNotNormal();

	protected abstract void onSendGift(User user);
}
