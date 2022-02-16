package ru.threeguns.network.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Progress {
	public static final int LOADING = 1;
	public static final int LOGIN = 2;
	public static final int REGISTER = 3;
	public static final int PAYMENT = 4;
	public static final int SEND = 5;
	public static final int RESTORE = 6;
	public static final int BIND = 7;

	int value() default LOADING;
}
