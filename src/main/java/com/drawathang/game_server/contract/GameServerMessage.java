package com.drawathang.game_server.contract;

public class GameServerMessage {
    public final long timestamp;
    public final GameServerMessageType type;
    public final int playerCount;

    public GameServerMessage(long timestamp, GameServerMessageType type, int playerCount) {
        this.timestamp = timestamp;
        this.type = type;
        this.playerCount = playerCount;
    }
}


