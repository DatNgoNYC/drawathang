package com.drawathang.game_server.services;

import com.drawathang.game_server.communication.BroadcastService;
import com.drawathang.game_server.services.domain.Session;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * GameServer manages active sessions and game state.
 * Serving as the main class/entrypoint to our business logic .
 */
@Service // Another bean that spring boot manages
public class GameServer implements IGameServer {

    /**
     * Stores active sessions in the lobby by their session IDs.
     */
    private final ConcurrentHashMap<String, Session> sessionsInLobby = new ConcurrentHashMap<>();

    /**
     * Store active rooms on the game server by their room IDs.
     */
    private final ConcurrentHashMap<String, Room> roomsMap = new ConcurrentHashMap<>();

    /**
     * Stores count of sessions on the game server.
     */
    private final AtomicInteger totalSessionsCount = new AtomicInteger(0);

    /**
     * Atomic counter for generating unique event timestamps.
     */
    private final AtomicLong atomicTimestamp = new AtomicLong(0);


    public void joinServer(String sessionId) {
        synchronized (sessionsInLobby) {

            // MAIN TRANSACTION:
            // Create a new session for the player and store it
            Session session = new Session(sessionId);
            sessionsInLobby.put(sessionId, session);

            // BROADCAST THE UPDATE:
            long timestamp = atomicTimestamp.incrementAndGet();
            List<String> recipients = List.copyOf(sessionsInLobby.keySet());

            BroadcastService.broadcast(recipients, Map.of(
                    "timestamp", timestamp,
                    "event", "USER_JOINED",
                    "sessionsCount", totalSessionsCount.incrementAndGet()
            ));
        }

    }

    public void leaveServer(String sessionId) {
        synchronized (sessionsInLobby) {

            // MAIN TRANSACTION:
            // Remove the session
            sessionsInLobby.remove(sessionId);
            totalSessionsCount.decrementAndGet();

            // BROADCAST THE UPDATE:
            long timestamp = atomicTimestamp.incrementAndGet();
            List<String> recipients = List.copyOf(sessionsInLobby.keySet());

            BroadcastService.broadcast(recipients, Map.of(
                    "timestamp", timestamp,
                    "event", "USER_LEFT",
                    "sessionsCount", totalSessionsCount.get()
            ));
        }
    }

    public void setUsername(String sessionId, String username) {
        Session session = this.sessionsInLobby.get(sessionId);
        session.setUsername(username);

        BroadcastService.broadcast(List.of(session.getSessionId()), Map.of(
                "event", "USERNAME_UPDATED"
        ));
    }

    public void createRoom(String sessionId, String roomName) {
        Session session = this.sessionsInLobby.remove(sessionId);

        Room room = new Room(roomName, session);
        this.roomsMap.put(room.getId(), room);

        // Collect room info for broadcast
        List<Map<String, Object>> roomsInfo = roomsMap.values().stream()
                .map(r -> Map.of(
                        "roomId", r.getId(),
                        "roomName", r.getName(),
                        "participantCount", r.getPlayers().size()
                ))
                .toList();

        List<String> recipients = List.copyOf(sessionsInLobby.keySet());

        BroadcastService.broadcast(recipients, Map.of(
                "timestamp", this.atomicTimestamp.incrementAndGet(),
                "event", "ROOM_CREATED",
                "sessionsCount", totalSessionsCount.get(),
                "roomsInfo", roomsInfo
        ));
    }

    public void leaveRoom(String sessionId, String roomId) {
        Room room = roomsMap.get(roomId);
        Session session = room.leave(sessionId);
        this.sessionsInLobby.put(sessionId, session);

        List<String> recipients = List.copyOf(sessionsInLobby.keySet());

        // Collect room info for broadcast
        List<Map<String, Object>> roomsInfo = roomsMap.values().stream()
                .map(r -> Map.of(
                        "roomId", r.getId(),
                        "roomName", r.getName(),
                        "participantCount", r.getPlayers().size()
                ))
                .toList();

        BroadcastService.broadcast(recipients, Map.of(
                "timestamp", this.atomicTimestamp.incrementAndGet(),
                "event", "ROOM_UPDATE",
                "sessionsCount", totalSessionsCount.get(),
                "roomsInfo", roomsInfo
        ));    }

}
