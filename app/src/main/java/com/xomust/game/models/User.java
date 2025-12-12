package com.xomust.game.models;

public class User {
    private String uid;
    private String email;
    private String username;
    private String profileImageUrl;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String uid, String email, String username, String profileImageUrl) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
