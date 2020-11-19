package com.jbol.dailydrops.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {
    private static SQLiteDatabase sqldb;
    private static SQLiteDatabaseHelper instance;
    private static final String dbName = "dailydrops.db";
    private static final int dbVersion = 1;
    private static final String CREATE_USER_DROPS_TABLE =
            "CREATE TABLE " + SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE + " ("
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_TITLE + " TEXT NOT NULL, "
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_NOTE + " TEXT, "
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_DATE + " INTEGER NOT NULL, "
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_TIME + " INTEGER NOT NULL, "
                    + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE + " INT NOT NULL "
                    + ");";
    private static final String CREATE_USER_LIKES_TABLE =
            "CREATE TABLE " + SQLiteDatabaseInfo.LikesTable.USER_LIKES_TABLE + " ("
                    + SQLiteDatabaseInfo.LikesColumn.COLUMN_ID + " TEXT PRIMARY KEY "
                    + ");";
    private static final String CREATE_USER_COLLECTION_TABLE =
            "CREATE TABLE " + SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE + " ("
                    + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_ID + " TEXT PRIMARY KEY, "
                    + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_TYPE + " TEXT NOT NULL "
                    + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_DROPS_TABLE);
        db.execSQL(CREATE_USER_LIKES_TABLE);
        db.execSQL(CREATE_USER_COLLECTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeDropsTableStatement = "DROP TABLE IF EXISTS " + SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE;
        String upgradeLikesTableStatement = "DROP TABLE IF EXISTS " + SQLiteDatabaseInfo.LikesTable.USER_LIKES_TABLE;
        String upgradeCollectionTableStatement = "DROP TABLE IF EXISTS " + SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE;

        db.execSQL(upgradeDropsTableStatement);
        db.execSQL(upgradeLikesTableStatement);
        db.execSQL(upgradeCollectionTableStatement);

        onCreate(db);
    }

    public SQLiteDatabaseHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }

    public static synchronized SQLiteDatabaseHelper getHelper(Context context){
        if (instance == null){
            instance = new SQLiteDatabaseHelper(context);
            sqldb = instance.getWritableDatabase();
        }
        return instance;
    }

    public boolean addDropToCollection(String dropId, String dropType) {
        ContentValues cv = new ContentValues();

        cv.put(SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_ID, dropId);
        cv.put(SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_TYPE, dropType);

        long insert = sqldb.insert(SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE, null, cv);
        return insert != -1;
    }

    public boolean dropIsInCollection(String dropId, String dropType) {
        String queryString = "SELECT * FROM " + SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE
                + " WHERE " + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_ID + " = '" + dropId + "'"
                + " AND " + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_TYPE + " = '" + dropType + "'";
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        boolean inCollection = false;
        if (cursor.moveToFirst()) {
            inCollection = true;
        }
        cursor.close();
        return inCollection;
    }

    public boolean deleteDropFromCollection(String dropId, String dropType) {
        String queryString = "DELETE FROM " + SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE
                + " WHERE " + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_ID + " = '" + dropId + "'"
                + " AND " + SQLiteDatabaseInfo.CollectionColumn.COLUMN_DROP_TYPE + " = '" + dropType + "'";
        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public HashMap<String, String> getAllCollectionDrops() {
        HashMap<String, String> idToType = new HashMap<>();

        String queryString = "SELECT * FROM " + SQLiteDatabaseInfo.CollectionTable.USER_COLLECTION_TABLE;
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String type = cursor.getString(1);

                idToType.put(id, type);
            } while (cursor.moveToNext());
        } else {
            Log.d("dev", "Table is empty.");
        }

        cursor.close();
        return idToType;
    }



    public boolean addDropToLikes(String dropId) {
        ContentValues cv = new ContentValues();

        cv.put(SQLiteDatabaseInfo.LikesColumn.COLUMN_ID, dropId);

        long insert = sqldb.insert(SQLiteDatabaseInfo.LikesTable.USER_LIKES_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean dropIsLiked(String dropId) {
        String queryString = "SELECT * FROM " + SQLiteDatabaseInfo.LikesTable.USER_LIKES_TABLE + " WHERE id = '" + dropId + "'";
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

        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_TITLE, drop.getTitle());
        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_NOTE, drop.getNote());
        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_DATE, drop.getDate());
        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_TIME, drop.getTime());
        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE, drop.hasImage() ? 1 : 0);

        long insert = sqldb.insert(SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDropFromLocal(int id) {
        String queryString = "DELETE FROM " + SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE + " WHERE " + SQLiteDatabaseInfo.DropsColumn.COLUMN_ID + " = " + id;

        Cursor cursor = sqldb.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    public boolean updateDropFromLocal(GlobalDropModel newDrop) {
        SQLiteDropModel oldDrop = getDropByIdFromLocal(Integer.parseInt(newDrop.getId()));

        ContentValues cv = new ContentValues();

        if (!newDrop.getTitle().equals(oldDrop.getTitle())) {
            cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_TITLE, newDrop.getTitle()); }
        if (!newDrop.getNote().equals(oldDrop.getNote())) {
            cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_NOTE, newDrop.getNote()); }
        if (newDrop.getDate() != (oldDrop.getDate())) {
            cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_DATE, newDrop.getDate()); }
        if (newDrop.getTime() != (oldDrop.getTime())) {
            cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_DATE, newDrop.getTime()); }
        cv.put(SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_HAS_IMAGE, newDrop.getImage() != null ? 1 : 0);

        getWritableDatabase().update(SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE, cv, SQLiteDatabaseInfo.DropsColumn.COLUMN_ID + "=" + newDrop.getId(), null);

        return true;
    }

    public SQLiteDropModel getDropByIdFromLocal(int dropId) {
        String queryString = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE id = %d", SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE, dropId);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        SQLiteDropModel drop = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String note = cursor.getString(2);
            long date = cursor.getLong(3);
            long time = cursor.getLong(4);
            boolean hasImage = cursor.getInt(5) == 1;

            drop = new SQLiteDropModel(id, title, note, date, time, hasImage);
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

        String queryString = "SELECT * FROM " + SQLiteDatabaseInfo.DropsTable.USER_DROPS_TABLE + " ORDER BY " + SQLiteDatabaseInfo.DropsColumn.COLUMN_DROP_DATE;
        Cursor cursor = getReadableDatabase().rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String note = cursor.getString(2);
                long date = cursor.getLong(3);
                long time = cursor.getLong(4);
                boolean hasImage = cursor.getInt(5) == 1;

                SQLiteDropModel newDrop = new SQLiteDropModel(id, title, note, date, time, hasImage);
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
