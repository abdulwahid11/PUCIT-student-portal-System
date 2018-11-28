package DTO;

public class Conversation {
    String id;
    String name;
    String imageUrl;
    String lastMessage;
    boolean seen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Conversation() {
    }

    public Conversation(String name, String imageUrl, String lastMessage, boolean seen) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.lastMessage = lastMessage;
        this.seen = seen;
    }
}
