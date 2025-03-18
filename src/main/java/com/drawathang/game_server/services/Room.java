package com.drawathang.game_server.services;

import com.drawathang.game_server.communication.BroadcastService;
import com.drawathang.game_server.services.domain.Session;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Room {
    private final String id;
    private final String name;
    private final List<Session> players = new CopyOnWriteArrayList<>();
    private final AtomicLong atomicTimestamp = new AtomicLong(0);
    private Session host;

    public Room(String name, Session host) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.players.add(host);
        this.host = host;

        List<String> recipients = players.stream()
                .map(Session::getSessionId) // Extract session IDs
                .collect(Collectors.toList());

        BroadcastService.broadcast(recipients, Map.of(
                "timestamp", this.atomicTimestamp.incrementAndGet(),
                "roomName", this.name,
                "players", this.players.stream()
                        .map(player -> Map.of(
                                "sessionId", player.getSessionId(),
                                "userName", player.getUsername()
                        ))
                        .collect(Collectors.toList()))
        );
    }

    public String getId() {
        return this.id;
    }

    public Object getName() {
        return this.name;
    }

    public List<Session> getPlayers() {
        return this.players;
    }

    public Session leave(String sessionId) {
        this.players.remove();
    }
}
