package com.app.social.chitchat.models;

/**
 * Created by Durrani on 17-Jul-17.
 */

public class Users {

    public String name, email, profile_pic, profile_thumb_image;

    public Users() {

    }

    public Users(String name, String email, String profile_pic, String thumb_image) {
        this.name = name;
        this.email = email;
        this.profile_pic = profile_pic;
        this.profile_thumb_image = thumb_image;
    }

    public Users(String name, String profile_thumb_image) {
        this.name = name;
        this.profile_thumb_image = profile_thumb_image;
    }

    public String getProfile_thumb_image() {
        return profile_thumb_image;
    }

    public void setProfile_thumb_image(String thumb_image) {
        this.profile_thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
