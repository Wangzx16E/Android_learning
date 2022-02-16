package ru.threeguns.engine.controller;

import java.lang.reflect.Field;

import android.content.res.Resources;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import kh.hyper.utils.NotProguard;

public class TGString extends Module implements NotProguard {
	private static final String PREFIX = "tg_";

	// [START_TAG]
	public static String account_blank;
	public static String account_format_incorrect;
	public static String password_blank;
	public static String password_format_incorrect;
	public static String nickname_blank;
	public static String nickname_format_incorrect;
	public static String email_blank;
	public static String email_format_incorrect;
	public static String new_password_unmatch;
	public static String new_password_not_change;
	public static String network_token_invalid;
	public static String network_unknown_error;
	public static String network_not_available;
	public static String network_timed_out;
	public static String network_parse_json_error;
	public static String network_unknown_server_error;
	public static String network_unknown_client_error;
	public static String network_loading_loading;
	public static String network_loading_login;
	public static String network_loading_pay;
	public static String network_loading_register;
	public static String please_accept_agreement;
	public static String layout_loginhint;
	public static String chooseimg_sizeerror;
	public static String chooseimg_failed;
	// public static String clearcache_pending;
	// public static String clearcache_success_ag;
	// public static String clearcache_success;
	public static String invite_success;
	public static String invite_fail;
	public static String recharge_kb_success;
	public static String recharge_kb_failed;
	public static String checkupdate_title;
	public static String checkupdate_message;
	public static String checkupdate_btn;
	// [END_TAG]

	public void refreshString() {
		Resources res = getContext().getResources();
		Class<?> rClass = null;
		try {
			rClass = Class.forName("ru.threeguns.R");
			HL.w("TGPlatform Init : {}", getContext().getPackageName());
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("TGPlatform Init Failed : Cannot find R class of package : " + getContext().getPackageName());
		}
		Class<?>[] classes = rClass.getClasses();
		Class<?> stringClass = null;
		for (int i = 0; i < classes.length; ++i) {
			if (classes[i].getName().split("\\$")[1].equals("string")) {
				stringClass = classes[i];
				break;
			}
		}

		for (Field field : TGString.class.getFields()) {
			try {
				int id = stringClass.getField(PREFIX + field.getName()).getInt(null);
				field.set(null, res.getString(id));
				HL.w("Init String Success : {}", field.getName());
			} catch (Exception e) {
				e.printStackTrace();
				HL.w("Init String Failed : {}", field.getName());
				try {
					field.set(null, "Unknown String");
				} catch (Exception e1) {
				}
				continue;
			}
		}
	}

	@Override
	protected void onLoad(Parameter parameter) {
		refreshString();
	}

	@Override
	protected void onRelease() {

	}
}
