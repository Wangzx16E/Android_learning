package ru.threeguns.engine.pingback;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import kh.hyper.utils.HL;

public class PingBackEventCacheDBImpl extends SQLiteOpenHelper implements PingBackEventCache {
	private static final int VERSION = 2;
	private static final String DB_NAME = "TGPingbackEvent.db";

	private SQLiteDatabase dataBase;

	public PingBackEventCacheDBImpl(Context context) {
		super(context, DB_NAME, null, VERSION);
		init(context);
	}

	// id:唯一标示
	// request_url:请求地址
	// data_string:加密后的Data字符串
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS events" + //
				"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
				" request_url TEXT NOT NULL," + //
				" data_string TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		HL.w("DataBase onUpgrade : from  " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE events");
		db.execSQL("CREATE TABLE IF NOT EXISTS events" + //
				"(id INTEGER PRIMARY KEY AUTOINCREMENT," + //
				" request_url TEXT NOT NULL," + //
				" data_string TEXT NOT NULL)");
	}

	@Override
	public void init(Context context) {
		dataBase = getWritableDatabase();
	}

	@Override
	public void removeEvent(PingBackEvent event) {
		try {
			dataBase.delete("events", "id=?", new String[] { event.getId() + "" });
		} catch (Exception e) {
			HL.w("Remove Event Failed.");
			HL.w(e);
		}
	}

	@Override
	public PingBackEvent pollEvent() {
		try {
			Cursor c = dataBase.rawQuery(//
					"SELECT id,request_url,data_string FROM events WHERE id=(SELECT MIN(id) FROM events)", //
					null);
			if (c.moveToNext()) {
				int id = c.getInt(c.getColumnIndex("id"));
				String requestURL = c.getString(c.getColumnIndex("request_url"));
				String dataString = c.getString(c.getColumnIndex("data_string"));
				return new PingBackEvent(id, requestURL, dataString);
			}
		} catch (Exception e) {
			HL.w("Poll Event Failed.");
			HL.w(e);
		}
		return null;
	}

	@Override
	public void offerEvent(PingBackEvent event) {
		try {
			dataBase.execSQL("INSERT INTO events VALUES(null, ?, ?)", new Object[] { event.getRequestURL(), event.getDataString() });
		} catch (Exception e) {
			HL.w("Offer Event Failed.");
			HL.w(e);
		}
	}

	@Override
	public void release() {
		dataBase.close();
	}

	@Override
	public void clearEvent() {
		dataBase.delete("events", "", new String[] {});
	}

}
