package ru.threeguns.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.reflect.Field;

import kh.hyper.core.Module;
import kh.hyper.utils.HUI;
import ru.threeguns.R;
import ru.threeguns.engine.controller.TGController;

public class EditTextEx extends FrameLayout {
  protected View containerView;

  protected ImageView iconImage;
  protected EditText editText;
  protected ImageView clearButton;
  protected ImageView alertButton;

  protected CharSequence hintValue;
  protected int inputType;
  protected int imeOptions;
  protected String imeActionLabel;
  protected Drawable normalIcon;
  protected Drawable selectedIcon;
  protected int maxLength;
  protected String digits;

  protected int iconRightMargin;

  public EditTextEx(Context context, AttributeSet attrs) {
    super(context, attrs);

    int[] ids = getStyleableArray(getContext(), "EditTextEx");
    TypedArray typedArray = context.obtainStyledAttributes(attrs, ids);
    hintValue = typedArray.getString(getStyleable(getContext(), "EditTextEx_hint"));
    inputType = typedArray.getInt(getStyleable(getContext(), "EditTextEx_inputType"), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    imeOptions = typedArray.getInt(getStyleable(getContext(), "EditTextEx_imeOptions"), 1);
    imeActionLabel = typedArray.getString(getStyleable(getContext(), "EditTextEx_imeActionLabel"));
    normalIcon = typedArray.getDrawable(getStyleable(getContext(), "EditTextEx_normalIcon"));
    selectedIcon = typedArray.getDrawable(getStyleable(getContext(), "EditTextEx_selectedIcon"));
    iconRightMargin = typedArray.getInt(getStyleable(getContext(), "EditTextEx_iconRightMargin"), -1);
    maxLength = typedArray.getInt(getStyleable(getContext(), "EditTextEx_maxLength"), -1);
    digits = typedArray.getString(getStyleable(getContext(), "EditTextEx_digits"));

    typedArray.recycle();
    loadView();
    initView();
  }

  public static int[] getStyleableArray(Context context, String name) {
    return (int[]) getResourceId(context, name, "styleable");

  }

  public static int getStyleable(Context context, String name) {
    return ((Integer) getResourceId(context, name, "styleable")).intValue();

  }

  protected void loadView() {
    //		int tg_edittextex_id = getContext().getResources().getIdentifier("tg_edittextex", "layout", getContext().getPackageName());
    containerView = View.inflate(getContext(), R.layout.tg_edittextex, this);
  }

  @SuppressWarnings("deprecation")
  protected void initView() {
    int tg_edittextex_icon_id = getContext().getResources().getIdentifier("tg_edittextex_icon", "id", getContext().getPackageName());
    int tg_edittextex_et_id = getContext().getResources().getIdentifier("tg_edittextex_et", "id", getContext().getPackageName());
    int tg_edittextex_clear_btn_id = getContext().getResources().getIdentifier("tg_edittextex_clear_btn", "id", getContext().getPackageName());
    int tg_edittextex_alert_img_id = getContext().getResources().getIdentifier("tg_edittextex_alert_img", "id", getContext().getPackageName());

    iconImage = (ImageView) containerView.findViewById(tg_edittextex_icon_id);
    editText = (EditText) containerView.findViewById(tg_edittextex_et_id);
    clearButton = (ImageView) containerView.findViewById(tg_edittextex_clear_btn_id);
    alertButton = (ImageView) containerView.findViewById(tg_edittextex_alert_img_id);

    if (iconRightMargin != -1) {
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

    editText.setHint(hintValue);
    editText.setImeActionLabel(imeActionLabel, editText.getImeActionId());
    editText.setImeOptions(imeOptions);
    editText.setInputType(inputType);
    if (maxLength != -1) {
      editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }
    iconImage.setBackgroundDrawable(normalIcon);
    if (!TextUtils.isEmpty(digits)) {
      editText.setKeyListener(new DigitsKeyListener() {

        @Override
        protected char[] getAcceptedChars() {
          return digits.toCharArray();
        }

        @Override
        public int getInputType() {
          return inputType;
        }

      });
    }

    editText.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus && isEnabled()) {
        if (editText.getText().length() > 0) {
          clearButton.setVisibility(View.VISIBLE);
        } else {
          clearButton.setVisibility(View.GONE);
        }
        iconImage.setBackgroundDrawable(selectedIcon);
      } else {
        iconImage.setBackgroundDrawable(normalIcon);
        clearButton.setVisibility(View.GONE);
      }
    });

    editText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (isEnabled()) {
          clearButton.setVisibility(s.length() > 0 && editText.hasFocus() ? View.VISIBLE : View.GONE);
        }
      }
    });

    clearButton.setOnClickListener(v -> editText.post(new Runnable() {
      @Override
      public void run() {
        editText.setText("");
      }
    }));

  }

  private static Object getResourceId(Context context, String name, String type) {
    //		String className = context.getPackageName() + ".R";
    String className = Module.of(TGController.class).packageName + ".R";

    try {
      Class<?> cls = Class.forName(className);
      for (Class<?> childClass : cls.getClasses()) {
        String simple = childClass.getSimpleName();
        if (simple.equals(type)) {
          for (Field field : childClass.getFields()) {
            String fieldName = field.getName();

            if (fieldName.equals(name)) {
              System.out.println(fieldName);
              return field.get(null);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static final int[] getResourceDeclareStyleableIntArray(Context context, String name) {
    try {
      //use reflection to access the resource class
      Field[] fields2 = Class.forName(context.getPackageName() + ".R$styleable").getFields();

      //browse all fields
      for (Field f : fields2) {
        //pick matching field
        if (f.getName().equals(name)) {
          //return as int array
          int[] ret = (int[]) f.get(null);
          return ret;
        }
      }
    } catch (Throwable t) {
    }

    return null;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("EditTextEx_instanceState", super.onSaveInstanceState());
    bundle.putString("EditTextEx_text", this.editText.getText().toString());
    return bundle;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      this.editText.setText(bundle.getString("EditTextEx_text"));
      state = bundle.getParcelable("EditTextEx_instanceState");
    }
    super.onRestoreInstanceState(state);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    Parcelable state = container.get(getId());
    if (state != null) {
      onRestoreInstanceState(state);
    }
  }

  public void alert() {
    clearButton.setVisibility(View.GONE);
    alertButton.setVisibility(View.VISIBLE);
    alertButton.postDelayed(new Runnable() {
      @Override
      public void run() {
        alertButton.setVisibility(View.GONE);
        if (editText.hasFocus() && isEnabled() && editText.getText().length() > 0) {
          clearButton.setVisibility(View.VISIBLE);
        } else {
          clearButton.setVisibility(View.GONE);
        }
      }
    }, 3000);
  }

  public Editable getText() {
    return editText.getText();
  }

  public void setText(CharSequence text) {
    editText.setText(text);
  }

  public void setTextColor(int color) {
    editText.setTextColor(color);
  }

  public void setInputType(int type) {
    editText.setInputType(type);
  }

  public void setHint(CharSequence hint) {
    editText.setHint(hint);
  }

  @SuppressWarnings("deprecation")
  public void setNormalIcon(Drawable d) {
    normalIcon = d;
    iconImage.setBackgroundDrawable(normalIcon);
  }

  public void setSelectedIcon(Drawable d) {
    selectedIcon = d;
  }

  public void addTextChangedListener(TextWatcher watcher) {
    editText.addTextChangedListener(watcher);
  }

  public void setKeyListener(KeyListener input) {
    editText.setKeyListener(input);
    if (input == null) {
      clearButton.setVisibility(View.GONE);
      setEnabled(false);
    }
  }
}
