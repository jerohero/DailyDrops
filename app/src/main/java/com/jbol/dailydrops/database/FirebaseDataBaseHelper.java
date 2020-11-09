package com.jbol.dailydrops.database;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jbol.dailydrops.models.DropModel;

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

    public void addDrop(DropModel drop) {
        fbdb.child("drops").child(String.valueOf(drop.getId())).setValue(drop);
    }

    public void updateDrop(DropModel drop) {
//        fbdb.child("drops").child(String.valueOf(drop.getId()))
    }


}
