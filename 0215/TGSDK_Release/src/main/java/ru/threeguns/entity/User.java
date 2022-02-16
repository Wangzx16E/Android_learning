package ru.threeguns.entity;

import kh.hyper.utils.NotProguard;

public interface User extends NotProguard {
	public static final String USERTYPE_NORMAL = "1";
	public static final String USERTYPE_GUEST = "2";
	public static final String USERTYPE_VK = "3";
	public static final String USERTYPE_FACEBOOK = "4";
	public static final String USERTYPE_GOOGLE = "5";
	public static final String USERTYPE_PHONE = "6";

	/**
	 * @return 获得用户Id
	 */
	String getUserId();

	/**
	 * @return 获得用户昵称
	 */
	// String getUsername();

	/**
	 * @return 获得登录Token
	 */
	String getToken();

	/**
	 * @return 获得用户类型
	 */
	String getUserType();

	/**
	 * @return 获得用户昵称
	 */
	String getNickname();

}
