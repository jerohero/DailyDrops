package com.jbol.dailydrops.models;

import android.graphics.Bitmap;

public class GlobalDropModel {
    private String title;
    private String note;
    private long date;
    private Bitmap image;

    public GlobalDropModel(String title, String note, long date, Bitmap image) {
        this.title = title;
        this.note = note;
        this.date = date;
        this.image = image;
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

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
