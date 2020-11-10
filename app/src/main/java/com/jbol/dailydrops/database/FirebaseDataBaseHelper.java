package com.jbol.dailydrops.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jbol.dailydrops.models.SQLiteDropModel;

import java.util.ArrayList;

public class FirebaseDataBaseHelper {

    private static DatabaseReference fbdb;
    private static FirebaseDataBaseHelper instance;

    public static synchronized FirebaseDataBaseHelper getHelper(){
        if (instance == null){
            instance = new FirebaseDataBaseHelper();
            fbdb =  FirebaseDatabase.getInstance().getReference("message");
        }
        return instance;
    }

    public DatabaseReference getReference() {
        return fbdb;
    }

    public ArrayList<SQLiteDropModel> getDrops() {
        return null;
    }


}
