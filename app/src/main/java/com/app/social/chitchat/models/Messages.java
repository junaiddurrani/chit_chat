package com.app.social.chitchat.models;

/**
 * Created by Durrani on 10-Aug-17.
 */

public class Messages {

    private String message, type, from;
    private long time;
    private Boolean seen;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Messages() {
    }

    public long getTime() {

        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Messages(String message, String type, long time, Boolean seen) {

        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

}
