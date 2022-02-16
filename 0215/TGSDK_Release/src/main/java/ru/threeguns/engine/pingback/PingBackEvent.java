package ru.threeguns.engine.pingback;

public class PingBackEvent {
	private int id;
	private String requestURL;
	private String dataString;

	public PingBackEvent(String requestURL, String dataString) {
		this.requestURL = requestURL;
		this.dataString = dataString;
	}

	public PingBackEvent(int id, String requestURL, String dataString) {
		this.id = id;
		this.requestURL = requestURL;
		this.dataString = dataString;
	}

	public int getId() {
		return id;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public String getDataString() {
		return dataString;
	}

}
