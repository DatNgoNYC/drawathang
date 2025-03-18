package com.drawathang.game_server;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

class TestClientWebSocketHandler extends TextWebSocketHandler {
    private List<String> messages = new CopyOnWriteArrayList<>();
    private CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void resetMessages() {
        this.messages = new CopyOnWriteArrayList<>();
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        messages.add(message.getPayload());
        latch.countDown();
    }

    public List<String> getMessages() {
        return messages;
    }
}
