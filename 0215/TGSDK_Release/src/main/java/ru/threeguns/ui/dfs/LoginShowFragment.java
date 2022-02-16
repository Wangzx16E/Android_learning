package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import ru.threeguns.R;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.ui.LoginActivity;
import ru.threeguns.utils.DpUtil;

public class LoginShowFragment extends DFragment {
  public final String[] langs = {"en", "zh", "zh", "id", "ru", "es"};
  private String appLanguage;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.tg_activity_login_show, container, false);
    Activity activity = getActivity();
    if (activity instanceof LoginActivity) {
      LoginActivity loginActivity = (LoginActivity) getActivity();
      loginActivity.setBottom();
    }
    initView(v);
    return v;

  }

  @SuppressLint("NewApi")
  private void initView(View v) {
    ImageView sdklogin = (ImageView) v.findViewById(R.id.tg_sdklogin);
    ImageView fastlogin = (ImageView) v.findViewById(R.id.tg_fastlogin);
    ImageView emlogin = (ImageView) v.findViewById(R.id.tg_emlogin);
    String lang = "1";
    if (!TextUtils.isEmpty(lang)) {
      Locale locale = getContext().getResources().getConfiguration().locale;
      appLanguage = locale.getLanguage();
      HL.w(">>>>>>>>>>>>>>>>>>>>>>  Language" + appLanguage);
      for (int i = 1; i <= langs.length; i++) {
        if (appLanguage.startsWith(langs[i - 1])) {
          lang = String.valueOf(i);
          if (langs[i - 1].equals("zh")) {
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
    } else {
      lang = "2";
    }
    appLanguage = lang;
    if ("3".equals(appLanguage) || "2".equals(appLanguage)) {
      sdklogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_cn_phone_login));
      fastlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_cn_fast_login));
      emlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_cn_emlogin_icon));
    } else {
      sdklogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_en_phone_login));
      fastlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_en_fast_login));
      emlogin.setImageDrawable(getContext().getDrawable(R.drawable.tg_emlogin_icon));
    }

    sdklogin.setOnClickListener(v1 -> changeFragment(LoginDialogFragment.class));
    emlogin.setOnClickListener(v12 -> changeFragment(EmLoginDialogFragment.class));//嵌入式登录？
    fastlogin.setOnClickListener(v13 -> changeFragment(FastRegisterDialogFragment.class));
    initThirdplatform(v);
  }


  private void initThirdplatform(View v) {
    //        int tg_login_ll_id = getActivity().getResources().getIdentifier("tg_fragment_show_container", "id", getActivity().getPackageName());
    LinearLayout layout = (LinearLayout) v.findViewById(R.id.tg_fragment_show_container);

    List<ThirdPlatform> platforms = null;
    try {
      platforms = Module.of(ThirdPlatformManager.class).getThirdPlatforms();
    } catch (Exception e) {
      HL.e("initThirdplatform getThirdPlatforms Exception " + e.toString());
    }

    if (platforms == null || platforms.size() == 0) {
      layout.setVisibility(View.GONE);
    } else {
      layout.setVisibility(View.VISIBLE);
      for (int i = 0; i < platforms.size(); i++) {
        final ThirdPlatform platform = platforms.get(i);
        if (platform.isLoginEnabled()) {
          ImageView imageView = new ImageView(v.getContext());
          imageView.setImageResource(platform.getLoginIcon());
          imageView.setAdjustViewBounds(true);
          LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(DpUtil.dp2px(250), LinearLayout.LayoutParams.WRAP_CONTENT);
          param.topMargin = DpUtil.dp2px(10);
          param.leftMargin = DpUtil.dp2px(30);
          param.rightMargin = DpUtil.dp2px(30);
          imageView.setLayoutParams(param);
          imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              platform.requestLogin();
            }
          });
          layout.addView(imageView);
        }
      }
    }

  }

  @Override
  protected void onBack() {
    return;
  }
}
