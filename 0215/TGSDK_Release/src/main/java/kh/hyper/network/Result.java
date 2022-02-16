package kh.hyper.network;

import kh.hyper.utils.NotProguard;

public class Result implements NotProguard {
	private int code;
	private String data;
	private Throwable error;

	public Result(int code, String data, Throwable error) {
		super();
		this.code = code;
		this.data = data;
		this.error = error;
	}

	public int getCode() {
		return code;
	}

	public String getData() {
		return data;
	}

	public Throwable getError() {
		return error;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[")//
				.append("Result")//
				.append(" code:")//
				.append(code)//
				.append(" , data:")//
				.append(data)//
				.append(" , error:")//
				.append(error == null ? "null" : error.getClass().getName())//
				.append("]");
		return builder.toString();
	}

}