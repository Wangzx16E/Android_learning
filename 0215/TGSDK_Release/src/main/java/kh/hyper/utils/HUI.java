package kh.hyper.utils;

import android.content.Context;

public final class HUI {
	public static int dp2px(Context context, int dp) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dp * density + 0.5f);
	}

	public static float px2dp(Context context, int px) {
		float density = context.getResources().getDisplayMetrics().density;
		return px / density;
	}
}
