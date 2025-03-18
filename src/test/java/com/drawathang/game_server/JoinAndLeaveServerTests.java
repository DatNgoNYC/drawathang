package com.drawathang.game_server;

import com.drawathang.game_server.application.GameServerApplication;
import com.drawathang.game_server.contract.ClientMessage;
import com.drawathang.game_server.contract.ClientMessageType;
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
public class JoinAndLeaveServerTests {

    @LocalServerPort
    private int port;

    @Test
    void testWebSocketConnection() throws Exception {
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
        session1.close();
        session2.close();
    }

    @Test
    void testInstantaneousJoins() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestClientWebSocketHandler handler1 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler2 = new TestClientWebSocketHandler();

        WebSocketSession session1 = client.execute(handler1, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session2 = client.execute(handler2, new WebSocketHttpHeaders(), uri).get();

        // Instantiate a global latch
        CountDownLatch latch = new CountDownLatch(3);

        // Attach latches
        handler1.setLatch(latch);
        handler2.setLatch(latch);

        // ✅ Send message and wait for handler1 to receive it
        String joinMessage = JsonUtil.toJson(new ClientMessage(ClientMessageType.JOIN_SERVER));
        session1.sendMessage(new TextMessage(joinMessage));
        session2.sendMessage(new TextMessage(joinMessage));
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Three messages should have been sent by the server");

        System.out.println(handler1.getMessages());
        System.out.println(handler2.getMessages());

        session1.close();
        session2.close();
    }

    @Test
    void testInstantaneousJoins2() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestClientWebSocketHandler handler1 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler2 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler3 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler4 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler5 = new TestClientWebSocketHandler();

