package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {

    private final Map<Long, Long> waitingUsers = new ConcurrentHashMap<>();
    @Autowired
    private GameRepository gameRepository;

    public MatchmakingResult joinPlayer(Long userId) {
        System.out.println("Attempting to add userId to waiting list: " + userId);

        // Null check for userId
        if (userId == null) {
            System.err.println("Error: Attempt to add null userId to waiting list");
            return new MatchmakingResult(false, null, null, null);
        }

        // Put the user in the waiting list
        if (waitingUsers.putIfAbsent(userId, System.currentTimeMillis()) == null) {
            System.out.println("UserId " + userId + " added to the matchmaking queue.");
        } else {
            System.out.println("UserId " + userId + " is already in the matchmaking queue.");
        }

        // Check if there are at least two players in the queue
        if (waitingUsers.size() >= 2) {
            Iterator<Long> iterator = waitingUsers.keySet().iterator();
            Long firstPlayerId = iterator.next();
            Long secondPlayerId = iterator.next();

            // Create a new game but don't initialize yet
            Game game = new Game();
            gameRepository.save(game); // Persist new game to the database

            // Clear the users from the waiting list
            waitingUsers.remove(firstPlayerId);
            waitingUsers.remove(secondPlayerId);

            return new MatchmakingResult(true, game.getGameId(), firstPlayerId, secondPlayerId);
        }

        return new MatchmakingResult(false, null, null, null);
    }
}
