package com.jbol.dailydrops.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jbol.dailydrops.models.FirebaseDropList;
import com.jbol.dailydrops.models.FirebaseDropModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class FirebaseDatabaseHelper {
    // Parse Firebase snapshot to a list of FirebaseDropModels
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
