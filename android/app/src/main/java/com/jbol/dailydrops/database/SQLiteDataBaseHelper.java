package com.jbol.dailydrops.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.DropModel;

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
    private static final String dbName = "user_drops.db";
    private static final int dbVersion = 1;

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
        InputStream dataset = MainActivity.getContext().getResources().openRawResource(R.raw.dataset);
        try {
            String sql = convertStreamToString(dataset);
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String upgradeTableStatement = "DROP TABLE IF EXISTS " + SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE;
        db.execSQL(upgradeTableStatement);
        onCreate(db);
    }

    public boolean addDrop(DropModel drop) {
        ContentValues cv = new ContentValues();

        cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_TITLE, drop.getTitle());
        cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_NOTE, drop.getNote());
        cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_DATE, drop.getDate());
        cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_HAS_IMAGE, drop.hasImage() ? 1 : 0);

        long insert = sqldb.insert(SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDrop(DropModel drop) {
        String queryString = "DELETE FROM " + SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE + " WHERE " + SQLiteDataBaseInfo.DropColumn.COLUMN_ID + " = " + drop.getId();

        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateDrop(DropModel newDrop) {
        DropModel oldDrop = getDropById(newDrop.getId());

        ContentValues cv = new ContentValues();

        if (!newDrop.getTitle().equals(oldDrop.getTitle())) {
            cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_TITLE, newDrop.getTitle()); }
        if (!newDrop.getNote().equals(oldDrop.getNote())) {
            cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_NOTE, newDrop.getNote()); }
        if (newDrop.getDate() != (oldDrop.getDate())) {
            cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_DATE, newDrop.getDate()); }
        cv.put(SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_HAS_IMAGE, newDrop.hasImage() ? 1 : 0);

        getWritableDatabase().update(SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE, cv, SQLiteDataBaseInfo.DropColumn.COLUMN_ID + "=" + newDrop.getId(), null);

        return true;
    }

    public DropModel getDropById(int dropId) {
        String queryString = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE id = %d", SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE, dropId);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        DropModel drop = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String note = cursor.getString(2);
            long date = cursor.getLong(3);
            boolean hasImage = cursor.getInt(4) == 1;

            drop = new DropModel(id, title, note, date, hasImage);
        }
        cursor.close();
        return drop;
    }

    public int getLastInsertedDropId() {
        String queryString = "SELECT last_insert_rowid()";
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        int dropId = 0;
        if (cursor.moveToFirst()) {
            dropId = cursor.getInt(0);
        }
        cursor.close();
        return dropId;
    }

    public List<DropModel> getAllDrops() {
        List<DropModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + SQLiteDataBaseInfo.DropTables.USER_DROPS_TABLE + " ORDER BY " + SQLiteDataBaseInfo.DropColumn.COLUMN_DROP_DATE;
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String note = cursor.getString(2);
                long date = cursor.getLong(3);
                boolean hasImage = cursor.getInt(4) == 1;

                DropModel newDrop = new DropModel(id, title, note, date, hasImage);
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
