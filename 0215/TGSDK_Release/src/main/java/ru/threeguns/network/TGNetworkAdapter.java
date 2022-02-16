package ru.threeguns.network;

import com.squareup.okhttp.Response;

import kh.hyper.core.Module;
import kh.hyper.network.OKHttpAdapter;
import kh.hyper.network.RequestEntity;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.UIHelper;

public class TGNetworkAdapter extends OKHttpAdapter {

	@Override
	public Response connectInternal(RequestEntity request) {
		String progressString = request.getParams().get("progress");
		if (progressString != null) {
			if (progressString.contains("注册")){
				Module.of(UIHelper.class).showProgressDialog("请稍后");
			}else {
				Module.of(UIHelper.class).showProgressDialog(progressString);
			}
			HL.w(">>>>>>>>>>>>>>进度框开启");
		}
		HL.w(">>>>>>>>>>>>>>网络请求"+request.getUrl());
		Response response = super.connectInternal(request);
		HL.w(">>>>>>>>>>>>>>请求完了");
		if (progressString != null) {
			Module.of(UIHelper.class).closeProgressDialog();
			HL.w(">>>>>>>>>>>>>>进度框关闭");
		}
		return response;
	}

}
