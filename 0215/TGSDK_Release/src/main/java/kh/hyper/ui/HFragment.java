package kh.hyper.ui;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

public class HFragment extends Fragment {
  public HFragment() {
  }

  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    this.onEnter();
  }

  public void onDestroyView() {
    super.onDestroyView();
    this.onExit();
  }

  protected void onExit() {}

  protected void onEnter() {}

  protected void requestFinish() {
    this.getActivity().finish();
  }

  protected void onBack() {
    this.requestBack();
  }

  protected void requestBack() {
    ((HFragmentActivity) this.getActivity()).requestBack();
  }

  protected void changeFragment(Class<? extends Fragment> fragmentClass) {
    ((HFragmentActivity) this.getActivity()).changeFragment(fragmentClass);
  }

  protected void changeFragment(Demand demand) {
    ((HFragmentActivity) this.getActivity()).changeFragment(demand);
  }
}
