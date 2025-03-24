package com.drawathang.game_server.services;

import com.drawathang.game_server.services.domain.DrawEvent;
import com.drawathang.game_server.services.domain.Session;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomService {
    /**
     * Store active rooms on the game server by their room IDs.
     */
    private final ConcurrentHashMap<String, Room> roomsMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> playersToRoomMap = new ConcurrentHashMap<>();

    public void createRoom(Session session, String roomName) {
        Room room = new Room(roomName, session);
        this.roomsMap.put(room.getId(), room);
        this.playersToRoomMap.put(session.getSessionId(), room.getId());

    }

    public void joinRoom(Session session, String roomId) {
        Room room = this.roomsMap.get(roomId);
        room.join(session);
        this.playersToRoomMap.put(session.getSessionId(), room.getId());

    }

    public Session leaveRoom(String sessionId) {
        String roomId = this.playersToRoomMap.get(sessionId);
        Room room = this.roomsMap.get(roomId);

        Session removedPlayer = room.leave(sessionId);

        if (room.getPlayers().isEmpty()) {
            this.roomsMap.remove(roomId);
        }

        return removedPlayer;

    }


    public List<Map<String, Object>> getRoomsInfo() {
        return roomsMap.values().stream()
                .map(r -> Map.of(
                        "roomId", r.getId(),
                        "roomName", r.getName(),
                        "participantCount", r.getPlayers().size()
                ))
                .toList();
    }

    public void submitGuess(String sessionId, String guess) {
        String roomId = this.playersToRoomMap.get(sessionId);
        Room room = this.roomsMap.get(roomId);
        room.submitGuess(sessionId, guess);

    }

    public void submitDrawEvent(String sessionId, DrawEvent drawEvent) {

    }
}
