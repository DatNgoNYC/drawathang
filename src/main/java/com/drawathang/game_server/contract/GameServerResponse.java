package com.drawathang.game_server.contract;

public class GameServerResponse {
    public GameServerMessage payload;
    public RequiredBroadcastInfo requiredBroadcastInfo;

    public GameServerResponse(RequiredBroadcastInfo broadcastInfo, GameServerMessage payload) {
        this.requiredBroadcastInfo = broadcastInfo;
        this.payload = payload;
    }
}


