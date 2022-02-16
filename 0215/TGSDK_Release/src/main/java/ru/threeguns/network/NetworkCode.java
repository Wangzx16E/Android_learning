package ru.threeguns.network;

import kh.hyper.utils.NotProguard;

public class NetworkCode implements NotProguard {
	public static final int NO_ERROR = 0;
	public static final int NETWORK_NOT_AVAILABLE = -1;
	public static final int NETWORK_TIMED_OUT = -2;
	public static final int NETWORK_INTERRUPTED = -3;
	public static final int UNKNOWN_SERVER_ERROR = -4;
	public static final int UNKNOWN_HOST_ERROR = -5;
	public static final int PARSE_JSON_ERROR = -6;

	public static final int SERVER_ERROR = -100000;

}
