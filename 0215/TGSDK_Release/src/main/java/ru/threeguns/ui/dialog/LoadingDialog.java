package ru.threeguns.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ru.threeguns.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.tg_progress_dialog,null,false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        setCanceledOnTouchOutside(false);
        addContentView(view,params);
    }
}
