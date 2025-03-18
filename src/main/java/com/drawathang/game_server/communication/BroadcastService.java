package com.drawathang.game_server.communication;

import com.drawathang.game_server.contract.GameServerMessage;
import com.drawathang.game_server.util.JsonUtil;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class BroadcastService {
    /**
     * Stores message queues for each session to enable asynchronous message sending.
     */
    private static final ConcurrentHashMap<String, BlockingQueue<TextMessage>> messageQueues = new ConcurrentHashMap<>();


    /**
     * Registers a new session and starts a dedicated sender thread.
     */
    public static void registerSession(WebSocketSession session) {
        messageQueues.put(session.getId(), new LinkedBlockingQueue<>());

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

    /**
     * Removes a session from the broadcast system.
     */
    public static void unregisterSession(String sessionId) {
        messageQueues.remove(sessionId);
    }

    /**
     * Broadcasts a message to recipients.
     */
    public static void broadcast(List<String> recipients, Object payload) {
        TextMessage textMessage = new TextMessage(JsonUtil.toJson(payload));

        for (String sessionId : recipients) {
            BlockingQueue<TextMessage> queue = messageQueues.get(sessionId);

            queue.offer(textMessage);
        }
    }
}
