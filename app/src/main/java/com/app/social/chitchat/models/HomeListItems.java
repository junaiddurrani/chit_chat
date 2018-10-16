package com.app.social.chitchat.models;

public class HomeListItems {
    private String homePage_items;
    private int homePageImage = NO_IMAGE_PROVIDED;
    private static final int NO_IMAGE_PROVIDED = -1;

    public HomeListItems(String items, int img) {
        homePage_items = items;
        homePageImage = img;
    }

    public String getHomePage_items() {
        return homePage_items;
    }

    public boolean hasImage(){
        return homePageImage != NO_IMAGE_PROVIDED;
    }

    public int getHomePageImage() {
        return homePageImage;
    }

}
