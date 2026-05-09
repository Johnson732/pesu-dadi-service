package com.pesudadi.model;

import java.time.Instant;

public class ChatRoom {

    private final String roomId;
    private final String userOneSessionId;
    private final String userTwoSessionId;
    private final Instant createdAt;
    private boolean active;

    public ChatRoom(String roomId, String userOneSessionId, String userTwoSessionId, Instant createdAt, boolean active) {
        this.roomId = roomId;
        this.userOneSessionId = userOneSessionId;
        this.userTwoSessionId = userTwoSessionId;
        this.createdAt = createdAt;
        this.active = active;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserOneSessionId() {
        return userOneSessionId;
    }

    public String getUserTwoSessionId() {
        return userTwoSessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean includes(String sessionId) {
        return userOneSessionId.equals(sessionId) || userTwoSessionId.equals(sessionId);
    }

    public String partnerFor(String sessionId) {
        if (userOneSessionId.equals(sessionId)) {
            return userTwoSessionId;
        }
        if (userTwoSessionId.equals(sessionId)) {
            return userOneSessionId;
        }
        return null;
    }
}
