package ru.threeguns.engine.manager.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.manager.FastInfoCache;

public class FastInfoCacheDBImpl extends Module implements FastInfoCache {
    private static final String DB_NAME = "TG_COMMON_FAST_DB";
    private static final String TABLE_NAME = "TG_FASTS";
    private static final String ACTIVEUSER_TABLE_NAME = "TG_ACTIVE_FAST_USER";
    private static final int VERSION = 1;
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onLoad(Parameter parameter) {
        dbHelper = new SQLiteOpenHelper(getContext(), DB_NAME, null, VERSION) {

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                HL.w("DataBase onUpgrade : from  " + oldVersion + " to " + newVersion);
                db.execSQL("DROP TABLE " + TABLE_NAME);
                db.execSQL("DROP TABLE " + ACTIVEUSER_TABLE_NAME);
                createDB(db);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                createDB(db);
            }

            private void createDB(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + //
                        "(user_id TEXT PRIMARY KEY NOT NULL," + //
                        " username TEXT NOT NULL," + //
                        " password TEXT ," + //
                        " nickname TEXT ," + //
                        " token TEXT ," + //
                        " user_type TEXT NOT NULL," + //
                        " update_at INTEGER NOT NULL)");
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ACTIVEUSER_TABLE_NAME + //
                        "(id INTEGER PRIMARY KEY," + //
                        " active_user_id TEXT)");
            }
        };
        db = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onRelease() {
        db.close();
    }

    @Override
    public List<InternalUser> getAllUsers() {
        List<InternalUser> list = new ArrayList<InternalUser>();
        try {
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY update_at DESC LIMIT 5", null);
            while (c.moveToNext()) {
                String userId = c.getString(c.getColumnIndex("user_id"));
                String username = c.getString(c.getColumnIndex("username"));
                String password = c.getString(c.getColumnIndex("password"));
                String nickname = c.getString(c.getColumnIndex("nickname"));
                String token = c.getString(c.getColumnIndex("token"));
                String userType = c.getString(c.getColumnIndex("user_type"));
                list.add(new InternalUser()//
                        .setUserId(userId)//
                        .setUsername(username)//
                        .setPassword(password)//
                        .setNickname(nickname)//
                        .setToken(token)//
                        .setUserType(userType)//
                );
            }
        } catch (Exception e) {
            HL.w("PingBackEventCacheDBImpl", "Poll Event Failed.");
            HL.w(e);
        }
        return list;
    }

    @Override
    public void updateUser(InternalUser user) {
        try {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            db.execSQL("REPLACE INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?, ?, ?)", //
                    new Object[] { user.getUserId(), //
                            user.getUsername(), //
                            user.getPassword(), //
                            user.getNickname(), //
                            user.getToken(), //
                            user.getUserType(), //
                            Integer.valueOf(currentTime) //
                    });
        } catch (Exception e) {
            HL.w("Update User Failed.");
            HL.w(e);
        }
    }

    @Override
    public InternalUser getUserById(String id) {
        try {
            Cursor c = db.rawQuery(//
                    "SELECT * FROM " + TABLE_NAME + " WHERE user_id=(?)", //
                    new String[] { id });
            if (c.moveToNext()) {
                String userId = c.getString(c.getColumnIndex("user_id"));
                String username = c.getString(c.getColumnIndex("username"));
                String password = c.getString(c.getColumnIndex("password"));
                String nickname = c.getString(c.getColumnIndex("nickname"));
                String token = c.getString(c.getColumnIndex("token"));
                String userType = c.getString(c.getColumnIndex("user_type"));
                return new InternalUser()//
                        .setUserId(userId)//
                        .setUsername(username)//
                        .setPassword(password)//
                        .setNickname(nickname)//
                        .setToken(token)//
                        .setUserType(userType);
            }
        } catch (Exception e) {
            HL.w("getUserById Failed.");
            HL.w(e);
        }
        return null;
    }

    @Override
    public void deleteUserById(String id) {
        try {
            db.delete(TABLE_NAME, "id=?", new String[] { id });
        } catch (Exception e) {
            HL.w("Delete User Failed.");
            HL.w(e);
        }
    }

    @Override
    public String getActiveUserId() {
        try {
            Cursor c = db.rawQuery("SELECT * FROM " + ACTIVEUSER_TABLE_NAME, null);
            if (c.moveToNext()) {
                String userId = c.getString(c.getColumnIndex("active_user_id"));
                return userId;
            }
        } catch (Exception e) {
            HL.w("getActiveUserId Failed.");
            HL.w(e);
        }
        return null;
    }

    @Override
    public InternalUser getActiveUser() {
        String id = getActiveUserId();
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        return getUserById(id);
    }

    @Override
    public void setActiveUserId(String id) {
        try {
            db.execSQL("REPLACE INTO " + ACTIVEUSER_TABLE_NAME + " VALUES(?, ?)", new Object[] { Integer.valueOf(1), id });
        } catch (Exception e) {
            HL.w("setActiveUserId Failed.");
            HL.w(e);
        }
    }

}
