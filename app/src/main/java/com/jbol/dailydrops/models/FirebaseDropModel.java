package com.jbol.dailydrops.models;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FirebaseDropModel {
    private String id;
    private String title;
    private String note;
    private long date;
    private String imagePath;

    // default constructor
    public FirebaseDropModel() {}

    // constructor
    public FirebaseDropModel(String id, String title, String note, long date, String imagePath) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date = date;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
