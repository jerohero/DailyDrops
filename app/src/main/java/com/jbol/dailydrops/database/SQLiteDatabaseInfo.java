package com.jbol.dailydrops.database;

public class SQLiteDatabaseInfo {

    public class CollectionTable {
        public static final String USER_COLLECTION_TABLE = "user_collection";
    }

    public class CollectionColumn {
        public static final String COLUMN_DROP_ID = "drop_id";
        public static final String COLUMN_DROP_TYPE = "drop_type";
    }

    public class LikesTable {
        public static final String USER_LIKES_TABLE = "user_likes";
    }

    public class LikesColumn {
        public static final String COLUMN_ID = "id";
    }

    public class DropsTable {
        public static final String USER_DROPS_TABLE = "user_drops";
    }

    public class DropsColumn {
        public static final String COLUMN_DROP_TITLE = "title";
        public static final String COLUMN_DROP_NOTE = "note";
        public static final String COLUMN_DROP_DATE = "date";
        public static final String COLUMN_DROP_TIME = "time";
        public static final String COLUMN_DROP_HAS_IMAGE = "has_image";
        public static final String COLUMN_ID = "id";
    }

}
