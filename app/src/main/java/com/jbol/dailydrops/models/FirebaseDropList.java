package com.jbol.dailydrops.models;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class FirebaseDropList {
    private Map<String, FirebaseDropModel> drops = new HashMap<>();

    public FirebaseDropList() { }

    public Map<String, FirebaseDropModel> getDrops() {
        return drops;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
