package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameCleanupOrchestrator {

    @Autowired
    private GameCleanupService gameCleanupService;

    public void orchestrateCleanup(Game game) {
        if (game.getGameStatus() == GameStatus.FINISHED) {
            gameCleanupService.cleanupGameData(game);
        } else {
            System.out.println("Attempted to cleanup game data for game ID " + game.getGameId() + " which is not finished.");
        }
    }
}
