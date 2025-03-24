package com.drawathang.game_server.services;

import com.drawathang.game_server.communication.BroadcastService;
import com.drawathang.game_server.services.domain.Guess;
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
    private final List<Guess> guesses = new CopyOnWriteArrayList<>();
    private Session host;

    public Room(String name, Session host) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.players.add(host);
        this.host = host;

        this.broadcastRoomUpdate();
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
        Session removedPlayer = null;

        for (Session player : players)
            if (player.getSessionId().equals(sessionId)) {
                players.remove(player);
                removedPlayer = player;
            }
        this.broadcastRoomUpdate();
        return removedPlayer;
    }

    public void join(Session session) {
        this.players.add(session);

        this.broadcastRoomUpdate();
    }

    public void submitGuess(String sessionId, String guessText) {
        Session session = getSessionById(sessionId);
        if (session != null) {
            Guess guess = new Guess(sessionId, session.getUsername(), guessText);
            this.guesses.add(guess);
        }
        this.broadcastRoomUpdate();
    }

    public Session getSessionById(String sessionId) {
        for (Session session : this.players) {
            if (session.getSessionId().equals(sessionId)) {
                return session;
            }
        }
        return null;
    }

    public void broadcastRoomUpdate() {
        List<String> recipients = players.stream()
                .map(Session::getSessionId) // Extract session IDs
                .collect(Collectors.toList());

        List<Map<String, String>> guessData = guesses.stream()
                .map(guess -> Map.of(
                        "sessionId", guess.getSessionId(),
                        "userName", guess.getUsername(),
                        "guess", guess.getGuess()
                ))
                .collect(Collectors.toList());

        BroadcastService.broadcast(recipients, Map.of(
                "timestamp", this.atomicTimestamp.incrementAndGet(),
                "roomName", this.name,
                "players", this.players.stream()
                        .map(player -> Map.of(
                                "sessionId", player.getSessionId(),
                                "userName", player.getUsername()
                        ))
                        .collect(Collectors.toList()),
                "guesses", guessData)
        );
    }
}
