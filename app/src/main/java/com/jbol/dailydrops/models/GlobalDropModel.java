package com.jbol.dailydrops.models;

import java.io.Serializable;

public class GlobalDropModel implements Serializable, Comparable<GlobalDropModel>{
    public static final String ONLINE_TYPE = "ON";
    public static final String OFFLINE_TYPE = "OFF";

    private String id;
    private String title;
    private String note;
    private long date;
    private String image; // Drop's ID for offline (ex. 31 + .jpg), image link for online
    private String type;
    private long likes;

    public GlobalDropModel(Object inputDrop) {
        if (inputDrop instanceof SQLiteDropModel) {
            SQLiteDropModel sqLiteDropModel = (SQLiteDropModel) inputDrop;
            this.id = String.valueOf(sqLiteDropModel.getId());
            this.title = sqLiteDropModel.getTitle();
            this.note = sqLiteDropModel.getNote();
            this.date = sqLiteDropModel.getDate();
            this.image = sqLiteDropModel.hasImage() ? String.valueOf(sqLiteDropModel.getId()) : null;
            this.likes = 0;
            this.type = OFFLINE_TYPE;
        }
        else if (inputDrop instanceof FirebaseDropModel) {
            FirebaseDropModel firebaseDropModel = (FirebaseDropModel) inputDrop;
            this.id = firebaseDropModel.getId();
            this.title = firebaseDropModel.getTitle();
            this.note = firebaseDropModel.getNote();
            this.date = firebaseDropModel.getDate();
            this.image = firebaseDropModel.getImagePath();
            this.likes = firebaseDropModel.getLikes();
            this.type = ONLINE_TYPE;
        }
    }

    @Override
    public int compareTo(GlobalDropModel drop) {
        return Long.compare(this.date, drop.date);
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLikes() {
        return likes;
    }

}
