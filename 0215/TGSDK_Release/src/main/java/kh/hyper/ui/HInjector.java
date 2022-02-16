package kh.hyper.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kh.hyper.ui.annotations.IContainer;
import kh.hyper.ui.annotations.IEvent;
import kh.hyper.ui.annotations.IView;
import kh.hyper.utils.HL;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class HInjector {
	public static void inject(final Activity activity) {
		IContainer containerAnnotation = activity.getClass().getAnnotation(IContainer.class);
		if (containerAnnotation != null) {
			if (containerAnnotation.fullscreen()) {
				activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			if (containerAnnotation.no_title()) {
				activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
			}
			if (containerAnnotation.value() != View.NO_ID) {
				View view = View.inflate(activity, containerAnnotation.value(), null);
				activity.setContentView(view);
			}
		}

		for (Field field : activity.getClass().getDeclaredFields()) {
			IView annotation = field.getAnnotation(IView.class);
			if (annotation == null) {
				continue;
			}
			int id = annotation.value();
			View view = activity.findViewById(id);
			if (view == null) {
				HL.w("Cannot init view : {} , findViewById return null.", field.getName());
				continue;
			}
			field.setAccessible(true);
			try {
				field.set(activity, view);
			} catch (Exception e) {
				HL.w("Init view failed : {}", field.getName());
				HL.w(e.getMessage());
			}
		}
		Map<View, List<Method>> clickMethodMap = new HashMap<View, List<Method>>();
		Map<View, List<Method>> longClickMethodMap = new HashMap<View, List<Method>>();
		Map<View, List<Method>> touchMethodMap = new HashMap<View, List<Method>>();
		Map<View, List<Method>> focusChangeMethodMap = new HashMap<View, List<Method>>();
		for (Method method : activity.getClass().getDeclaredMethods()) {
			IEvent annotation = method.getAnnotation(IEvent.class);
			if (annotation == null) {
				continue;
			}
			int id = annotation.value();
			View view = activity.findViewById(id);
			if (view == null) {
				HL.w("Cannot init event : {} , findViewById return null.", method.getName());
				continue;
			}
			Map<View, List<Method>> targetMap = null;
			switch (annotation.type()) {
			case IEvent.CLICK:
				targetMap = clickMethodMap;
				break;
			case IEvent.LONG_CLICK:
				targetMap = longClickMethodMap;
				break;
			case IEvent.TOUCH:
				targetMap = touchMethodMap;
				break;
			case IEvent.FOCUS_CHANGE:
				targetMap = focusChangeMethodMap;
				break;
			}
			List<Method> methodList = targetMap.get(view);
			if (methodList == null) {
				methodList = new ArrayList<Method>();
				targetMap.put(view, methodList);
			}
			methodList.add(method);
		}

		for (Map.Entry<View, List<Method>> entry : clickMethodMap.entrySet()) {
			final View view = entry.getKey();
			final List<Method> methodList = entry.getValue();
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (Method method : methodList) {
						method.setAccessible(true);
						try {
							method.invoke(activity, v);
						} catch (Exception e) {
							HL.w("Invoke method failed : {}", method.getName());
							HL.w(e.getMessage());
						}
					}
				}
			});
		}

		for (Map.Entry<View, List<Method>> entry : longClickMethodMap.entrySet()) {
			final View view = entry.getKey();
			final List<Method> methodList = entry.getValue();
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					for (Method method : methodList) {
						method.setAccessible(true);
						try {
							method.invoke(activity, v);
						} catch (Exception e) {
							HL.w("Invoke method failed : {}", method.getName());
							HL.w(e.getMessage());
						}
					}
					return false;
				}

			});
		}

		for (Map.Entry<View, List<Method>> entry : touchMethodMap.entrySet()) {
			final View view = entry.getKey();
			final List<Method> methodList = entry.getValue();
			view.setOnTouchListener(new View.OnTouchListener() {
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					for (Method method : methodList) {
						method.setAccessible(true);
						try {
							method.invoke(activity, v, event);
						} catch (Exception e) {
							HL.w("Invoke method failed : {}", method.getName());
							HL.w(e.getMessage());
						}
					}
					return false;
				}
			});
		}

		for (Map.Entry<View, List<Method>> entry : focusChangeMethodMap.entrySet()) {
			final View view = entry.getKey();
			final List<Method> methodList = entry.getValue();
			view.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					for (Method method : methodList) {
						method.setAccessible(true);
						try {
							method.invoke(activity, v, hasFocus);
						} catch (Exception e) {
							HL.w("Invoke method failed : {}", method.getName());
							HL.w(e.getMessage());
						}
					}
				}
			});
		}
	}
}
