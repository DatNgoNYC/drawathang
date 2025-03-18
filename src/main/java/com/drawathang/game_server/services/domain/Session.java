package com.drawathang.game_server.services.domain;

public class Session {
    private final String sessionId;
    private String username = "";

    public Session(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getUsername() {
        return this.username;
    }

    public String setUsername(String username) {
        return this.username = username;
    }
}
