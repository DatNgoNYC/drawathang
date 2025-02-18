package com.drawathang.game_server;

import com.drawathang.game_server.application.GameServerApplication;
import com.drawathang.game_server.contract.ClientMessageProtocol;
import com.drawathang.game_server.contract.ClientMessageType;
import com.drawathang.game_server.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GameServerApplication.class)
public class GameServerApplicationTests {



    @LocalServerPort
    private int port;

    @Test
    void testWebSocketConnection() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestWebSocketHandler handler1 = new TestWebSocketHandler();
        TestWebSocketHandler handler2 = new TestWebSocketHandler();

        CompletableFuture<WebSocketSession> session1Future = client.execute(handler1, new WebSocketHttpHeaders(), uri);
        CompletableFuture<WebSocketSession> session2Future = client.execute(handler2, new WebSocketHttpHeaders(), uri);

        WebSocketSession session1 = session1Future.get();
        WebSocketSession session2 = session2Future.get();

        // ✅ Declare latches before sending messages
        CountDownLatch handler1Latch;
        CountDownLatch handler2Latch;

        // ✅ Instantiate session1's latch.
        handler1Latch = new CountDownLatch(1);
        handler1.setLatch(handler1Latch);

        // ✅ Send message and wait for handler1 to receive it
        String joinMessage = JsonUtil.toJson(new ClientMessageProtocol(ClientMessageType.JOIN_SERVER));
        session1.sendMessage(new TextMessage(joinMessage));
        assertTrue(handler1Latch.await(1, TimeUnit.SECONDS), "1 message should have been broadcast from the server");

        // ✅ Reset latches before sending session2's message
        handler1Latch = new CountDownLatch(1);
        handler1.setLatch(handler1Latch);
        handler2Latch = new CountDownLatch(1);
        handler2.setLatch(handler2Latch);

        session2.sendMessage(new TextMessage(joinMessage));
        assertTrue(handler1Latch.await(1, TimeUnit.SECONDS), "Another message should have been broadcast from the server");
        assertTrue(handler2Latch.await(1, TimeUnit.SECONDS), "Another message should have been broadcast from the server");

        // Assert received messages count
        assertEquals(2, handler1.getMessages().size(), "Handler1 should have received 2 messages");
        assertEquals(1, handler2.getMessages().size(), "Handler2 should have received 1 message");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());
        session1.close();
        session2.close();
//        Thread.sleep(300);
    }

    @Test
    void testWebSocketConnection2() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestWebSocketHandler handler1 = new TestWebSocketHandler();
        TestWebSocketHandler handler2 = new TestWebSocketHandler();

        CompletableFuture<WebSocketSession> session1Future = client.execute(handler1, new WebSocketHttpHeaders(), uri);
        CompletableFuture<WebSocketSession> session2Future = client.execute(handler2, new WebSocketHttpHeaders(), uri);

        WebSocketSession session1 = session1Future.get();
        WebSocketSession session2 = session2Future.get();

        // Instantiate a global latch
        CountDownLatch latch = new CountDownLatch(3);

        // Attach latches
        handler1.setLatch(latch);
        handler2.setLatch(latch);

        // ✅ Send message and wait for handler1 to receive it
        String joinMessage = JsonUtil.toJson(new ClientMessageProtocol(ClientMessageType.JOIN_SERVER));
        session1.sendMessage(new TextMessage(joinMessage));
        session2.sendMessage(new TextMessage(joinMessage));
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Three messages should have been sent by the server");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());

        session1.close();
        session2.close();
//        Thread.sleep(200);
    }

    @Test
    void testWebSocketConnection3() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestWebSocketHandler handler1 = new TestWebSocketHandler();
        TestWebSocketHandler handler2 = new TestWebSocketHandler();
        TestWebSocketHandler handler3 = new TestWebSocketHandler();
        TestWebSocketHandler handler4 = new TestWebSocketHandler();

        CompletableFuture<WebSocketSession> session1Future = client.execute(handler1, new WebSocketHttpHeaders(), uri);
        CompletableFuture<WebSocketSession> session2Future = client.execute(handler2, new WebSocketHttpHeaders(), uri);
        CompletableFuture<WebSocketSession> session3Future = client.execute(handler3, new WebSocketHttpHeaders(), uri);
        CompletableFuture<WebSocketSession> session4Future = client.execute(handler4, new WebSocketHttpHeaders(), uri);

        WebSocketSession session1 = session1Future.get();
        WebSocketSession session2 = session2Future.get();
        WebSocketSession session3 = session3Future.get();
        WebSocketSession session4 = session4Future.get();

        // Instantiate a global latch
        CountDownLatch latch = new CountDownLatch(10);

        // Attach latches
        handler1.setLatch(latch);
        handler2.setLatch(latch);
        handler3.setLatch(latch);
        handler4.setLatch(latch);

        // ✅ Send message and wait for handler1 to receive it
        String joinMessage = JsonUtil.toJson(new ClientMessageProtocol(ClientMessageType.JOIN_SERVER));
        session1.sendMessage(new TextMessage(joinMessage));
//        Thread.sleep(100);
        session2.sendMessage(new TextMessage(joinMessage));
//        Thread.sleep(100);
        session3.sendMessage(new TextMessage(joinMessage));
//        Thread.sleep(100);
        session4.sendMessage(new TextMessage(joinMessage));
        assertTrue(latch.await(1, TimeUnit.SECONDS), "10 messages should have been sent by the server");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());
        System.out.println(handler3.getMessages());
        System.out.println(handler4.getMessages());
    }

}

class TestWebSocketHandler extends TextWebSocketHandler {
    private final List<String> messages = new CopyOnWriteArrayList<>();
    private CountDownLatch latch;
    private final CountDownLatch connectionLatch = new CountDownLatch(1);

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        connectionLatch.countDown(); // ✅ Release waiting threads
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