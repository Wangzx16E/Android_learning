package ru.threeguns.ui.views;

import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;


public class CheckBoxWrapper {
	protected boolean checked;
	protected ImageView mCheckImage;
	protected OnCheckedChangeListener mOnCheckedChangeListener;

	public CheckBoxWrapper(View layout, ImageView checkImage, Boolean cheked) {
		mCheckImage = checkImage;
		this.checked = cheked;
		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				checked = !checked;
				if (mOnCheckedChangeListener != null) {
					mOnCheckedChangeListener.onCheckedChanged(null, checked);
				}
				updateCheckImage();
			}
		});
		updateCheckImage();
	}

	protected void updateCheckImage() {
        int tg_checkbox_checked_id = this.mCheckImage.getContext().getResources().getIdentifier("tg_checkbox_checked", "drawable", this.mCheckImage.getContext().getPackageName());
        int tg_checkbox_normal_id = this.mCheckImage.getContext().getResources().getIdentifier("tg_checkbox_normal", "drawable", this.mCheckImage.getContext().getPackageName());

		mCheckImage.setImageResource(checked ? tg_checkbox_checked_id : tg_checkbox_normal_id);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		mCheckImage.post(new Runnable() {
			@Override
			public void run() {
				updateCheckImage();
			}
		});
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

}
