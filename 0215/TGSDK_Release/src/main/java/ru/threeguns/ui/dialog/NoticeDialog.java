package ru.threeguns.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import kh.hyper.core.Module;
import ru.threeguns.engine.controller.UserCenter;
import ru.threeguns.engine.manager.NoticeManager;
import ru.threeguns.engine.manager.SPCache;
import ru.threeguns.entity.Notice;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.fragments.NoticeDetailFragment;
import ru.threeguns.utils.ActivityHolder;

public class NoticeDialog extends TGDialog {
	private Notice notice;
	private CheckBox notShowNextTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int transparent_id = getContext().getResources().getIdentifier("transparent", "color", getContext().getPackageName());
		getWindow().setBackgroundDrawableResource(transparent_id);

        int tg_dialog_notice_id = getContext().getResources().getIdentifier("tg_dialog_notice", "layout", getContext().getPackageName());
		setContentView(tg_dialog_notice_id);

        int tg_checkbox_id = getContext().getResources().getIdentifier("tg_checkbox", "id", getContext().getPackageName());
        int tg_desc_id = getContext().getResources().getIdentifier("tg_desc", "id", getContext().getPackageName());
        int tg_link_id = getContext().getResources().getIdentifier("tg_link", "id", getContext().getPackageName());
        int tg_checkbox_desc_id = getContext().getResources().getIdentifier("tg_checkbox_desc", "id", getContext().getPackageName());

		notShowNextTime = (CheckBox) findViewById(tg_checkbox_id);

		TextView desc = (TextView) findViewById(tg_desc_id);
		TextView link = (TextView) findViewById(tg_link_id);
		TextView checkboxDesc = (TextView) findViewById(tg_checkbox_desc_id);

		desc.setText(notice.getContent());

        int tg_exit_btn_id = getContext().getResources().getIdentifier("tg_exit_btn", "id", getContext().getPackageName());
		findViewById(tg_exit_btn_id).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (notShowNextTime.isChecked()) {
					Module.of(SPCache.class).save(NoticeManager.KT_NOTIFY_NOTICE_ID + Module.of(UserCenter.class).getUserId(), notice.getId());
				}
				dismiss();
			}
		});

		link.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		link.getPaint().setAntiAlias(true);
		link.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Module.of(NoticeManager.class).notifyReadNotice();
				Activity act = Module.of(ActivityHolder.class).getTopActivity();
				Intent i = new Intent(act, CommonWebActivity.class);
				i.putExtra("fragment", NoticeDetailFragment.class);
				i.putExtra("target_url", NoticeDialog.this.notice.getUrl());
				act.startActivity(i);
				if (notShowNextTime.isChecked()) {
					Module.of(SPCache.class).save(NoticeManager.KT_NOTIFY_NOTICE_ID + Module.of(UserCenter.class).getUserId(), notice.getId());
				}
				dismiss();
			}
		});

		checkboxDesc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				notShowNextTime.setChecked(!notShowNextTime.isChecked());
			}
		});

	}

	public NoticeDialog(Context context, Notice notice) {
		super(context);
		this.notice = notice;
	}

}
