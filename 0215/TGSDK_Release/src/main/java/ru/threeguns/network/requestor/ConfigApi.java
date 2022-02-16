package ru.threeguns.network.requestor;

import kh.hyper.network.annotations.Address;
import kh.hyper.network.annotations.P;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.annotations.Progress;

public interface ConfigApi extends TGApi {
	//初始配置请求
	@Progress(Progress.LOADING)
	@Address("/api/config")
	void config(TGResultHandler handler);

	//更新
	@Progress(Progress.LOADING)
	@Address("/api/check_update")
	void checkUpdate(@P("current_version") String version, TGResultHandler handler);
}
