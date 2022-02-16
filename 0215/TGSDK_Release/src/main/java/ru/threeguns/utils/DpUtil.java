package ru.threeguns.utils;

import ru.threeguns.engine.controller.TGApplication;

public class DpUtil {

    private static float scale;

    static {
        scale = TGApplication.tgApplication.getResources().getDisplayMetrics().density;
    }

    public static int dp2px(int dpVal) {
        return (int) (scale * dpVal + 0.5f);
    }
}
