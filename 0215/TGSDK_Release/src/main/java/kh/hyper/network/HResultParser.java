package kh.hyper.network;

import java.io.IOException;

import com.squareup.okhttp.Response;

public class HResultParser implements ResultParser<Response, Result> {

	@Override
	public Result parseResult(Response t) {
		try {
			String data = t.body().string();
			return new Result(t.code(), data, null);
		} catch (IOException e) {
			return new Result(t.code(), null, e);
		}
	}
}
