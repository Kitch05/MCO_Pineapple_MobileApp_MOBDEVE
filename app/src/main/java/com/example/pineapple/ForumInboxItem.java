package com.example.pineapple;

public class ForumInboxItem {
    private String sender;
    private String messagePreview;
    private String timestamp;
    private String fullMessage; // This should hold the full message content
    private boolean isRead;

    // Constructor
    public ForumInboxItem(String sender, String messagePreview, String timestamp, String fullMessage, boolean isRead) {
        this.sender = sender;
        this.messagePreview = messagePreview;
        this.timestamp = timestamp;
        this.fullMessage = fullMessage; // Initialize full message
        this.isRead = isRead;
    }

    // Getters
    public String getSender() {
        return sender;
    }

    public String getMessagePreview() {
        return messagePreview;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    // Method to get the full message
    public String getFullMessage() {
        return fullMessage; // Return the full message content
    }
}
