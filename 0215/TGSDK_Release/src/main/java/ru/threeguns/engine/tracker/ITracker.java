package ru.threeguns.engine.tracker;

import kh.hyper.utils.NotProguard;
import ru.threeguns.manager.TrackManager.TrackEvent;

public interface ITracker extends NotProguard {
	void trackEvent(TrackEvent event);
}
