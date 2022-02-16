package kh.hyper.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Pair;

public class RequestEntity {
	private String url;
	private String httpMethod;
	private Map<String, String> params;
	private List<Pair<String, String>> headers;
	private byte[] body;
	private int timeout;

	private RequestEntity(String url, String httpMethod, Map<String, String> params, List<Pair<String, String>> headers, int timeout, byte[] body) {
		this.url = url;
		this.httpMethod = httpMethod;
		this.params = params;
		this.headers = headers;
		this.timeout = timeout;
		this.body = body;
	}

	public String getUrl() {
		return url;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public List<Pair<String, String>> getHeaders() {
		return headers;
	}

	public int getTimeout() {
		return timeout;
	}

	public byte[] getBody() {
		return body;
	}

	public static class Builder {
		private String url;
		private String httpMethod;
		private Map<String, String> params;
		private List<Pair<String, String>> headers;
		private int timeout;
		private byte[] body;

		public Builder() {
			params = new HashMap<String, String>();
			headers = new ArrayList<Pair<String, String>>();
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder method(String method) {
			this.httpMethod = method;
			return this;
		}

		public Builder params(Map<String, String> params) {
			this.params.putAll(params);
			return this;
		}

		public Builder headers(List<Pair<String, String>> headers) {
			this.headers.addAll(headers);
			return this;
		}

		public Builder timeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder body(byte[] body) {
			this.body = body;
			return this;
		}

		public RequestEntity build() {
			if (url == null) {
				throw new NullPointerException("url cannot be null");
			}
			if (httpMethod == null) {
				throw new NullPointerException("httpMethod cannot be null");
			}
			if (timeout == 0) {
				throw new IllegalArgumentException("timeout must > 0");
			}
			return new RequestEntity(url, httpMethod, params, headers, timeout, body);
		}

	}

}
