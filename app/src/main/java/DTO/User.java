package DTO;

import android.net.Uri;

import com.google.firebase.database.Exclude;

public class User {
    private String Id;
    private String email;
    private String password;
    private String userName;
    private String dateOfBirth;
    private Boolean isCR;
    private String thumbPath;
    private String deviceToken;
    private long online;

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public Boolean getCR() {
        return isCR;
    }

    public void setCR(Boolean CR) {
        isCR = CR;
    }


    public User(String id , String email, String password, String userName, String dateOfBirth,Boolean isCR,String thumb,String deviceTok,long isOnline) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.dateOfBirth = dateOfBirth;
        this.Id=id;
        this.isCR=isCR;
        this.thumbPath=thumb;
        this.deviceToken=deviceTok;
        this.online=isOnline;
    }

    @Exclude
    public String getId() {
        return Id;
    }

    @Exclude
    public void setId(String id) {
        Id = id;
    }

    public User() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
