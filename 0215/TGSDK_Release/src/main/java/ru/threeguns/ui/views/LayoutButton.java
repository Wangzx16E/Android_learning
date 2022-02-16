package ru.threeguns.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class LayoutButton extends RelativeLayout {

	public LayoutButton(Context context) {
		super(context);
	}

	public LayoutButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LayoutButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

}
