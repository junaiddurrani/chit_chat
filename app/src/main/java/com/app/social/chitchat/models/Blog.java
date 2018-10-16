package com.app.social.chitchat.models;

/**
 * Created by Junai on 8/24/2017.
 */

public class Blog {

    String desc,image,uid,username,post_time;

    public Blog(String desc, String image, String uid, String username, String post_time) {
        this.desc = desc;
        this.image = image;
        this.uid = uid;
        this.username = username;
        this.post_time = post_time;
    }

    public Blog() {
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}