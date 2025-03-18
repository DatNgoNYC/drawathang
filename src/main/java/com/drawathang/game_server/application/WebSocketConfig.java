package com.drawathang.game_server.application;

import com.drawathang.game_server.communication.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures WebSocket support for the game server.
 * <p>
 * This class enables WebSocket communication and registers the WebSocket handler
 * to manage real-time interactions between players.
 * <p>
 * The WebSocket endpoint is exposed at `/game-server`.
 */
@Configuration // Marks this class as a Spring configuration class
@EnableWebSocket // Enables WebSocket support in the Spring application
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    /**
     * Constructor for WebSocketConfig.
     *
     * @param webSocketHandler The WebSocket handler responsible for managing
     *                         incoming WebSocket connections and messages.
     */
    public WebSocketConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Registers the WebSocket handler to the given endpoint.
     *
     * @param registry The registry to add WebSocket handlers.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/game-server").setAllowedOrigins("*");
    }
}