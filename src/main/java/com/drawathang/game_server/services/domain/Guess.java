package com.drawathang.game_server.services.domain;

public class Guess {
    private final String sessionId;
    private final String username;
    private final String guess;

    public Guess(String sessionId, String username, String guess) {
        this.sessionId = sessionId;
        this.username = username;
        this.guess = guess;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public String getGuess() {
        return guess;
    }
}