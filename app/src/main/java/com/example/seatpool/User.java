package com.example.seatpool;

public class User {
    private String profile;
    private String id;
    private String pw;
    private String userName;

    public User(){}

    public User(String profile, String id, String pw, String userName){
        this.profile = profile;
        this.id = id;
        this.pw = pw;
        this. userName = userName;

    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
