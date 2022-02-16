package ru.threeguns.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public final class MD5Util {
	public static String md5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		byte[] md5Bytes;
		try {
			md5Bytes = md5.digest(str.getBytes("UTF-8"));
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16) {
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}

	public static String md5_16(String str) {
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		return md5(str).substring(8, 24);
	}
}
