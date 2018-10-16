package com.app.social.chitchat.models;

/**
 * Created by Junai on 9/4/2017.
 */

public class Comments {

    String from,comment,type;
    long time;

    public Comments(String from, String comment, String type, long time) {
        this.from = from;
        this.comment = comment;
        this.type = type;
        this.time = time;
    }

    public Comments() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
