package ru.threeguns.event.handler;

import kh.hyper.event.EventManager;
import kh.hyper.utils.NotProguard;

public abstract class TGHandler implements NotProguard {
	protected boolean registered = false;

	public void register() {
		if (registered) {
			return;
		}
		EventManager.instance.register(this);
		registered = true;
	}

	public void unregister() {
		if (!registered) {
			return;
		}
		EventManager.instance.unregister(this);
		registered = false;
	}
}
