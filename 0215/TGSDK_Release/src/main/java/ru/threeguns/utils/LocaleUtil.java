package ru.threeguns.utils;

import java.util.Locale;

public class LocaleUtil {
	public static final String FOLLOW_SYSTEM = "DEFAULT";

	public static Locale fromString(String str) {
		if (FOLLOW_SYSTEM.equals(str)) {
			return Locale.getDefault();
		} else if ("th".equals(str)) {
			return new Locale("th");
		} else if ("zh-rCN".equals(str)) {
			return Locale.SIMPLIFIED_CHINESE;
		} else if ("zh-rTW".equals(str)) {
			return Locale.TRADITIONAL_CHINESE;
		} else if ("ru".equals(str)) {
			return new Locale("ru");
		} else {
			return Locale.US;
		}
	}
}
