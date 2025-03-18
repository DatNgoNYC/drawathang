package com.drawathang.game_server.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point to the Draw-a-Thang game server.
 * <p>
 * This class bootstraps the Spring Boot application and scans the base package
 * "com.drawathang.game_server" for components, configurations, and services.
 * <p>
 * Usage:
 * Run the main method to start the game server.
 */
@SpringBootApplication(scanBasePackages = "com.drawathang.game_server")
public class GameServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameServerApplication.class, args);
	}

}
