package com.example.startupledgerpro.model;

public class Notification {
    private final String id;
    private final String recipientId;
    private final String title;
    private final String message;
    private boolean isRead;

    public Notification(String id, String recipientId, String title, String message, boolean isRead) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
    }

    public String getId() { return id; }
    public String getRecipientId() { return recipientId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { this.isRead = read; }
}