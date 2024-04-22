package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Game;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {
    private Map<Long, Long> waitingUsers = new ConcurrentHashMap<>();
    @Autowired
    private GameService gameService;

    public MatchmakingResult joinPlayer(Long userId) {
        waitingUsers.put(userId, null);

        if (waitingUsers.size() >= 2) {
            Iterator<Long> iterator = waitingUsers.keySet().iterator();
            Long firstPlayerId = iterator.next();
            Long secondPlayerId = iterator.next();

            Game game = gameService.startGame(firstPlayerId, secondPlayerId);

            waitingUsers.remove(firstPlayerId);
            waitingUsers.remove(secondPlayerId);

            return new MatchmakingResult(true, game.getGameId(), firstPlayerId, secondPlayerId);
        }
        return new MatchmakingResult(false, null, null, null);
    }
}
