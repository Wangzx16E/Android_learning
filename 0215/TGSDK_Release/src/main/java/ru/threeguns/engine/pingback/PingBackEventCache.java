package ru.threeguns.engine.pingback;

import android.content.Context;

public interface PingBackEventCache {

	void init(Context context);

	void release();

	PingBackEvent pollEvent();

	void clearEvent();

	void removeEvent(PingBackEvent event);

	void offerEvent(PingBackEvent event);

}
