package com.drawathang.game_server;


import com.drawathang.game_server.application.GameServerApplication;
import com.drawathang.game_server.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GameServerApplication.class)
public class RoomServiceTests {

    @LocalServerPort
    private int port;

    @Test
    void testCreateRoom() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestClientWebSocketHandler handler1 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler2 = new TestClientWebSocketHandler();

        WebSocketSession session1 = client.execute(handler1, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session2 = client.execute(handler2, new WebSocketHttpHeaders(), uri).get();

        // ✅ Send join message for session 1
        String joinMessage = JsonUtil.toJson(Map.of("type", "JOIN_SERVER"));

        CountDownLatch handler1Latch = new CountDownLatch(1);
        handler1.setLatch(handler1Latch);

        session1.sendMessage(new TextMessage(joinMessage));
        assertTrue(handler1Latch.await(1, TimeUnit.SECONDS), "1 message should have been broadcast from the server");

        // ✅ Reset latches before sending session2's join message
        handler1Latch = new CountDownLatch(1);
        handler1.setLatch(handler1Latch);
        CountDownLatch handler2Latch = new CountDownLatch(1);
        handler2.setLatch(handler2Latch);

        session2.sendMessage(new TextMessage(joinMessage));
        assertTrue(handler1Latch.await(1, TimeUnit.SECONDS), "Another message should have been broadcast from the server");
        assertTrue(handler2Latch.await(1, TimeUnit.SECONDS), "Another message should have been broadcast from the server");

        // Assert received messages count
        assertEquals(2, handler1.getMessages().size(), "Handler1 should have received 2 messages");
        assertEquals(1, handler2.getMessages().size(), "Handler2 should have received 1 message");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());

        // create new latches for room creation command
        handler1Latch = new CountDownLatch(1);
        handler2Latch = new CountDownLatch(1);
        handler1.setLatch(handler1Latch);
        handler2.setLatch(handler2Latch);

        // Create Room Message
        String createRoomMessage = JsonUtil.toJson(Map.of("type", "CREATE_ROOM", "roomName", "testroom"));
        session2.sendMessage(new TextMessage(createRoomMessage));

        assertTrue(handler2Latch.await(1, TimeUnit.SECONDS), "Another room based message should have been broadcast from the server");
        assertTrue(handler1Latch.await(1, TimeUnit.SECONDS), "Another lobby message should have been broadcast from the server");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());
    }

    @Test
    void testSetUsername() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestClientWebSocketHandler handler1 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler2 = new TestClientWebSocketHandler();

        WebSocketSession session1 = client.execute(handler1, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session2 = client.execute(handler2, new WebSocketHttpHeaders(), uri).get();

        String joinMessage = JsonUtil.toJson(Map.of("type", "JOIN_SERVER"));

    }
}