        WebSocketSession session1 = client.execute(handler1, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session2 = client.execute(handler2, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session3 = client.execute(handler3, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session4 = client.execute(handler4, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session5 = client.execute(handler5, new WebSocketHttpHeaders(), uri).get();

        // Instantiate a global latch
        CountDownLatch latch = new CountDownLatch(15);

        // Attach latches
        handler1.setLatch(latch);
        handler2.setLatch(latch);
        handler3.setLatch(latch);
        handler4.setLatch(latch);
        handler5.setLatch(latch);

        // ✅ Send the instantaneous messages and wait for handler to receive it
        String joinMessage = JsonUtil.toJson(Map.of("type", "JOIN_SERVER"));
        session1.sendMessage(new TextMessage(joinMessage));
        session2.sendMessage(new TextMessage(joinMessage));
        session3.sendMessage(new TextMessage(joinMessage));
        session4.sendMessage(new TextMessage(joinMessage));
        session5.sendMessage(new TextMessage(joinMessage));

        assertTrue(latch.await(1, TimeUnit.SECONDS), "15 messages should have been sent by the server");

        System.out.println("Handler 1:");
        System.out.println(handler1.getMessages());
        System.out.println("\nHandler 2:");
        System.out.println(handler2.getMessages());
        System.out.println("\nHandler 3:");
        System.out.println(handler3.getMessages());
        System.out.println("\nHandler 4:");
        System.out.println(handler4.getMessages());
        System.out.println("\nHandler 5:");
        System.out.println(handler5.getMessages());
        int messageCount = handler1.getMessages().size() + handler2.getMessages().size() + handler3.getMessages().size() + handler4.getMessages().size() + handler5.getMessages().size();
        System.out.println("\nmessageCount: " + messageCount + "\n");
    }

    @Test
    void testJoinsAndLeaves() throws Exception {
        // Set up websocket client sessions
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI("ws://localhost:" + port + "/game-server");

        TestClientWebSocketHandler handler1 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler2 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler3 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler4 = new TestClientWebSocketHandler();
        TestClientWebSocketHandler handler5 = new TestClientWebSocketHandler();

        WebSocketSession session1 = client.execute(handler1, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session2 = client.execute(handler2, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session3 = client.execute(handler3, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session4 = client.execute(handler4, new WebSocketHttpHeaders(), uri).get();
        WebSocketSession session5 = client.execute(handler5, new WebSocketHttpHeaders(), uri).get();

        // Instantiate a global latch
        CountDownLatch latch = new CountDownLatch(15);

        // Attach latches
        handler1.setLatch(latch);
        handler2.setLatch(latch);
        handler3.setLatch(latch);
        handler4.setLatch(latch);
        handler5.setLatch(latch);

        // ✅ Send the instantaneous messages and wait for handler to receive it
        String joinMessage = JsonUtil.toJson(Map.of("type", "JOIN_SERVER"));
        session1.sendMessage(new TextMessage(joinMessage));
        session2.sendMessage(new TextMessage(joinMessage));
        session3.sendMessage(new TextMessage(joinMessage));
        session4.sendMessage(new TextMessage(joinMessage));
        session5.sendMessage(new TextMessage(joinMessage));

        assertTrue(latch.await(1, TimeUnit.SECONDS), "15 messages should have been sent by the server");

        System.out.println("Handler 1:");
        System.out.println(handler1.getMessages());
        System.out.println("\nHandler 2:");
        System.out.println(handler2.getMessages());
        System.out.println("\nHandler 3:");
        System.out.println(handler3.getMessages());
        System.out.println("\nHandler 4:");
        System.out.println(handler4.getMessages());
        System.out.println("\nHandler 5:");
        System.out.println(handler5.getMessages());
        int messageCount = handler1.getMessages().size() + handler2.getMessages().size() + handler3.getMessages().size() + handler4.getMessages().size() + handler5.getMessages().size();
        System.out.println("\nmessageCount: " + messageCount + "\n");

        // Instantiate a new global latch for the leave message
        CountDownLatch leaveLatch = new CountDownLatch(4);

        // Attach latches
        handler1.setLatch(leaveLatch);
        handler2.setLatch(leaveLatch);
        handler3.setLatch(leaveLatch);
        handler4.setLatch(leaveLatch);
        handler5.setLatch(leaveLatch);

        // reset message history
        handler2.resetMessages();
        handler3.resetMessages();
        handler4.resetMessages();
        handler5.resetMessages();

        // Send out leave message and wait for handler of other sessions to receive it
        String leaveMessage = JsonUtil.toJson(Map.of("type", "LEAVE_SERVER"));
        session1.sendMessage(new TextMessage(leaveMessage));

        assertTrue(leaveLatch.await(1, TimeUnit.SECONDS), "4 total messages should be sent to the 4 remaining sessions");

        System.out.println("\nHandler 2:");
        System.out.println(handler2.getMessages());
        System.out.println("\nHandler 3:");
        System.out.println(handler3.getMessages());
        System.out.println("\nHandler 4:");
        System.out.println(handler4.getMessages());
        System.out.println("\nHandler 5:");
        System.out.println(handler5.getMessages());
        int messageCount2 = handler2.getMessages().size() + handler3.getMessages().size() + handler4.getMessages().size() + handler5.getMessages().size();
        System.out.println("\nmessageCount: " + messageCount2 + "\n");

        CountDownLatch leaveLatch2 = new CountDownLatch(3);
        handler3.setLatch(leaveLatch2);
        handler4.setLatch(leaveLatch2);
        handler5.setLatch(leaveLatch2);

        session2.sendMessage(new TextMessage(leaveMessage));

        assertTrue(leaveLatch2.await(1, TimeUnit.SECONDS));

        System.out.println("\nHandler 3:");
        System.out.println(handler3.getMessages());
        System.out.println("\nHandler 4:");
        System.out.println(handler4.getMessages());
        System.out.println("\nHandler 5:");
        System.out.println(handler5.getMessages());
        int messageCount3 = handler3.getMessages().size() + handler4.getMessages().size() + handler5.getMessages().size();
        System.out.println("\nmessageCount: " + messageCount3 + "\n");

    }


}

