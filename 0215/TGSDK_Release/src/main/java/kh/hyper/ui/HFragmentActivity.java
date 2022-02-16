//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kh.hyper.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import kh.hyper.utils.HL;

public class HFragmentActivity extends FragmentActivity {
  private static final int DEFAULT_FRAGMENT_CONTAINER_ID = 0x77777777;
  protected int fragmentContainerId;
  protected View containerView;
  protected FragmentManager fragmentManager;
  protected Stack<Fragment> fragmentStack;
  protected boolean cacheFragmentInstance = true;
  protected Map<Class<? extends Fragment>, Fragment> fragmentInstanceCache;
  protected InputMethodManager imm;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();
    fragmentManager = getSupportFragmentManager();

    fragmentStack = new Stack<Fragment>();
    cacheFragmentInstance = true;
    fragmentInstanceCache = new HashMap<Class<? extends Fragment>, Fragment>();
    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
  }

  protected void initView() {
    FrameLayout layout = new FrameLayout(this);
    layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    FrameLayout container = new FrameLayout(this);
    containerView = container;
    FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    param.gravity = Gravity.CENTER;
    container.setLayoutParams(param);
    container.setId(DEFAULT_FRAGMENT_CONTAINER_ID);
    layout.addView(container);
    fragmentContainerId = DEFAULT_FRAGMENT_CONTAINER_ID;
    setContentView(layout);
  }

  public void changeFragment(Class<? extends Fragment> fragmentClass) {
    this.changeFragment(new Demand(fragmentClass));
  }

  public void changeFragment(Demand demand) {
    Fragment targetFragment = null;
    int position = -1;

    for (int i = fragmentStack.size() - 1; i >= 0; i--) {
      if (fragmentStack.get(i).getClass() == demand.fragmentClass) {
        targetFragment = fragmentStack.get(i);
        break;
      }
    }

    if (position != -1) {
      fragmentStack.remove(position);
    }

    if (targetFragment == null) {
      if (cacheFragmentInstance && fragmentInstanceCache.containsKey(demand.fragmentClass)) {
        targetFragment = fragmentInstanceCache.get(demand.fragmentClass);
      } else {
        try {
          targetFragment = (Fragment) demand.fragmentClass.newInstance();
        } catch (InstantiationException e) {
          HL.w("Create Fragment Insatance throws InstantiationException : ");
          HL.w(e.getMessage());
        } catch (IllegalAccessException e) {
          HL.w("Create Fragment Insatance throws IllegalAccessException : ");
          HL.w(e.getMessage());
        }

        if (cacheFragmentInstance) {
          fragmentInstanceCache.put(demand.fragmentClass, targetFragment);
        }
      }
    }

    if (demand.clear()) {
      fragmentStack.clear();
      fragmentStack.add(targetFragment);
    } else {
      fragmentStack.add(targetFragment);
    }

    imm.hideSoftInputFromWindow(containerView.getWindowToken(), 0);
    targetFragment.setArguments(demand.bundle);
    replaceFragmentInternel(targetFragment, demand);
  }

  protected void replaceFragmentInternel(Fragment fragment, Demand demand) {
    fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(fragmentContainerId, fragment).commit();
  }

  public void requestBack() {
    this.fragmentStack.pop();
    if (this.fragmentStack.size() == 0) {
      this.onExit();
      this.finish();
    } else {
      Fragment s = (Fragment) this.fragmentStack.lastElement();
      this.replaceFragmentInternel(s, (new Demand(s.getClass())).back(true));
    }

  }

  protected void onExit() {
  }

  protected void preloadFragment(Class<? extends Fragment> fragmentClass) {
    if (!this.cacheFragmentInstance) {
      HL.w("cacheFragmentInstance is false , preloadFragment will be ignored.");
    }

    try {
      Fragment s = (Fragment) fragmentClass.newInstance();
      fragmentInstanceCache.put(fragmentClass, s);
    } catch (InstantiationException e) {
      HL.w("Preload Fragment Insatance throws InstantiationException : ");
      HL.w(e.getMessage());
    } catch (IllegalAccessException e) {
      HL.w("Preload Fragment Insatance throws IllegalAccessException : ");
      HL.w(e.getMessage());
    }

  }

  @Override
  public void onBackPressed() {
    Fragment s = fragmentStack.peek();
    if (s != null) {
      if (s instanceof HFragment) {
        ((HFragment) s).onBack();
      }
    } else {
      HL.w("Unexpected.Cannot get the first Fragment.");
    }

  }
}
