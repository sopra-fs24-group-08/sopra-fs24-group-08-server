/*
package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {

    private final Map<String, String> waitingPlayers = new ConcurrentHashMap<>();

    public MatchmakingResult joinPlayer(String gameId) {
        // Example logic for pairing
        waitingPlayers.put(gameId, "waiting");
        if (waitingPlayers.size() >= 2) {
            String gameSession = "game-" + UUID.randomUUID().toString();
            // remove two players from the map and create a game
            waitingPlayers.remove(gameId);
            return new MatchmakingResult(true, gameSession);
        }
        return new MatchmakingResult(false, null);
    }
}
*/
