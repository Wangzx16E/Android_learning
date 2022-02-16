package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.network.HClient;
import kh.hyper.ui.Demand;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.TGString;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.NoticeManager;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.requestor.UserApi;
import ru.threeguns.ui.dfs.GuestUpgradeDialogFragment;
import ru.threeguns.ui.dfs.RestorePasswordDialogFragment;
import ru.threeguns.ui.views.LayoutButton;

public class AccountFragment extends HFragment {
  private TextView accountText;

  private TextView nicknameHint;
  private TextView nicknameText;
  private EditText nicknameEditText;
  private TextView setNicknameButton;
  private TextView changeNicknameHint;
  // private LinearLayout setNicknameLayout;

  private LayoutButton upgradeBtn;
  private LayoutButton changePasswordBtn;

  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  	Activity activity = getActivity();
  	Resources res = activity.getResources();
  	String packageName = activity.getPackageName();
  	
    int tg_fragment_account_id = res.getIdentifier("tg_fragment_account", "layout", packageName);
    View v = inflater.inflate(tg_fragment_account_id, null);

    int tg_back_btn_id = res.getIdentifier("tg_back_btn", "id", packageName);
    v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().finish();
      }
    });

    int tg_account_text_id = res.getIdentifier("tg_account_text", "id", packageName);
    int tg_nickname_hint_id = res.getIdentifier("tg_nickname_hint", "id", packageName);
    int tg_nickname_text_id = res.getIdentifier("tg_nickname_text", "id", packageName);
    int tg_nickname_et_id = res.getIdentifier("tg_nickname_et", "id", packageName);
    int tg_setnickname_btn_id = res.getIdentifier("tg_setnickname_btn", "id", packageName);
    int tg_changenickname_hint_id = res.getIdentifier("tg_changenickname_hint", "id", packageName);

    accountText = v.findViewById(tg_account_text_id);
    nicknameHint = v.findViewById(tg_nickname_hint_id);
    nicknameText = v.findViewById(tg_nickname_text_id);
    nicknameEditText = v.findViewById(tg_nickname_et_id);
    setNicknameButton = v.findViewById(tg_setnickname_btn_id);
    changeNicknameHint = v.findViewById(tg_changenickname_hint_id);

    int tg_upgrade_btn_id = res.getIdentifier("tg_upgrade_btn", "id", packageName);
    int tg_changepw_btn_id = res.getIdentifier("tg_changepw_btn", "id", packageName);
    upgradeBtn = v.findViewById(tg_upgrade_btn_id);
    changePasswordBtn = v.findViewById(tg_changepw_btn_id);

    if (Module.of(UserCenter.class).getActiveUser() != null && User.USERTYPE_NORMAL.equals(Module.of(UserCenter.class).getActiveUser().getUserType())) {
      changePasswordBtn.setVisibility(View.VISIBLE);
      upgradeBtn.setVisibility(View.GONE);
    } else {
      changePasswordBtn.setVisibility(View.GONE);
      upgradeBtn.setVisibility(View.VISIBLE);
    }

    int tg_newnotice_hint_id = res.getIdentifier("tg_newnotice_hint", "id", packageName);
    TextView newNoticeHint = v.findViewById(tg_newnotice_hint_id);
    if (Module.of(NoticeManager.class).haveNewNotice()) {
      newNoticeHint.setVisibility(View.VISIBLE);
    } else {
      newNoticeHint.setVisibility(View.GONE);
    }

    upgradeBtn.setOnClickListener(v13 -> changeFragment(GuestUpgradeDialogFragment.class));
    changePasswordBtn.setOnClickListener(v14 -> changeFragment(RestorePasswordDialogFragment.class));

    //		v.findViewById(R.id.tg_myfeedback_btn).setOnClickListener(new View.OnClickListener() {
    //			@Override
    //			public void onClick(View v) {
    //				changeFragment(FeedbackFragment.class);
    //			}
    //		});

    setNicknameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String nickname = nicknameEditText.getText().toString();

        if (TextUtils.isEmpty(nickname)) {
          Module.of(UIHelper.class).showToast(TGString.nickname_blank);
          return;
        }

        HClient.of(UserApi.class).setNickname(nickname, new TGResultHandler() {

          @Override
          protected void onSuccess(JSONObject json) throws JSONException {
            Module.of(UserCenter.class).notifySetNickname(nickname);
            Module.of(UIHelper.class).post2MainThread(new Runnable() {
              @Override
              public void run() {
                refreshUserInfo();
              }
            });
          }

          @Override
          protected void onFailed(int errorCode, String msg) {
            Module.of(UIHelper.class).showToast(msg);
          }
        });

      }
    });

    int tg_notice_btn_id = getActivity().getResources().getIdentifier("tg_notice_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_notice_btn_id).setOnClickListener(v1 -> changeFragment(NoticeFragment.class));

    int tg_logout_btn_id = getActivity().getResources().getIdentifier("tg_logout_btn", "id", getActivity().getPackageName());
    v.findViewById(tg_logout_btn_id).setOnClickListener(v12 -> {
			Module.of(UserCenter.class).notifyLogoutInternel();
			requestFinish();
		});

    int tg_wallet_btn_id = getActivity().getResources().getIdentifier("tg_wallet_btn", "id", getActivity().getPackageName());
    View wallet = v.findViewById(tg_wallet_btn_id);

    wallet.setVisibility(View.GONE);

    int tg_fbfans_btn_id = getActivity().getResources().getIdentifier("tg_fbfans_btn", "id", getActivity().getPackageName());
    View fbfans = v.findViewById(tg_fbfans_btn_id);
    ThirdPlatform fb = Module.of(ThirdPlatformManager.class).getThirdPlatformByName("Facebook");
    if (fb != null && !TextUtils.isEmpty(fb.__getExtraConfig("fbfans"))) {
      final String fbFansUrl = fb.__getExtraConfig("fbfans");
      fbfans.setVisibility(View.VISIBLE);
      fbfans.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Bundle b = new Bundle();
          b.putString("target_url", fbFansUrl);

          changeFragment(new Demand(CommonWebFragment.class).bundle(b));
        }
      });
    } else {
      fbfans.setVisibility(View.GONE);
    }

    int tg_vkfans_btn_id = getActivity().getResources().getIdentifier("tg_vkfans_btn", "id", getActivity().getPackageName());
    View vkfans = v.findViewById(tg_vkfans_btn_id);
    ThirdPlatform vk = Module.of(ThirdPlatformManager.class).getThirdPlatformByName("VK");
    if (vk != null && !TextUtils.isEmpty(vk.__getExtraConfig("vkfans"))) {
      final String vkFansUrl = vk.__getExtraConfig("vkfans");
      vkfans.setVisibility(View.VISIBLE);
      vkfans.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Bundle b = new Bundle();
          b.putString("target_url", vkFansUrl);

          changeFragment(new Demand(CommonWebFragment.class).bundle(b));
        }
      });
    } else {
      vkfans.setVisibility(View.GONE);
    }

    return v;
  }

  /**
   * 刷新用户信息
   */
  private void refreshUserInfo() {
    if (Module.of(UserCenter.class).getActiveUser() != null) {
      accountText.setText(Module.of(UserCenter.class).getActiveUser().getUsername());

      if (TextUtils.isEmpty(Module.of(UserCenter.class).getNickname())) {
        nicknameHint.setVisibility(View.GONE);
        nicknameText.setVisibility(View.GONE);

        nicknameEditText.setVisibility(View.VISIBLE);
        setNicknameButton.setVisibility(View.VISIBLE);
        changeNicknameHint.setVisibility(View.VISIBLE);
      } else {
        Resources res = getActivity().getResources();

        if (User.USERTYPE_VK.equals(Module.of(UserCenter.class).getUserType())) {
          int tg_layout_vk_nickname_hint_id = getActivity().getResources().getIdentifier("tg_layout_vk_nickname_hint", "string", getActivity().getPackageName());
          nicknameHint.setText(res.getString(tg_layout_vk_nickname_hint_id));
        } else {
          int tg_layout_nickname_hint_id = getActivity().getResources().getIdentifier("tg_layout_nickname_hint", "string", getActivity().getPackageName());
          nicknameHint.setText(res.getString(tg_layout_nickname_hint_id));
        }
        nicknameHint.setVisibility(View.VISIBLE);
        nicknameText.setVisibility(View.VISIBLE);
        nicknameText.setText(Module.of(UserCenter.class).getNickname());

        nicknameEditText.setVisibility(View.GONE);
        setNicknameButton.setVisibility(View.GONE);
        changeNicknameHint.setVisibility(View.GONE);
      }
    } else {
      getActivity().finish();
    }
  }

  @Override
  public void onEnter() {
    super.onEnter();

    Module.of(UIHelper.class).post2MainThread(new Runnable() {
      @Override
      public void run() {
        refreshUserInfo();
      }
    });
  }

}
