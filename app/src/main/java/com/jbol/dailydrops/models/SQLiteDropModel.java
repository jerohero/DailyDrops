package com.jbol.dailydrops.models;

import com.google.gson.Gson;
import java.io.Serializable;

public class SQLiteDropModel implements Serializable {
    private int id;
    private String title;
    private String note;
    private long date;
    private boolean hasImage;

    // default constructor
    public SQLiteDropModel() {
    }

    // constructor
    public SQLiteDropModel(int id, String title, String note, long date, boolean hasImage) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date = date;
        this.hasImage = hasImage;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public long getDate() {
        return date;
    }

    public boolean hasImage() {
        return hasImage;
    }
}
