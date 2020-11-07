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

public class DataBaseHelper extends SQLiteOpenHelper {

    private static SQLiteDatabase sqldb;
    private static DataBaseHelper instance;
    private static final String dbName = "user_drops.db";
    private static final int dbVersion = 1;

    public DataBaseHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }

    public static synchronized DataBaseHelper getHelper(Context context){
        if (instance == null){
            instance = new DataBaseHelper(context);
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
        Log.d("fliepflap", "convertStreamToString: " + sql);
        return sql;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeTableStatement = "DROP TABLE IF EXISTS " + DataBaseInfo.DropTables.USER_DROPS_TABLE;
        db.execSQL(upgradeTableStatement);
        onCreate(db);
    }

    public boolean addDrop(DropModel drop) {
        ContentValues cv = new ContentValues();

        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_TITLE, drop.getTitle());
        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_NOTE, drop.getNote());
        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_DATE, drop.getDate());

        long insert = sqldb.insert(DataBaseInfo.DropTables.USER_DROPS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDrop(DropModel drop) {
        String queryString = "DELETE FROM " + DataBaseInfo.DropTables.USER_DROPS_TABLE + " WHERE " + DataBaseInfo.DropColumn.COLUMN_ID + " = " + drop.getId();

        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateDrop(DropModel drop) {
//        String queryString = "UPDATE " + DataBaseInfo.DropTables.USER_DROPS_TABLE + " SET " +
        return false;
    }

    public List<DropModel> getAllDrops() {
        List<DropModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + DataBaseInfo.DropTables.USER_DROPS_TABLE + " ORDER BY " + DataBaseInfo.DropColumn.COLUMN_DROP_DATE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String note = cursor.getString(2);
                    long date = cursor.getLong(3);

                    DropModel newDrop = new DropModel(id, title, note, date);
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
