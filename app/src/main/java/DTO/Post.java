package DTO;

import com.google.firebase.database.Exclude;

import java.sql.Time;
import java.util.Date;

public class Post {
    private String postId;
    private String text;
    private String fileUrl;
    private String dateString;
    private String postedBy;
    private String attachmentType;
    private String postedByName;
    private String postedByImageUrl;


    public String getPostedByImageUrl() {
        return postedByImageUrl;
    }

    public void setPostedByImageUrl(String postedByImageUrl) {
        this.postedByImageUrl = postedByImageUrl;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
    }

    @Exclude
    public String getPostId() {
        return postId;
    }

    @Exclude
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Post() {
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public Post(String text, String fileUrl, String dateString, String postedBy, String id, String type,String name,String photoUrl) {
        this.text = text;
        this.postedByImageUrl=photoUrl;
        this.fileUrl = fileUrl;
        this.dateString = dateString;
        this.postedBy = postedBy;
        this.postId=id;
        this.attachmentType=type;
        this.postedByName=name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
}
