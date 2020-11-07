package com.jbol.dailydrops.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.jbol.dailydrops.models.DropModel;
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
        String createTableStatement =
                "CREATE TABLE " + DataBaseInfo.DropTables.USER_DROPS_TABLE + " (" +
                DataBaseInfo.DropColumn.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataBaseInfo.DropColumn.COLUMN_DROP_TITLE + " TEXT, " +
                DataBaseInfo.DropColumn.COLUMN_DROP_NOTE + " TEXT, " +
                DataBaseInfo.DropColumn.COLUMN_DROP_DATE + " TEXT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeTableStatement = "DROP TABLE IF EXISTS " + DataBaseInfo.DropTables.USER_DROPS_TABLE;
        db.execSQL(upgradeTableStatement);
        onCreate(db);
    }

    public boolean addDrop(DropModel dropModel) {
        ContentValues cv = new ContentValues();

        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_TITLE, dropModel.getTitle());
        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_NOTE, dropModel.getNote());
        cv.put(DataBaseInfo.DropColumn.COLUMN_DROP_DATE, dropModel.getDate());

        long insert = sqldb.insert(DataBaseInfo.DropTables.USER_DROPS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDrop(DropModel dropModel) {
        String queryString = "DELETE FROM " + DataBaseInfo.DropTables.USER_DROPS_TABLE + " WHERE " + DataBaseInfo.DropColumn.COLUMN_ID + " = " + dropModel.getId();

        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public List<DropModel> getAllDrops() {
        List<DropModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + DataBaseInfo.DropTables.USER_DROPS_TABLE;
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
