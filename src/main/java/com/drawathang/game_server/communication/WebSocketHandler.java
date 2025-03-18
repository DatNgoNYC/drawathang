package com.drawathang.game_server.communication;

import com.drawathang.game_server.contract.ClientMessage;
import com.drawathang.game_server.contract.GameServerMessage;
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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles WebSocket connections for the Draw-a-Thang game server.
 * Manages WebSocket sessions, message queues, and real-time communication.
 */
@Component  // just a "bean" that spring boot manages
public class WebSocketHandler extends TextWebSocketHandler {

    /**
     * Stores message queues for each session to enable asynchronous message sending.
     */
    private final ConcurrentHashMap<String, BlockingQueue<TextMessage>> messageQueues = new ConcurrentHashMap<>();

    /**
     * Reference to the game server logic to handle game state changes.
     */
    private final GameServer gameServer;

    /**
     * Constructor to initialize the WebSocketHandler with a GameServer instance.
     *
     * @param gameServer The game server instance that handles business logic.
     */
    public WebSocketHandler(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    /**
     * Called when a new WebSocket connection is established.
     * Initializes the session and starts a dedicated message sender thread for the session.
     *
     * @param session The WebSocket session that was established.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        BroadcastService.registerSession(session);
//        messageQueues.put(session.getId(), new LinkedBlockingQueue<>());
//
//        // Start a separate thread to process and send outgoing messages for this session
//        new Thread(() -> {
//
//            BlockingQueue<TextMessage> queue = messageQueues.get(session.getId());
//
//            while (session.isOpen()) {
//                try {
//                    TextMessage message = queue.take();
//                    session.sendMessage(message);
//                } catch (InterruptedException | IOException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//        }).start();
    }

    /**
     * Handles incoming WebSocket messages from clients.
     * Parses the message, determines the action type, and processes accordingly.
     *
     * @param session The WebSocket session from which the message was received.
     * @param message The text message received from the client.
     * @throws IOException If an error occurs while sending a response.
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        // Parse JSON string into a Map instead of a class
        @SuppressWarnings("unchecked") // Suppresses the unchecked cast warning
        Map<String, Object> clientMessageMap = JsonUtil.fromJson(message.getPayload(), Map.class);

        // Extract the type dynamically
        String type = (String) clientMessageMap.get("type");

        switch (type) {
            case "JOIN_SERVER":
                this.gameServer.joinServer(session.getId());
                break;

            case "LEAVE_SERVER":
                this.gameServer.leaveServer(session.getId());
                break;

            case "CREATE_ROOM":
                this.gameServer.createRoom(session.getId(), (String) clientMessageMap.get("roomName"));
                break;

            case "SET_USERNAME":
                this.gameServer.setUsername(session.getId(), (String) clientMessageMap.get("username"));
                break;



            default:
                session.sendMessage(new TextMessage("Not a valid message."));
                break;
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * Cleans up session data and notifies other players.
     *
     * @param session The WebSocket session that was closed.
     * @param status  The close status indicating the reason for closure.
     * @throws IOException If an error occurs while sending a response.
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
        BroadcastService.unregisterSession(session.getId());
        //        // Remove session's message queue
//        messageQueues.remove(session.getId());
//
//        // Notify the game server that the player has left
//        GameServerResponse gameServerResponse = this.gameServer.leaveServer(session.getId());
//        RequiredBroadcastInfo requiredBroadcastInfo = gameServerResponse.requiredBroadcastInfo;
//        GameServerMessage gameServerMessage = gameServerResponse.payload;
//
//        // Create a message to inform other players
//        TextMessage textMessage = new TextMessage(JsonUtil.toJson(gameServerMessage));
//
//        // Send the update to all specified recipients
//        for (String sessionId : requiredBroadcastInfo.recipients) {
//            BlockingQueue<TextMessage> queue = messageQueues.get(sessionId);
//
//            queue.offer(textMessage);
//        }

    }

}