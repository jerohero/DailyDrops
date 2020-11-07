package com.jbol.dailydrops.models;

import com.google.gson.Gson;

import java.io.Serializable;

public class DropModel implements Serializable {

    private int id;
    private String title;
    private String note;
    private long date;

    // constructors
    public DropModel(int id, String title, String note, Long date) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date = date;
    }


    @Override
    public String toString() {
//        Log.d("dev", new GsonBuilder().setPrettyPrinting().create().toJson(this));
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
}
