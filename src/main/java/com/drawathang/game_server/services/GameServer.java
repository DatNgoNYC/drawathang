package com.drawathang.game_server.services;

import com.drawathang.game_server.contract.GameServerMessageProtocol;
import com.drawathang.game_server.contract.GameServerMessageType;
import com.drawathang.game_server.contract.GameServerResponse;
import com.drawathang.game_server.contract.RequiredBroadcastInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServer {
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public GameServerResponse joinServer(String sessionId) {
        // MAIN TRANSACTION:
        // Create a Session with the sessionId, then store it.
        Session session = new Session(sessionId);
        sessions.put(sessionId, session);

        // RESPONDING WITH UPDATE:
        // Create the RequiredBroadcastInfo (list of recipients)
        List<String> recipients = sessions.keySet().stream().toList();
        RequiredBroadcastInfo broadcastInfo = new RequiredBroadcastInfo(recipients);

        // Create the GameServerApplication payload for the client
        int sessionsCount = sessions.size();
        GameServerMessageProtocol payload = new GameServerMessageProtocol(GameServerMessageType.USER_JOINED, sessionsCount);

        return new GameServerResponse(broadcastInfo, payload);
    }

    public GameServerResponse leaveServer(String sessionId) {
        // MAIN TRANSACTION:
        // Remove the session
        sessions.remove(sessionId);

        // RESPONDING WITH UPDATE:
        // Create the RequiredBroadcastInfo (list of recipients)
        List<String> recipients = sessions.keySet().stream().toList();
        RequiredBroadcastInfo broadcastInfo = new RequiredBroadcastInfo(recipients);

        // Create the GameServerApplication payload for the client
        int sessionsCount = sessions.size();
        GameServerMessageProtocol payload = new GameServerMessageProtocol(GameServerMessageType.USER_LEFT, sessionsCount);

        return new GameServerResponse(broadcastInfo, payload);
    }
}
