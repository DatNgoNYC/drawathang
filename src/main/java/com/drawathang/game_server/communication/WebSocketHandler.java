package com.drawathang.game_server.communication;

import com.drawathang.game_server.contract.ClientMessageProtocol;
import com.drawathang.game_server.contract.GameServerMessageProtocol;
import com.drawathang.game_server.contract.GameServerResponse;
import com.drawathang.game_server.contract.RequiredBroadcastInfo;
import com.drawathang.game_server.services.GameServer;
import com.drawathang.game_server.util.JsonUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockingQueue<TextMessage>> messageQueues = new ConcurrentHashMap<>();

    private final GameServer gameServer;

    public WebSocketHandler(GameServer gameServer) {
        this.gameServer = gameServer;
    }


    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        webSocketSessions.put(session.getId(), session);
        messageQueues.put(session.getId(), new LinkedBlockingQueue<>());

        // Start a thread for sending server responses - for this session.
        new Thread(() -> {

            BlockingQueue<TextMessage> queue = messageQueues.get(session.getId());

            while (session.isOpen()) {
                try {
                    TextMessage message = queue.take();
                    session.sendMessage(message);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            }

        }).start();
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        ClientMessageProtocol clientMessage = JsonUtil.fromJson(message.getPayload(), ClientMessageProtocol.class);

        switch (clientMessage.getType()) {
            case JOIN_SERVER:

                GameServerResponse gameServerResponse = this.gameServer.joinServer(session.getId());
                RequiredBroadcastInfo broadcastInfo = gameServerResponse.requiredBroadcastInfo;
                GameServerMessageProtocol payload = gameServerResponse.payload;

                // iterate over recipients which is String matching the websocket sessionIds. use the websocket session to send
                TextMessage textMessage = new TextMessage(JsonUtil.toJson(payload));
                for (String sessionId : broadcastInfo.recipients) {
                    BlockingQueue<TextMessage> queue = messageQueues.get(sessionId);

                    queue.offer(textMessage);

//                    WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
//
//                    synchronized (webSocketSession) {
//                        webSocketSession.sendMessage(new TextMessage(JsonUtil.toJson(payload)));
//                    }
                }

                break;

            case SET_USERNAME:
                session.sendMessage(new TextMessage("Not implemented yet."));

            default:
                session.sendMessage(new TextMessage("Not a valid message."));
        }
    }

    @SuppressWarnings("resource") // Spring manages WebSocketSession lifecycle; no explicit close needed on Map.remove()
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        webSocketSessions.remove(session.getId());
        messageQueues.remove(session.getId());

        GameServerResponse gameServerResponse = this.gameServer.leaveServer(session.getId());

        RequiredBroadcastInfo requiredBroadcastInfo = gameServerResponse.requiredBroadcastInfo;
        GameServerMessageProtocol gameServerMessageProtocol = gameServerResponse.payload;

        // iterate over recipients which is String matching the websocket sessionIds. use the websocket session to send
        TextMessage textMessage = new TextMessage(JsonUtil.toJson(gameServerMessageProtocol));
        for (String sessionId : requiredBroadcastInfo.recipients) {
            BlockingQueue<TextMessage> queue = messageQueues.get(sessionId);

            queue.offer(textMessage);
        }

    }

}