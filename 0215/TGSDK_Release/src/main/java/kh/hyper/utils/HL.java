package kh.hyper.utils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class HL {
	public static final int LOG_LEVEL_INFO = 1;
	public static final int LOG_LEVEL_DEBUG = 2;
	public static final int LOG_LEVEL_WARNING = 3;
	public static final int LOG_LEVEL_ERROR = 4;
	public static final int LOG_LEVEL_NONE = 5;
	public static final int LOG_LEVEL_ALL = LOG_LEVEL_INFO;

	public static boolean LOG_FLAG = true;
	public static int LOG_LEVEL = LOG_LEVEL_ALL;

	private static List<Logger> loggers = new ArrayList<Logger>();

	static {
		HL.addLogger(new Logger() {
			@Override
			public void log(int logLevel, String tag, String message) {
				switch (logLevel) {
				case LOG_LEVEL_INFO:
					Log.i(tag, message);
					break;
				case LOG_LEVEL_DEBUG:
					Log.d(tag, message);
					break;
				case LOG_LEVEL_WARNING:
					Log.w(tag, message);
					break;
				case LOG_LEVEL_ERROR:
					Log.e(tag, message);
					break;
				default:
					Log.e(tag, message);
					break;
				}
			}
		});
	}

	public static void addLogger(Logger logger) {
		loggers.add(logger);
	}

	public static <T> void i(T t) {
		log(LOG_LEVEL_INFO, t.toString());
	}

	public static void i(String message, Object... args) {
		if (args.length == 0) {
			log(LOG_LEVEL_INFO, message);
		} else {
			log(LOG_LEVEL_INFO, formatMessage(message, args));
		}
	}

	public static <T> void d(T t) {
		log(LOG_LEVEL_DEBUG, t.toString());
	}

	public static void d(String message, Object... args) {
		if (args.length == 0) {
			log(LOG_LEVEL_DEBUG, message);
		} else {
			log(LOG_LEVEL_DEBUG, formatMessage(message, args));
		}
	}

	public static <T> void w(T t) {
		log(LOG_LEVEL_WARNING, t.toString());
	}

	public static void w(String message, Object... args) {
		if (args.length == 0) {
			log(LOG_LEVEL_WARNING, message);
		} else {
			log(LOG_LEVEL_WARNING, formatMessage(message, args));
		}
	}

	public static <T> void e(T t) {
		log(LOG_LEVEL_ERROR, t.toString());
	}

	public static void e(String message, Object... args) {
		if (args.length == 0) {
			log(LOG_LEVEL_ERROR, message);
		} else {
			log(LOG_LEVEL_ERROR, formatMessage(message, args));
		}
	}

	private static String formatMessage(String message, Object... args) {
		String msg = new String(message);
		for (int i = 0, sz = args.length; i < sz; i++) {
			int index = msg.indexOf("{}");
			if (index != -1) {
				msg = msg.substring(0, index) + (args[i] == null ? "[null]" : args[i].toString()) + msg.substring(index + 2, msg.length());
			} else {
				break;
			}
		}
		return msg;
	}

	public static void log(int logLevel, String message) {
		if (LOG_FLAG && logLevel >= LOG_LEVEL) {
			for (Logger logger : loggers) {
				logger.log(logLevel, getTag(), message);
			}
		}
	}

	private static String getTag() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		if (elements != null && elements.length >= 6 && elements[5] != null) {
			String className = Thread.currentThread().getStackTrace()[5].getClassName();
			return "redfox-"+className.substring(className.lastIndexOf(".") + 1);
		}
		return "redfox-UNKNOWN";
	}

	public static interface Logger {
		void log(int logLevel, String tag, String message);
	}

}
