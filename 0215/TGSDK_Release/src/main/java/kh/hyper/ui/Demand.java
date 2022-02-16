package kh.hyper.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class Demand {
  public static final int MODE_NORMAL = 0;

  public static final int MODE_CLEAR = 1;

  public static final int MODE_BACK = 2;

  protected Class<? extends Fragment> fragmentClass;

  protected int mode;

  protected Bundle bundle;

  public Demand(Class<? extends Fragment> fragmentClass) {
    this.mode = MODE_NORMAL;
    this.fragmentClass = fragmentClass;
  }

  public Demand fragment(Class<? extends Fragment> fragmentClass) {
    this.fragmentClass = fragmentClass;
    return this;
  }

  public Demand bundle(Bundle bundle) {
    this.bundle = bundle;
    return this;
  }

  public boolean clear() {
    return ((this.mode & MODE_CLEAR) != 0);
  }

  public Demand clear(boolean b) {
    if (b) {
      this.mode |= MODE_CLEAR;
    } else {
      this.mode &= ~MODE_CLEAR;
    }
    return this;
  }

  public boolean back() {
    return ((this.mode & MODE_BACK) != 0);
  }

  public Demand back(boolean b) {
    if (b) {
      this.mode |= MODE_BACK;
    } else {
      this.mode &= ~MODE_BACK;
    }
    return this;
  }
}
