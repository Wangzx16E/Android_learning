package ru.threeguns.network;

import com.squareup.okhttp.Response;

import kh.hyper.network.ResultParser;

public class TGResultParser implements ResultParser<Response, Response> {
	@Override
	public Response parseResult(Response t) {
		return t;
	}
}
