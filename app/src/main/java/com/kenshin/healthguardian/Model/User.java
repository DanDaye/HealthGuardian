package com.kenshin.healthguardian.Model;

/**
 * Created by Kenshin on 2017/5/16.
 */

public class User {
    private String userName;
    private String userID;

    public User(String userNname, String userID) {
        this.userName = userNname;
        this.userID = userID;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserNname(String userNname) {
        this.userName = userNname;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
