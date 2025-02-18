package com.drawathang.game_server.contract;

public class GameServerResponse {
    public GameServerMessageProtocol payload;
    public RequiredBroadcastInfo requiredBroadcastInfo;

    public GameServerResponse(RequiredBroadcastInfo broadcastInfo, GameServerMessageProtocol payload) {
        this.requiredBroadcastInfo = broadcastInfo;
        this.payload = payload;
    }
}


