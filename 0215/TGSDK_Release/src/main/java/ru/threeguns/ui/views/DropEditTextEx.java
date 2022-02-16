package ru.threeguns.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import kh.hyper.core.Module;
import kh.hyper.utils.HUI;
import ru.threeguns.engine.controller.UIHelper;

public class DropEditTextEx extends EditTextEx {
  private final ImageView dropBtn;
  private boolean drop = false;
  private View.OnClickListener onDropClickListener;

  public DropEditTextEx(Context context, AttributeSet attrs) {
    super(context, attrs);

    int tg_drop_btn_id = getContext().getResources().getIdentifier("tg_drop_btn", "id", getContext().getPackageName());
    dropBtn = (ImageView) containerView.findViewById(tg_drop_btn_id);

    dropBtn.setOnClickListener(v -> {
			if (drop) {
				drop = false;
				Module.of(UIHelper.class).post2MainThread(new Runnable() {

					@Override
					public void run() {
						int tg_arrow_down_id = getContext().getResources().getIdentifier("tg_arrow_down", "drawable", getContext().getPackageName());
						dropBtn.setImageResource(tg_arrow_down_id);
					}

				});
				if (onDropClickListener != null) {
					onDropClickListener.onClick(dropBtn);
				}
			} else {
				drop = true;
				Module.of(UIHelper.class).post2MainThread(new Runnable() {

					@Override
					public void run() {
						int tg_arrow_up_id = getContext().getResources().getIdentifier("tg_arrow_up", "drawable", getContext().getPackageName());
						dropBtn.setImageResource(tg_arrow_up_id);
					}

				});
				if (onDropClickListener != null) {
					onDropClickListener.onClick(dropBtn);
				}
			}
		});
  }

  public boolean isDrop() {
    return drop;
  }

  public void hideDropButton() {
    dropBtn.setVisibility(View.GONE);
    iconRightMargin = 8;
    FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) clearButton.getLayoutParams();
    p.rightMargin = HUI.dp2px(getContext(), iconRightMargin);
    clearButton.setLayoutParams(p);
    p = (FrameLayout.LayoutParams) alertButton.getLayoutParams();
    p.rightMargin = HUI.dp2px(getContext(), iconRightMargin);
    alertButton.setLayoutParams(p);
    p = (FrameLayout.LayoutParams) editText.getLayoutParams();
    p.rightMargin += HUI.dp2px(getContext(), iconRightMargin);
    editText.setLayoutParams(p);
  }

  public void packupDropButton() {
    Module.of(UIHelper.class).post2MainThread(new Runnable() {

      @Override
      public void run() {
        int tg_arrow_down_id = getContext().getResources().getIdentifier("tg_arrow_down", "drawable", getContext().getPackageName());
        dropBtn.setImageResource(tg_arrow_down_id);
      }

    });
    Module.of(UIHelper.class).post2MainThreadDelayed(new Runnable() {
      @Override
      public void run() {
        drop = false;
      }
    }, 500);
  }

  @Override
  protected void loadView() {
    int tg_dropedittextex_id = getContext().getResources().getIdentifier("tg_dropedittextex", "layout", getContext().getPackageName());
    containerView = View.inflate(getContext(), tg_dropedittextex_id, this);
  }

  public View.OnClickListener getOnDropClickListener() {
    return onDropClickListener;
  }

  public void setOnDropClickListener(View.OnClickListener onDropClickListener) {
    this.onDropClickListener = onDropClickListener;
  }

}
