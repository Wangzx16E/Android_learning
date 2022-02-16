package ru.threeguns.network;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import kh.hyper.core.Module;
import kh.hyper.network.HRequestGenerator;
import kh.hyper.network.RequestEntity;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.SystemInfo;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.network.annotations.Progress;
import ru.threeguns.network.annotations.WithToken;

public class TGRequestGenerator extends HRequestGenerator {
	@Override
	public RequestEntity generateRequest(Class<?> serverClass, Method method, Object[] args) {
		RequestEntity req = super.generateRequest(serverClass, method, args);

		//请求接口时传入的基础参数
		Map<String, String> params = new HashMap<String, String>();
//		Log.e("TAGccccc", "generateRequest: " + Module.of(TGController.class).appId +" "
//		+ Module.of(TGController.class).appLanguage +" "
//		+ Module.of(TGController.class).channelId +" "
//		+ Module.of(SystemInfo.class).deviceNo +" "
//		+ Module.of(SystemInfo.class).os_name +" "
//		+ TGController.SDK_VERSION +" "
//		+ Module.kernel().getContext().getPackageName() +" "
//		);
		params.put("appid", Module.of(TGController.class).appId);
//		params.put("sdklang", Module.of(TGController.class).appLanguage);
		params.put("sdklang", Module.of(TGController.class).appLanguage);
		params.put("channel_id", Module.of(TGController.class).channelId);
		params.put("deviceNo", Module.of(SystemInfo.class).deviceNo);
		params.put("deviceType", Module.of(SystemInfo.class).os_name);
		params.put("sdk_version", TGController.SDK_VERSION);
		params.put("package_name", Module.kernel().getContext().getPackageName());

		WithToken withToken = method.getAnnotation(WithToken.class);
		if (withToken != null) {
			params.put("user_id", Module.of(UserCenter.class).getUserId());
			params.put("token", Module.of(UserCenter.class).getToken());
		}

		params.putAll(req.getParams());

		HL.i("===========Network Request==========");
		HL.e("===url : {}", req.getUrl());
		HL.i("===params : ");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			HL.e("params {} : {}", entry.getKey(), entry.getValue());
		}
		HL.i("====================================");

		String data = NetworkUtil.makeDataString(params);

		req.getParams().clear();

		Progress progress = method.getAnnotation(Progress.class);
		int progressType = 0;
		if (progress != null) {
			progressType = progress.value();
		}
		req.getParams().put("progress", getProgressString(progressType));

		return new RequestEntity.Builder()//
				.body(data.getBytes())//
				.headers(req.getHeaders())//
				.method(req.getHttpMethod())//
				.timeout(req.getTimeout())//
				.url(req.getUrl())//
				.params(req.getParams())//
				.build();
	}

	private String getProgressString(int type) {
		switch (type) {
		case Progress.LOADING:
			return TGString.network_loading_loading;
		case Progress.LOGIN:
			return TGString.network_loading_login;
		case Progress.REGISTER:
			return TGString.network_loading_register;
		case Progress.PAYMENT:
			return TGString.network_loading_pay;
		default:
			return null;
		}
	}
}
