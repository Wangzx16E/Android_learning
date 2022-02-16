package ru.threeguns.ui.toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.event.HandleThreadType;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.NoticeManager;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.engine.manager.ToolbarManager;
import ru.threeguns.event.NewNoticeEvent;
import ru.threeguns.event.ReadNoticeEvent;
import ru.threeguns.ui.AccountActivity;

@SuppressLint("ClickableViewAccessibility")
public class ToolbarView extends RelativeLayout {
  private static final long FADE_TOOLBARIMG_COUNTDOWN = 5 * 1000;
  private static final long HIDE_TOOLBARIMG_COUNTDOWN = 5 * 1000;

  private static final float hide_rate = 0.5f;
  // private static final float TOUCH_RANGE_RATE = 0.5f;
  private static final int STATE_IDLE = 0;
  private static final int STATE_HIDE = 1;
  private static final int STATE_MENU = 2;
  private int state = STATE_IDLE;

  // toolbar image size in px
  private int toolbarSize;

  private RelativeLayout toolbarLayout;
  private LinearLayout menuLayout;
  private ImageView toolbarImage;
  private RelativeLayout toolbarImageLayout;
  private View redhintView;
  private View usercenterRedhintView;

  private RelativeLayout hideHintLayout;
  private ImageView hideHintButton;
  private TextView hideHintText;
  private LinearLayout hideHintRect;

  private float layoutX;
  private float layoutY;
  private float lastX;
  private float lastY;
  private float lastLayoutX;
  private float lastLayoutY;
  private float lastRawX;
  private float lastRawY;

  private boolean clickFlag;
  private int clickRange;

  private List<View> iconViewList;

  private volatile int hideToolbarVersion;

  private Runnable hideToolbarImgCallback;
  private Runnable fadeToolbarImgCallback;

  private Window window;

