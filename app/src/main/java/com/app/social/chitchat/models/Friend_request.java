package com.app.social.chitchat.models;

/**
 * Created by Junai on 8/22/2017.
 */

public class Friend_request {

    private String request_type;

    public Friend_request(String request_type) {
        this.request_type = request_type;
    }

    public Friend_request() {
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
