package ru.threeguns.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import kh.hyper.utils.HUI;
import ru.threeguns.R;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.ui.dfs.FastRegisterDialogFragment;
import ru.threeguns.ui.dfs.LoginDialogFragment;

public class TgLoginDialog extends Dialog {
    private String appLanguage;
    private LoginActivity activity;
    private Context context;
    public final String[] langs = {"en", "zh", "zh", "id", "ru", "es"};
    public TgLoginDialog(@NonNull Context context, LoginActivity activity) {
        super(context,R.style.tg_transparent_dialog);
        this.activity = activity;
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.tg_activity_login_show,null,false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        initView(v);
        setContentView(v,params);
    }

    @SuppressLint("NewApi")
    private void initView(View v){

        ImageView sdklogin = (ImageView) v.findViewById(R.id.tg_sdklogin);
        ImageView fastlogin = (ImageView) v.findViewById(R.id.tg_fastlogin);
        String lang = "1";
        if (!TextUtils.isEmpty(lang)) {
            Locale locale = getContext().getResources().getConfiguration().locale;
            appLanguage = locale.getLanguage();
            HL.w(">>>>>>>>>>>>>>>>>>>>>>  Language" + appLanguage);
            for (int i = 1; i <= langs.length; i++) {
                if (appLanguage.startsWith(langs[i-1])) {
                    lang = String.valueOf(i);
                    if (langs[i-1].equals("zh")) {
                        HL.w(">>>>>>>>>>>>>>>>>>>>>>   Country" + locale.getCountry());
                        if (locale.getCountry().indexOf("cn") == 0 || locale.getCountry().indexOf("CN") == 0) {
                            lang = "2";
                        } else {
                            lang = "3";
                        }
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(lang)) {
                lang = "1";
            }
        }else{
            lang = "2";
        }
        appLanguage = lang;
        if ("3".equals(appLanguage) || "2".equals(appLanguage)){
            sdklogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_cn_phone_login));
            fastlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_cn_fast_login));
        }else{
            sdklogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_en_phone_login));
            fastlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_en_fast_login));

        }

        sdklogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeFragment(LoginDialogFragment.class);
                dismiss();
            }
        });


        fastlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeFragment(FastRegisterDialogFragment.class);
                dismiss();
            }
        });

        initThirdplatform(v);
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void initThirdplatform(View v) {
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.tg_fragment_show_container);

        List<ThirdPlatform> platforms = null;
        try {
            platforms = Module.of(ThirdPlatformManager.class).getThirdPlatforms();
        } catch (Exception e) {
            HL.e("initThirdplatform getThirdPlatforms Exception " + e.toString());
        }

        if (platforms == null || platforms.size() == 0) {
            layout.setVisibility(View.GONE);
            Window window = getWindow();
            window.setGravity(Gravity.BOTTOM);
        } else {
            layout.setVisibility(View.VISIBLE);
            Window window = getWindow();
            window.setGravity(Gravity.CENTER);
            for (int i = 0; i < platforms.size(); i++) {
                final ThirdPlatform platform = platforms.get(i);
                if (platform.isLoginEnabled()) {
                    ImageView imageView = new ImageView(v.getContext());
                    imageView.setImageResource(platform.getIconResource());
                    imageView.setAdjustViewBounds(true);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    param.rightMargin = HUI.dp2px(context, 10);
                    param.topMargin = HUI.dp2px(context, 10);
                    param.weight = 1;
                    imageView.setLayoutParams(param);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            platform.requestLogin();
                        }
                    });
                    layout.addView(imageView,0);
                }
            }
        }

    }

}
