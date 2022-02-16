package ru.threeguns.engine.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import kh.hyper.core.Module;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.entity.Notice;

public class NoticeCache extends SQLiteOpenHelper {
	private static final int VERSION = 2;
	private static final String DB_NAME = "TG_NOTICE.db";
	private static final String TABLE_NAME = "notices";

	private SQLiteDatabase dataBase;

	public NoticeCache(Context context) {
		super(context, DB_NAME, null, VERSION);
		init(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + //
				"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
				" user_id TEXT NOT NULL," + //
				" notice_id INTEGER NOT NULL," + //
				" content TEXT NOT NULL," + //
				" url TEXT NOT NULL," + //
				" timestamp TEXT NOT NULL," + //
				" sdklang TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		HL.w("DataBase onUpgrade : from  " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE " + TABLE_NAME);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + //
				"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
				" user_id TEXT NOT NULL," + //
				" notice_id INTEGER NOT NULL," + //
				" content TEXT NOT NULL," + //
				" url TEXT NOT NULL," + //
				" timestamp TEXT NOT NULL," + //
				" sdklang TEXT NOT NULL)");
	}

	public void init(Context context) {
		dataBase = getWritableDatabase();
	}

	public List<Notice> queryAllNotices(String userId) {
		List<Notice> list = new ArrayList<Notice>();
		Cursor c = dataBase.query(TABLE_NAME, null, "user_id=? and sdklang=?", new String[] { userId, Module.of(TGController.class).appLanguage }, null, null, "notice_id DESC", "10");
		while (c.moveToNext()) {
			int id = c.getInt(c.getColumnIndex("notice_id"));
			String content = c.getString(c.getColumnIndex("content"));
			String timestamp = c.getString(c.getColumnIndex("timestamp"));
			String url = c.getString(c.getColumnIndex("url"));
			Notice notice = new Notice(id, content, url, timestamp, Module.of(TGController.class).appLanguage);
			list.add(notice);
		}
		return list;
	}

	public void pushNotice(String userId, List<Notice> noticeList) {
		for (Notice notice : noticeList) {
			pushNotice(userId, notice);
		}
	}

	public void pushNotice(String userId, Notice notice) {
		ContentValues cv = new ContentValues();
		cv.put("user_id", userId);
		cv.put("notice_id", notice.getId());
		cv.put("content", notice.getContent());
		cv.put("url", notice.getUrl());
		cv.put("timestamp", notice.getTimestamp());
		cv.put("sdklang", notice.getSdklang());
		dataBase.insert(TABLE_NAME, null, cv);
	}

	public int queryLastestNoticeId(String userId) {
		int id = -1;
		Cursor c = dataBase.query(TABLE_NAME, null, "user_id=? and sdklang=?", new String[] { userId, Module.of(TGController.class).appLanguage }, null, null, "notice_id DESC", "1");
		try {
			if (c != null) {
				if (c.moveToNext()) {
					id = c.getInt(c.getColumnIndex("notice_id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			HL.i(">>>>>>>>>>>>>>>  queryLastestNoticeId " + e.toString());
		}
		return id;
	}

	public void release() {
		dataBase.close();
	}

}
