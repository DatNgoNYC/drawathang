package com.drawathang.game_server.contract;

public class GameServerMessageProtocol {
    public final long timestamp;
    public final GameServerMessageType type;
    public final int playerCount;

    public GameServerMessageProtocol(GameServerMessageType type, int playerCount) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.playerCount = playerCount;
    }
}
