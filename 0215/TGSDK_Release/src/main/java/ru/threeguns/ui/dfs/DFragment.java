package ru.threeguns.ui.dfs;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.event.UserLoginEvent;
import ru.threeguns.utils.VerifyUtil;

public class DFragment extends HFragment {
  protected void requestExit(boolean dispatchEvent) {
    if (dispatchEvent) {
      EventManager.instance.dispatch(new UserLoginEvent(UserLoginEvent.USER_CANCEL, null, false));
    }
    Module.of(UIHelper.class).closeProgressDialog();
    if (getActivity() != null) {
      getActivity().finish();
    }
  }

  protected boolean validAccount(String account) {
    String msg = VerifyUtil.verifyAccount(account);
    if (msg != null) {
      showErrorMessage(msg);
      return false;
    }
    return true;
  }

  protected void showErrorMessage(String message) {
    Module.of(UIHelper.class).showToast(message);
  }

  protected boolean validUsername(String username) {
    String msg = VerifyUtil.verifyUsername(username);
    if (msg != null) {
      showErrorMessage(msg);
      return false;
    }
    return true;
  }

  protected boolean validEmail(String email) {
    String msg = VerifyUtil.verifyEmail(email);
    if (msg != null) {
      showErrorMessage(msg);
      return false;
    }
    return true;
  }

  protected boolean validNickname(String nickname) {
    String msg = VerifyUtil.verifyNickname(nickname);
    if (msg != null) {
      showErrorMessage(msg);
      return false;
    }
    return true;
  }

  protected boolean validPassword(String password) {
    String msg = VerifyUtil.verifyPassword(password);
    if (msg != null) {
      showErrorMessage(msg);
      return false;
    }
    return true;
  }
}
