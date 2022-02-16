package ru.threeguns.network;

import android.util.Log;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import kh.hyper.network.ResultHandler;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGString;

public abstract class TGResultHandler implements ResultHandler<Response> {
	private static final String TAG = "TGResultHandler";
	private JSONObject json;

	@Override
	public void onResult(Response response) {
		int httpCode = response.code();
		Log.e("Network Result","");
		Log.e("===code : {}", httpCode+"");
		if (httpCode == 200) {
			try {
				String data = response.body().string();
				HL.w("===body : {}", data);
				HL.w("===================================");
				Log.e(TAG, "onResult body: " + data );
				json = new JSONObject(data);
				int code = json.getInt("code");
				JSONObject dataJson = json.optJSONObject("data");
				if (code == NetworkCode.NO_ERROR) {
					onSuccess(dataJson);
				} else {
					onFailedExtra(code, json.optString("desc"), dataJson);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "onResult: JSONException" );
				onFailed(NetworkCode.PARSE_JSON_ERROR, TGString.network_parse_json_error);
			} catch (IOException e) {
				e.printStackTrace();
				HL.w("===error : IOException");
				HL.w("===================================");
				onFailed(NetworkCode.UNKNOWN_HOST_ERROR, TGString.network_unknown_error);
			}
		} else {
			String data = "[null]";
			try {
				ResponseBody body = response.body();
				data = body == null ? "[empty body]" : body.string();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e(TAG, "onResult: " + data );
			onFailed(NetworkCode.UNKNOWN_HOST_ERROR, TGString.network_unknown_server_error + response.code());
		}
	}

	protected String require(String p) throws JSONException {
		return json.getString(p);
	}

	protected abstract void onSuccess(JSONObject json) throws JSONException;

	protected abstract void onFailed(int errorCode, String msg);

	protected void onFailedExtra(int errorCode, String msg, JSONObject json) {
		onFailed(errorCode, msg);
	}
}