  public ToolbarView(Context context, Window window) {
    super(context);
    int tg_toolbar_layout_id = getContext().getResources().getIdentifier("tg_toolbar_layout", "layout", getContext().getPackageName());
    LayoutInflater.from(context).inflate(tg_toolbar_layout_id, this);

    this.window = window;

    int tg_toolbar_layout_id_id = getContext().getResources().getIdentifier("tg_toolbar_layout", "id", getContext().getPackageName());
    int tg_toolbar_img_id = getContext().getResources().getIdentifier("tg_toolbar_img", "id", getContext().getPackageName());
    toolbarLayout = (RelativeLayout) findViewById(tg_toolbar_layout_id_id);
    toolbarImage = (ImageView) findViewById(tg_toolbar_img_id);

    if (toolbarImage == null) {
      new AlertDialog.Builder(context).setTitle("Error").setMessage("Init failed.Please restart the game.")
          .setPositiveButton("Exit", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              System.exit(-1);
              android.os.Process.killProcess(android.os.Process.myPid());
            }
          }).create().show();
      return;
    }

    int tg_tb_size_id = getContext().getResources().getIdentifier("tg_tb_size", "dimen", getContext().getPackageName());
    toolbarSize = getContext().getResources().getDimensionPixelSize(tg_tb_size_id);
    HL.w("toolbarSize = {}", toolbarSize);

    int tg_toolbar_img_layout_id = getContext().getResources().getIdentifier("tg_toolbar_img_layout", "id", getContext().getPackageName());
    toolbarImageLayout = (RelativeLayout) findViewById(tg_toolbar_img_layout_id);

    int tg_toolbar_menu_layout_id = getContext().getResources().getIdentifier("tg_toolbar_menu_layout", "id", getContext().getPackageName());
    menuLayout = (LinearLayout) findViewById(tg_toolbar_menu_layout_id);
    menuLayout.setVisibility(View.GONE);

    int tg_toolbar_hide_layout_id = getContext().getResources().getIdentifier("tg_toolbar_hide_layout", "id", getContext().getPackageName());
    int tg_toolbar_hide_btn_id = getContext().getResources().getIdentifier("tg_toolbar_hide_btn", "id", getContext().getPackageName());
    int tg_toolbar_hide_hint_id = getContext().getResources().getIdentifier("tg_toolbar_hide_hint", "id", getContext().getPackageName());
    int tg_toolbar_hide_rect_id = getContext().getResources().getIdentifier("tg_toolbar_hide_rect", "id", getContext().getPackageName());
    hideHintLayout = (RelativeLayout) findViewById(tg_toolbar_hide_layout_id);
    hideHintButton = (ImageView) findViewById(tg_toolbar_hide_btn_id);
    hideHintText = (TextView) findViewById(tg_toolbar_hide_hint_id);
    hideHintRect = (LinearLayout) findViewById(tg_toolbar_hide_rect_id);

    int tg_toolbar_redhint_id = getContext().getResources().getIdentifier("tg_toolbar_redhint", "id", getContext().getPackageName());
    int tg_usercenter_redhint_id = getContext().getResources().getIdentifier("tg_usercenter_redhint", "id", getContext().getPackageName());
    redhintView = findViewById(tg_toolbar_redhint_id);
    usercenterRedhintView = findViewById(tg_usercenter_redhint_id);
    redhintView.setVisibility(Module.of(NoticeManager.class).haveNewNotice() ? View.VISIBLE : View.GONE);
    usercenterRedhintView.setVisibility(Module.of(NoticeManager.class).haveNewNotice() ? View.VISIBLE : View.GONE);

    clickRange = ViewConfiguration.get(context).getScaledTouchSlop();

    toolbarLayout.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (menuLayout.getVisibility() == View.VISIBLE) {
          return false;
        }
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            // if (state == STATE_HIDE) {
            // HL.w("event.getX() = {}",event.getX());
            // HL.w("toolbarSize * TOUCH_RANGE_RATE = {}",toolbarSize *
            // TOUCH_RANGE_RATE);
            // if (layoutX < getWidth() / 2) {
            // if (event.getX() > toolbarSize * TOUCH_RANGE_RATE) {
            // HL.w("not in click range , return true");
            // return true;
            // }
            // } else {
            // if (event.getX() < toolbarSize * TOUCH_RANGE_RATE) {
            // HL.w("not in click range , return true");
            // return true;
            // }
            // }
            // }
            clickFlag = true;
            toolbarImage.clearAnimation();
            AlphaAnimation anim = new AlphaAnimation(0.45f, 1.0f);
            anim.setFillAfter(true);
            toolbarLayout.startAnimation(anim);

            stopHideCountDown();

            lastX = event.getX();
            lastY = event.getY();
            lastRawX = event.getRawX();
            lastRawY = event.getRawY();
            lastLayoutX = layoutX;
            lastLayoutY = layoutY;

            hideToolbarVersion++;

            if (state == STATE_HIDE) {
              toolbarLayout.clearAnimation();
              state = STATE_IDLE;
            }

            break;
          case MotionEvent.ACTION_MOVE:
            float currentX = event.getX();
            float currentY = event.getY();
            float currentRawX = event.getRawX();
            float currentRawY = event.getRawY();

            if (Math.abs(currentRawX - lastRawX) > clickRange) {
              clickFlag = false;
              hideHintLayout.setVisibility(View.VISIBLE);
            }
            if (Math.abs(currentRawY - lastRawY) > clickRange) {
              clickFlag = false;
              hideHintLayout.setVisibility(View.VISIBLE);
            }

            layoutX += (currentX - lastX);
            layoutY += (currentY - lastY);

            if (layoutX < 0) {
              layoutX = 0;
            }
            if (layoutX > getWidth() - toolbarSize) {
              layoutX = getWidth() - toolbarSize;
            }
            if (layoutY < 0) {
              layoutY = 0;
            }
            if (layoutY > getHeight() - toolbarSize) {
              layoutY = getHeight() - toolbarSize;
            }

            Rect r = new Rect();
            hideHintRect.getGlobalVisibleRect(r);
            float rx = event.getRawX();
            float ry = event.getRawY();
            if (r.contains((int) rx, (int) ry)) {
              hideHintText.setTextColor(Color.YELLOW);
              int tg_hidehint_btn_trigger_id = getContext().getResources().getIdentifier("tg_hidehint_btn_trigger", "drawable", getContext().getPackageName());
              hideHintButton.setImageResource(tg_hidehint_btn_trigger_id);
            } else {
              hideHintText.setTextColor(Color.WHITE);
              int tg_hidehint_btn_id = getContext().getResources().getIdentifier("tg_hidehint_btn", "drawable", getContext().getPackageName());
              hideHintButton.setImageResource(tg_hidehint_btn_id);
            }

            updateViewPosition();
            break;
          case MotionEvent.ACTION_UP:
            float dx = Math.abs(layoutX - lastLayoutX);
            float dy = Math.abs(layoutY - lastLayoutY);
            int halfScreen = getWidth() / 2;

            currentRawX = event.getRawX();
            currentRawY = event.getRawY();

            hideHintLayout.setVisibility(View.GONE);

            if ((currentRawX - lastRawX) < -toolbarSize / 4 && lastLayoutX < 10) {
              AlphaAnimation anim1 = new AlphaAnimation(1.0f, 0.45f);
              anim1.setFillAfter(true);
              toolbarLayout.startAnimation(anim1);
              stopHideCountDown();
              hideToolbarImage();
              return true;
            } else if ((currentRawX - lastRawX) > toolbarSize / 4 && lastLayoutX > getWidth() - toolbarSize - 10) {
              AlphaAnimation anim2 = new AlphaAnimation(1.0f, 0.45f);
              anim2.setFillAfter(true);
              toolbarLayout.startAnimation(anim2);
              stopHideCountDown();
              hideToolbarImage();
              return true;
            }

            if (clickFlag && dx < clickRange && dy < clickRange) {
              return false;
            } else {
              state = STATE_IDLE;

              Rect ar = new Rect();
              hideHintRect.getGlobalVisibleRect(ar);
              float arx = event.getRawX();
              float ary = event.getRawY();
              if (ar.contains((int) arx, (int) ary)) {
                confirmHideToolbar();

                return true;
              }

              if (layoutX < halfScreen) {
                layoutX = 0;
              }
              if (layoutX > halfScreen) {
                layoutX = halfScreen * 2 - toolbarSize;
              }
              updateViewPosition();
              startFadeCountDown();

              return true;
            }
          default:
            break;
        }
        return false;
      }
    });

    toolbarLayout.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (state == STATE_HIDE) {
          showToolbarImage();
        } else if (menuLayout.getVisibility() == View.GONE) {
          showMenuLayout();
        } else {
          hideMenuLayout();
        }
      }
    });

    setClickable(false);
    setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (menuLayout.getVisibility() == View.VISIBLE) {
          Rect r = new Rect();
          menuLayout.getGlobalVisibleRect(r);
          float x = event.getRawX();
          float y = event.getRawY();
          if (!r.contains((int) x, (int) y)) {
            hideMenuLayout();
          }
        }
        return false;
      }
    });

    layoutX = ((RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams()).leftMargin;
    layoutY = ((RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams()).topMargin;


    //		findViewById(R.id.tg_gift_btn).setOnClickListener(new View.OnClickListener() {
    //			@Override
    //			public void onClick(View v) {
    //				hideMenuLayout();
    //				Intent intent = new Intent(getContext(), CommonWebActivity.class);
    //				intent.putExtra("fragment", GiftFragment.class);
    //
    //				getContext().startActivity(intent);
    //			}
    //		});
    //
    //		findViewById(R.id.tg_feedback_btn).setOnClickListener(new View.OnClickListener() {
    //			@Override
    //			public void onClick(View v) {
    //				hideMenuLayout();
    //				Intent intent = new Intent(getContext(), CommonWebActivity.class);
    //				intent.putExtra("fragment", FeedbackFragment.class);
    //
    //				getContext().startActivity(intent);
    //			}
    //		});

    int tg_usercenter_btn_id = getContext().getResources().getIdentifier("tg_usercenter_btn", "id", getContext().getPackageName());
    int tg_toolbar_divider3_id = getContext().getResources().getIdentifier("tg_toolbar_divider3", "id", getContext().getPackageName());
    int tg_exit_btn_id = getContext().getResources().getIdentifier("tg_exit_btn", "id", getContext().getPackageName());

    findViewById(tg_usercenter_btn_id).setOnClickListener(v -> {
			hideMenuLayout();
			Intent intent = new Intent(getContext(), AccountActivity.class);
			getContext().startActivity(intent);
		});

    findViewById(tg_exit_btn_id).setOnClickListener(v -> {
			hideMenuLayout();
			Module.of(UserCenter.class).notifyLogoutInternel();
		});

    iconViewList = new ArrayList<View>();
    iconViewList.add(findViewById(tg_usercenter_btn_id));
    //		iconViewList.add(findViewById(R.id.tg_toolbar_divider1));
    //		iconViewList.add(findViewById(R.id.tg_feedback_btn));
    //		iconViewList.add(findViewById(R.id.tg_toolbar_divider2));
    //		iconViewList.add(findViewById(R.id.tg_gift_btn));
    iconViewList.add(findViewById(tg_toolbar_divider3_id));
    iconViewList.add(findViewById(tg_exit_btn_id));

    state = STATE_IDLE;

    hideToolbarImgCallback = new Runnable() {
      @Override
      public void run() {
        hideToolbarImage();
      }
    };

    fadeToolbarImgCallback = new Runnable() {
      @Override
      public void run() {
        Animation anim = new AlphaAnimation(1.0f, 0.45f);
        anim.setDuration(800);
        anim.setFillAfter(true);
        toolbarLayout.startAnimation(anim);
        startHideCountDown();
      }
    };

    startFadeCountDown();
  }

  protected void resetToolbarPos() {
    Module.of(UIHelper.class).post2MainThread(new Runnable() {
      public void run() {
        layoutX = 0;
        layoutY = 150;
        updateViewPosition();

        // stopHideCountDown();
        startHideCountDown();
      }
    });
  }

  protected boolean confirmHideToolbar() {
    Module.of(ToolbarManager.class).turnOnFlipFunction();
    if (!TextUtils.isEmpty(Module.of(SPCache.class).load("SPK_NOT_SHOW_CONFIRM_HIDE_HINT"))) {
      Module.of(ToolbarManager.class).hideToolbar();
      resetToolbarPos();
      return true;
    }
    Module.of(UIHelper.class).post2MainThread(new Runnable() {
      public void run() {
        final AlertDialog d = new AlertDialog.Builder(getContext()).setCancelable(false).create();
        d.show();

        int tg_hide_hint_dialog_id = getContext().getResources().getIdentifier("tg_hide_hint_dialog", "layout", getContext().getPackageName());
        int tg_checkbox_id = getContext().getResources().getIdentifier("tg_checkbox", "id", getContext().getPackageName());
        int tg_checkbox_desc_id = getContext().getResources().getIdentifier("tg_checkbox_desc", "id", getContext().getPackageName());

        d.setContentView(tg_hide_hint_dialog_id);
        final CheckBox cb = (CheckBox) d.findViewById(tg_checkbox_id);
        d.findViewById(tg_checkbox_desc_id).setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            cb.setChecked(!cb.isChecked());
          }
        });
        int tg_hide_hint_sure_id = getContext().getResources().getIdentifier("tg_hide_hint_sure", "id", getContext().getPackageName());
        d.findViewById(tg_hide_hint_sure_id).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            d.dismiss();
            Module.of(ToolbarManager.class).hideToolbar();
            resetToolbarPos();
            if (cb.isChecked()) {
              Module.of(SPCache.class).save("SPK_NOT_SHOW_CONFIRM_HIDE_HINT", "true");
            }
          }
        });
        int tg_hide_hint_cancel_id = getContext().getResources().getIdentifier("tg_hide_hint_cancel", "id", getContext().getPackageName());
        d.findViewById(tg_hide_hint_cancel_id).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            d.dismiss();
            resetToolbarPos();
          }
        });
        int tg_hide_hint_img_id = getContext().getResources().getIdentifier("tg_hide_hint_img", "id", getContext().getPackageName());
        AnimationDrawable anim = (AnimationDrawable) ((ImageView) d.findViewById(tg_hide_hint_img_id)).getDrawable();
        anim.start();
      }
    });
    return true;
  }

  protected void startHideCountDown() {
    postDelayed(hideToolbarImgCallback, HIDE_TOOLBARIMG_COUNTDOWN);
  }

  public void notifyShow() {
    state = STATE_IDLE;
    toolbarImage.clearAnimation();
    toolbarLayout.clearAnimation();
    // AlphaAnimation anim = new AlphaAnimation(0.45f, 1.0f);
    // anim.setFillAfter(true);
    // toolbarLayout.startAnimation(anim);

    stopHideCountDown();
    startFadeCountDown();
    // measureView(this);
    // if (state == STATE_IDLE) {
    // hideToolbarImage();
    // }
  }

  protected void stopHideCountDown() {
    removeCallbacks(fadeToolbarImgCallback);
    removeCallbacks(hideToolbarImgCallback);
  }

  protected void startFadeCountDown() {
    postDelayed(fadeToolbarImgCallback, FADE_TOOLBARIMG_COUNTDOWN);
  }

  @Handle(HandleThreadType.MAIN)
  protected void onNewNotice(NewNoticeEvent e) {
    redhintView.setVisibility(View.VISIBLE);
    usercenterRedhintView.setVisibility(View.VISIBLE);
  }

  @Handle(HandleThreadType.MAIN)
  protected void onReadNotice(ReadNoticeEvent e) {
    redhintView.setVisibility(View.GONE);
    usercenterRedhintView.setVisibility(View.GONE);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    EventManager.instance.register(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    EventManager.instance.unregister(this);
  }

  private void showMenuLayout() {
    hideToolbarVersion++;
    state = STATE_MENU;

    toolbarImage.clearAnimation();
    // AlphaAnimation anim = new AlphaAnimation(0.45f, 1.0f);
    // anim.setFillAfter(true);
    // toolbarLayout.startAnimation(anim);
    menuLayout.setVisibility(View.VISIBLE);

    // ----------------------------------------

    // boolean left = layoutX < getWidth() / 2;
    // int duration = 300;
    // toolbarImage.clearAnimation();
    // AlphaAnimation anim = new AlphaAnimation(0.45f, 1.0f);
    // anim.setFillAfter(true);
    // toolbarLayout.startAnimation(anim);
    // menuLayout.setVisibility(View.VISIBLE);
    //
    // for (int i = 0, sz = iconViewList.size(); i < sz; i++) {
    // View icon = iconViewList.get(i);
    // float xOffset = left ? -(i + 1) : (sz - i);
    // TranslateAnimation tAnim = new
    // TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, xOffset, //
    // TranslateAnimation.RELATIVE_TO_SELF, 0, //
    // TranslateAnimation.RELATIVE_TO_SELF, 0, //
    // TranslateAnimation.RELATIVE_TO_SELF, 0);
    // tAnim.setDuration(duration);
    // tAnim.setDuration((left ? (i + 1) : (sz - i)) * duration / 4);
    // tAnim.setStartOffset((left ? i : (sz - i - 1)) * duration / 8);
    // icon.startAnimation(tAnim);
    // }

    // Animation anim = AnimationUtils.loadAnimation(getContext(),
    // R.anim.tg_tblayout_show);
    // menuLayout.startAnimation(anim);

    updateMenuLayoutDirection();
    updateLeftMargin();

  }

  // @SuppressWarnings("deprecation")
  private void hideMenuLayout() {
    // toolbarImage.setAlpha(153);
    updateMenuLayoutDirection();

    menuLayout.setVisibility(View.GONE);
    updateLeftMargin();

    state = STATE_IDLE;

    // Animation anim = AnimationUtils.loadAnimation(getContext(),
    // R.anim.tg_tblayout_hide);
    // anim.setAnimationListener(new Animation.AnimationListener() {
    //
    // @Override
    // public void onAnimationStart(Animation animation) {
    //
    // }
    //
    // @Override
    // public void onAnimationRepeat(Animation animation) {
    //
    // }
    //
    // @Override
    // public void onAnimationEnd(Animation animation) {
    // menuLayout.setVisibility(View.GONE);
    // updateLeftMargin();
    //
    // state = STATE_IDLE;
    // // hideToolbarImage();
    // }
    // });
    // menuLayout.startAnimation(anim);

    startFadeCountDown();
  }

  private void showToolbarImage() {
    hideToolbarVersion++;
    state = STATE_IDLE;
    post(new Runnable() {
      public void run() {
        Animation anim = null;
        float x = layoutX;
        if (x < getWidth() / 2) {
          anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, -hide_rate, //
              TranslateAnimation.RELATIVE_TO_SELF, 0, //
              TranslateAnimation.RELATIVE_TO_SELF, 0, //
              TranslateAnimation.RELATIVE_TO_SELF, 0);//
        } else {
          anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, hide_rate, //
              TranslateAnimation.RELATIVE_TO_SELF, 0, //
              TranslateAnimation.RELATIVE_TO_SELF, 0, //
              TranslateAnimation.RELATIVE_TO_SELF, 0);//
        }
        anim.setDuration(200);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {

          @Override
          public void onAnimationStart(Animation animation) {

          }

          @Override
          public void onAnimationEnd(Animation animation) {
            showMenuLayout();
          }

          @Override
          public void onAnimationRepeat(Animation animation) {

          }
        });
        toolbarImageLayout.startAnimation(anim);
      }
    });
  }

  private void hideToolbarImage() {
    float x = layoutX;
    final AnimationSet set = new AnimationSet(true);
    final Animation hideToolbarAnimation;
    if (x < getMeasuredWidth() / 2) {
      hideToolbarAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, //
          TranslateAnimation.RELATIVE_TO_SELF, -hide_rate, //
          TranslateAnimation.RELATIVE_TO_SELF, 0, //
          TranslateAnimation.RELATIVE_TO_SELF, 0);//
    } else {
      hideToolbarAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, //
          TranslateAnimation.RELATIVE_TO_SELF, hide_rate, //
          TranslateAnimation.RELATIVE_TO_SELF, 0, //
          TranslateAnimation.RELATIVE_TO_SELF, 0);//
    }
    hideToolbarAnimation.setDuration(200);
    hideToolbarAnimation.setFillAfter(true);

    AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.45f);
    alphaAnim.setFillAfter(true);

    set.addAnimation(hideToolbarAnimation);
    set.addAnimation(alphaAnim);

    set.setFillAfter(true);

    final int v = hideToolbarVersion;
    postDelayed(new Runnable() {
      public void run() {
        if (hideToolbarVersion == v) {
          state = STATE_HIDE;
          toolbarLayout.startAnimation(set);
        }
      }
    }, 3);
  }

  private void updateMenuLayoutDirection() {
    float x = layoutX;
    if (x < getWidth() / 2) {
      RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) menuLayout.getLayoutParams();
      int tg_toolbar_img_layout_id = getContext().getResources().getIdentifier("tg_toolbar_img_layout", "id", getContext().getPackageName());
      p.addRule(RelativeLayout.RIGHT_OF, tg_toolbar_img_layout_id);
      menuLayout.setLayoutParams(p);

      p = (RelativeLayout.LayoutParams) toolbarImageLayout.getLayoutParams();
      p.addRule(RelativeLayout.RIGHT_OF, 0);
      toolbarImageLayout.setLayoutParams(p);

    } else {
      RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) menuLayout.getLayoutParams();
      p.addRule(RelativeLayout.RIGHT_OF, 0);
      menuLayout.setLayoutParams(p);

      p = (RelativeLayout.LayoutParams) toolbarImageLayout.getLayoutParams();
      int tg_toolbar_menu_layout_id = getContext().getResources().getIdentifier("tg_toolbar_menu_layout", "id", getContext().getPackageName());
      p.addRule(RelativeLayout.RIGHT_OF, tg_toolbar_menu_layout_id);
      toolbarImageLayout.setLayoutParams(p);
    }
  }

  private void measureView(View view) {
    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    view.measure(widthMeasureSpec, heightMeasureSpec);
  }

  private void updateLeftMargin() {
    float x = layoutX;
    if (x < getWidth() / 2) {
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams();
      params.leftMargin = 0;
      toolbarLayout.setLayoutParams(params);
    } else {
      if (menuLayout.getVisibility() == View.VISIBLE) {
        measureView(menuLayout);
        float width = toolbarSize + menuLayout.getMeasuredWidth();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.leftMargin = (int) (getMeasuredWidth() - width);
        toolbarLayout.setLayoutParams(params);
      } else {
        float width = toolbarSize;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.leftMargin = (int) (getMeasuredWidth() - width);
        toolbarLayout.setLayoutParams(params);
      }
    }
  }

  private void updateViewPosition() {
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarLayout.getLayoutParams();
    params.leftMargin = (int) layoutX;
    params.topMargin = (int) layoutY;
    toolbarLayout.setLayoutParams(params);
  }

  public Window getWindow() {
    return window;
  }

}
