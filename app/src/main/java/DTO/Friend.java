package DTO;

public class Friend {
    String name;
    String imageUrl;
    String date;
    String id;
    String isOnline;

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Friend(String name,String img,String dt,String i,String isOnline) {
        this.name = name;
        this.date=dt;
        this.imageUrl=img;
        this.id=i;
        this.isOnline=isOnline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Friend() {
    }
}
