package com.example.dell.tueriapp;

import android.net.Uri;

import java.util.Date;

public class ChatModelClass {

    private String User, message, imageUrl;

    public ChatModelClass(){}

    public ChatModelClass(String user, String message) {
        User = user;
        this.message = message;

    }

    public ChatModelClass(String user, Uri imageUrl) {
        this.User = user;
        this.imageUrl = imageUrl.toString();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
