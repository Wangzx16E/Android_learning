package ru.threeguns.engine.controller;

import ru.threeguns.manager.PaymentManager;

public class Constants {
	public static final String PLATFORM_NAME = "3guns";

	public static String HOST_ADDRESS = "http://sapi.191game.com";
	public static String GAMEDEBUG_HOST_ADDRESS = "http://sapi.191game.com";

	public static String STATISTIC_HOST_ADDRESS = "http://sapi.191game.com";
	public static String DEBUG_STATISTIC_HOST_ADDRESS = "http://sapi.191game.com";

	// SPK
	public static String SPN_REFERRER = "SPN_REFERRER";
	public static String SPK_LOCALE = "SPK_LOCALE";
	public static String SPK_REFERRER = "SPK_REFERRER";

	// custom locale
	public static boolean ENABLE_CUSTOM_LOCALE = false;

	// GCM
	public static boolean GCM_ENABLED = true;

	// api 23 permission
	public static boolean REQUEST_PERMISSION = true;

	// notice
	public static boolean NOTICE_ENABLED = true;
	public static boolean NOTICE_DIALOG_ENABLED = true;

	// ----------------------------------DEBUG----------------------------------//
	public static boolean DEBUG = false;

	// public static String DEBUG_HOST_ADDRESS = "http://192.168.1.129";
	public static String DEBUG_HOST_ADDRESS = "http://sapi.191game.com";
	public static String DEBUG_PAYMENT_MANAGER = "Google";
	public static PaymentManager.PaymentRequest request = null;
}