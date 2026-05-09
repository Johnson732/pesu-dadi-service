package com.pesudadi.model;

import java.time.Instant;

public class UserSession {

    private final String sessionId;
    private Gender gender;
    private AgeRange ageRange;
    private SessionStatus status;
    private String currentRoomId;
    private boolean connected;
    private final Instant createdAt;

    public UserSession(String sessionId, SessionStatus status, boolean connected, Instant createdAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.connected = connected;
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
