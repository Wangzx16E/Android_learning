package kh.hyper.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IEvent {
	int type() default CLICK;

	int value();

	int CLICK = 0;
	int LONG_CLICK = 1;
	int TOUCH = 2;
	int FOCUS_CHANGE = 3;
}
