package com.jbol.dailydrops.models;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

//    private void sortDropsByDate() {
//        ArrayList<FirebaseDropModel> sortedDrops = new ArrayList<>();
//
//        Iterator<Map.Entry<String, FirebaseDropModel>> it = drops.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, FirebaseDropModel> pair = (Map.Entry<String, FirebaseDropModel>)it.next();
//            FirebaseDropModel drop = (FirebaseDropModel) pair.getValue();
//            drops.add(drop);
//            it.remove();
//        }
//    }

}
