package ru.threeguns.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import kh.hyper.core.Module;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.ui.Demand;
import kh.hyper.ui.HFragment;
import ru.threeguns.engine.controller.UIHelper;
import ru.threeguns.engine.manager.NoticeManager;
import ru.threeguns.entity.Notice;
import ru.threeguns.event.NewNoticeEvent;

public class NoticeFragment extends HFragment {
	private List<Notice> noticeList;
	private ListView listView;
	private BaseAdapter listAdapter;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tg_fragment_notice_id = getActivity().getResources().getIdentifier("tg_fragment_notice", "layout", getActivity().getPackageName());
		View v = inflater.inflate(tg_fragment_notice_id, null);

        int tg_back_btn_id = getActivity().getResources().getIdentifier("tg_back_btn", "id", getActivity().getPackageName());
		v.findViewById(tg_back_btn_id).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestBack();
			}
		});

        int tg_list_id = getActivity().getResources().getIdentifier("tg_list", "id", getActivity().getPackageName());
		listView = (ListView) v.findViewById(tg_list_id);

		noticeList = Module.of(NoticeManager.class).queryAllNotices();

		listAdapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
			        int tg_item_notice_id = getActivity().getResources().getIdentifier("tg_item_notice", "layout", getActivity().getPackageName());
					convertView = View.inflate(getActivity(), tg_item_notice_id, null);
				}
		        int tg_text_id = getActivity().getResources().getIdentifier("tg_text", "id", getActivity().getPackageName());
				((TextView) convertView.findViewById(tg_text_id)).setText(noticeList.get(position).getContent());
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return noticeList.get(position);
			}

			@Override
			public int getCount() {
				return noticeList.size();
			}
		};
		listView.setAdapter(listAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Demand d = new Demand(NoticeDetailFragment.class);
				Bundle b = new Bundle();
				b.putString("target_url", noticeList.get(position).getUrl());
				d.bundle(b);
				changeFragment(d);
			}
		});

		return v;
	}

	protected void refreshNotice() {
		Module.of(UIHelper.class).post2MainThread(new Runnable() {
			public void run() {
				noticeList = Module.of(NoticeManager.class).queryAllNotices();
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	@Handle
	protected void onNoticeRefresh(NewNoticeEvent e) {
		refreshNotice();
	}

	@Override
	public void onEnter() {
		super.onEnter();

		Module.of(NoticeManager.class).notifyReadNotice();
		refreshNotice();
		EventManager.instance.register(this);
	}

	@Override
	protected void onExit() {
		super.onExit();
		EventManager.instance.unregister(this);
	}

}
