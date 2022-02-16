package com.my;

import kh.hyper.utils.NotProguard;

public class EncryptUtil implements NotProguard {
	static {
		System.loadLibrary("NativeEncrypt");
	}

	public static native String nativeSign(String src, String key);
}
