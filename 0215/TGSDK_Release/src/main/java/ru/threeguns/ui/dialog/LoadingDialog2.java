package ru.threeguns.ui.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.threeguns.R;

public class LoadingDialog2 extends Dialog {
    private TextView copy,cancel;
    private Context context;
    public LoadingDialog2(@NonNull Context context) {
        super(context, R.style.VK_Transparent);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_load2,null,false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        setCanceledOnTouchOutside(false);
        initView(view);
        addContentView(view,params);
    }


    private void initView(View view){
        copy = view.findViewById(R.id.copy);
        cancel = view.findViewById(R.id.cancel);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData	 = ClipData.newPlainText("Label","红狐游戏");
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                Toast.makeText(getContext(),context.getResources().getString(R.string.tg_layout_copy),Toast.LENGTH_LONG).show();
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
