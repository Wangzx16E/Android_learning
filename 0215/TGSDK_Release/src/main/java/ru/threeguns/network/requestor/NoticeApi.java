package ru.threeguns.network.requestor;

import kh.hyper.network.annotations.Address;
import kh.hyper.network.annotations.P;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.annotations.WithToken;

public interface NoticeApi extends TGApi {
	//通知
	@WithToken
	@Address("/api/notice")
	void get(@P("notice_id") int noticeId, TGResultHandler handler);
}
