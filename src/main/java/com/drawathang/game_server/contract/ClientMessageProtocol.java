package com.drawathang.game_server.contract;

public class ClientMessageProtocol {
    private ClientMessageType type;

    // Default constructor for JSON deserialization
    public ClientMessageProtocol() {
    }

    // Constructor to initialize with a type
    public ClientMessageProtocol(ClientMessageType type) {
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
