package com.drawathang.game_server.services;

/**
 * Interface defining the contract for a GameServer.
 */
public interface IGameServer {

    /**
     * Handles a new player joining the server.
     *
     * @param sessionId The session ID of the player joining.
     */
    void joinServer(String sessionId);

    /**
     * Handles a player leaving the server.
     *
     * @param sessionId The session ID of the player leaving.
     */
    void leaveServer(String sessionId);

    void setUsername(String sessionId, String username);

    void createRoom(String sessionId, String roomName);

    void joinRoom(String sessionId, String roomId);

    void leaveRoom(String sessionId, String roomId);

    void submitGuess(String sessionId, String guess);

//
//    /* ROOM IMPLEMENTATION BELOW:  */
//
//    /**
//     * Handles a player creating a room.
//     *
//     * @param sessionId The session ID of the player creating the room.
//     * @param roomName The name of the room.
//     * @return A {@link GameServerResponse} containing broadcast info and the event payload.
//     */
//    GameServerResponse createRoom(String sessionId, String roomName);
//
//    GameServerResponse joinRoom(String sessionId, String roomId);
//
//    GameServerResponse leaveRoom(String sessionId, String roomId);
//
//    GameServerResponse submitGuess(String sessionId, String guess);
//
//    GameServerResponse submitDrawAction(String sessionId, DrawAction drawAction);
//
//    GameServerResponse startGame(String sessionId);
//
//    GameServerResponse pickWordToDraw(String sessionId, String word);

}