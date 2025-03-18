package com.drawathang.game_server.contract;

public class ClientMessage {
    private ClientMessageType type;

    // Default constructor for JSON deserialization
    public ClientMessage() {
    }

    // Constructor to initialize with a type
    public ClientMessage(ClientMessageType type) {
        this.type = type;
    }

    // Getter method
    public ClientMessageType getType() {
        return type;
    }

    // Setter method (needed for JSON deserialization)
    public void setType(ClientMessageType type) {
        this.type = type;
    }
}

