package com.example.abdul.pucitstudentportalsystem;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class Messages {

    private String message, type;
    private long  time;
    private boolean seen;

    private String from;
    private String otherUserImage;
    private String otherUserName;

    public String getOtherUserImage() {
        return otherUserImage;
    }

    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public Messages(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String message, String type, long time, boolean seen,String img,String name) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.otherUserImage=img;
        this.otherUserName=name;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Messages(){

    }

}
