package com.jbol.dailydrops.database;

public class SQLiteDataBaseInfo {

    public class DropTables {
        public static final String USER_DROPS_TABLE = "user_drops";
    }

    public class DropColumn {
        public static final String COLUMN_DROP_TITLE = "title";
        public static final String COLUMN_DROP_NOTE = "note";
        public static final String COLUMN_DROP_DATE = "date";
        public static final String COLUMN_DROP_HAS_IMAGE = "has_image";
        public static final String COLUMN_ID = "id";
    }
}