package kh.hyper.network;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.util.Pair;

public class OKHttpAdapter implements NetworkAdapter<RequestEntity, Response> {
	private static OkHttpClient okHttpClient;

	protected synchronized OkHttpClient getClient() {
		if (okHttpClient == null) {
			okHttpClient = new OkHttpClient();
		}
		return okHttpClient;
	}

	@Override
	public Response connectInternal(final RequestEntity request) {
		getClient().setConnectTimeout(request.getTimeout(), TimeUnit.SECONDS);

		Request.Builder builder = new Request.Builder();
		for (Pair<String, String> pair : request.getHeaders()) {
			builder.addHeader(pair.first, pair.second);
		}
		builder.url(request.getUrl());
		if ("GET".equals(request.getHttpMethod())) {
			builder.get();
		} else if ("DELETE".equals(request.getHttpMethod())) {
			builder.delete();
		} else {
			if (request.getBody() != null) {
				builder.method(request.getHttpMethod(), RequestBody.create(null, request.getBody()));
			} else {
				FormEncodingBuilder f = new FormEncodingBuilder();
				for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
					f.add(entry.getKey(), entry.getValue());
				}
				builder.method(request.getHttpMethod(), f.build());
			}
		}

		Call call = okHttpClient.newCall(builder.build());

		try {
			Response response = call.execute();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return new Response.Builder()//
					.request(new Request.Builder().url("http://failed").build())//
					.code(7)//
					.body(null)//
					.protocol(Protocol.HTTP_1_1)//
					.message(e.getLocalizedMessage())//
					.build();
		}

	}

}
