package ru.threeguns.event;

import kh.hyper.utils.NotProguard;
import ru.threeguns.entity.Notice;

public final class NewNoticeEvent implements NotProguard {
	private Notice notice;

	public Notice getNotice() {
		return notice;
	}

	public NewNoticeEvent(Notice notice) {
		super();
		this.notice = notice;
	}

}
