package com.jbol.dailydrops.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SQLiteDataBaseHelper extends SQLiteOpenHelper {

    private static SQLiteDatabase sqldb;
    private static SQLiteDataBaseHelper instance;
    private static final String dbName = "dailydrops.db";
    private static final int dbVersion = 1;

    private static final String CREATE_USER_DROPS_TABLE =
            "CREATE TABLE " + SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE + " ("
                    + SQLiteDataBaseInfo.DropsColumn.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_TITLE + " TEXT NOT NULL, "
                    + SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_NOTE + " TEXT, "
                    + SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_DATE + " INTEGER NOT NULL, "
                    + SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE + " INT NOT NULL "
                    + ");";


    private static final String CREATE_USER_LIKES_TABLE =
            "CREATE TABLE " + SQLiteDataBaseInfo.LikesTable.USER_LIKES_TABLE + " ("
                    + SQLiteDataBaseInfo.LikesColumn.COLUMN_ID + " TEXT PRIMARY KEY "
                    + ");";


    public SQLiteDataBaseHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }

    public static synchronized SQLiteDataBaseHelper getHelper(Context context){
        if (instance == null){
            instance = new SQLiteDataBaseHelper(context);
            sqldb = instance.getWritableDatabase();
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_DROPS_TABLE);
        db.execSQL(CREATE_USER_LIKES_TABLE);
    }

    private String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        String sql = sb.toString();

        return sql;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeTableStatement = "DROP TABLE IF EXISTS " + SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE;
        db.execSQL(upgradeTableStatement);
        onCreate(db);
    }

    public boolean addDropToLikes(String dropId) {
        ContentValues cv = new ContentValues();

        cv.put(SQLiteDataBaseInfo.LikesColumn.COLUMN_ID, dropId);

        long insert = sqldb.insert(SQLiteDataBaseInfo.LikesTable.USER_LIKES_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean dropIsLiked(String dropId) {
//        String queryString = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE id LIKE %s", SQLiteDataBaseInfo.LikesTable.USER_LIKES_TABLE, dropId);
        String queryString = "SELECT * FROM " + SQLiteDataBaseInfo.LikesTable.USER_LIKES_TABLE + " WHERE id = '" + dropId + "'";
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        boolean isLiked = false;
        if (cursor.moveToFirst()) {
            isLiked = true;
        }
        cursor.close();
        return isLiked;
    }

    public boolean addDropToLocal(SQLiteDropModel drop) {
        ContentValues cv = new ContentValues();

        cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_TITLE, drop.getTitle());
        cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_NOTE, drop.getNote());
        cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_DATE, drop.getDate());
        cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE, drop.hasImage() ? 1 : 0);

        long insert = sqldb.insert(SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDropFromLocal(int id) {
        String queryString = "DELETE FROM " + SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE + " WHERE " + SQLiteDataBaseInfo.DropsColumn.COLUMN_ID + " = " + id;

        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateDropFromLocal(GlobalDropModel newDrop) {
        SQLiteDropModel oldDrop = getDropByIdFromLocal(Integer.parseInt(newDrop.getId()));

        ContentValues cv = new ContentValues();

        if (!newDrop.getTitle().equals(oldDrop.getTitle())) {
            cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_TITLE, newDrop.getTitle()); }
        if (!newDrop.getNote().equals(oldDrop.getNote())) {
            cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_NOTE, newDrop.getNote()); }
        if (newDrop.getDate() != (oldDrop.getDate())) {
            cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_DATE, newDrop.getDate()); }
        cv.put(SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE, newDrop.getImage() != null ? 1 : 0);

        getWritableDatabase().update(SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE, cv, SQLiteDataBaseInfo.DropsColumn.COLUMN_ID + "=" + newDrop.getId(), null);

        return true;
    }

    public SQLiteDropModel getDropByIdFromLocal(int dropId) {
        String queryString = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE id = %d", SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE, dropId);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        SQLiteDropModel drop = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String note = cursor.getString(2);
            long date = cursor.getLong(3);
            boolean hasImage = cursor.getInt(4) == 1;

            drop = new SQLiteDropModel(id, title, note, date, hasImage);
        }
        cursor.close();
        return drop;
    }

    public int getLastInsertedDropIdFromLocal() {
        String queryString = "SELECT last_insert_rowid()";
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        int dropId = 0;
        if (cursor.moveToFirst()) {
            dropId = cursor.getInt(0);
        }
        cursor.close();
        return dropId;
    }

    public List<SQLiteDropModel> getAllDropsFromLocal() {
        List<SQLiteDropModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + SQLiteDataBaseInfo.DropsTable.USER_DROPS_TABLE + " ORDER BY " + SQLiteDataBaseInfo.DropsColumn.COLUMN_DROP_DATE;
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String note = cursor.getString(2);
                long date = cursor.getLong(3);
                boolean hasImage = cursor.getInt(4) == 1;

                SQLiteDropModel newDrop = new SQLiteDropModel(id, title, note, date, hasImage);
                    returnList.add(newDrop);
            } while (cursor.moveToNext());
        } else {
            Log.d("dev", "Database is empty.");
        }

        cursor.close();
//        db.close();
        return returnList;
    }

}
