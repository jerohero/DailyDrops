package com.jbol.dailydrops.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jbol.dailydrops.models.FirebaseDropList;
import com.jbol.dailydrops.models.FirebaseDropModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class FirebaseDatabaseHelper {
    private DatabaseReference fbDropsReference;
    private ArrayList<FirebaseDropModel> firebaseDropModelArrayList = new ArrayList<>();

    public void createListener(DatabaseReference ref) {
        firebaseDropModelArrayList = null;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    firebaseDropModelArrayList.add(null);
                    return; }
                firebaseDropModelArrayList = collectFirebaseDrops(snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });
    }

    public ArrayList<FirebaseDropModel> getFirebaseDrops() {
        return firebaseDropModelArrayList;
    }

    public ArrayList<FirebaseDropModel> collectFirebaseDrops(Object snapshotValue) {
        ArrayList<FirebaseDropModel> drops = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(snapshotValue);
        FirebaseDropList dropList = gson.fromJson(jsonElement, FirebaseDropList.class);

        Iterator<Map.Entry<String, FirebaseDropModel>> it = dropList.getDrops().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, FirebaseDropModel> pair = it.next();
            FirebaseDropModel drop = pair.getValue();
            drop.setId(pair.getKey());
            drops.add(drop);
            it.remove();
        }

        return drops;
    }
}
