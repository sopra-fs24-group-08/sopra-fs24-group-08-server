package ch.uzh.ifi.hase.soprafs24.EventListener;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameCleanupService;
import ch.uzh.ifi.hase.soprafs24.service.util.GameEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GameCleanupListener {
    private final GameCleanupService gameCleanupService;
    private final GameEventService gameEventService;
    private static final Logger logger = LoggerFactory.getLogger(GameCleanupListener.class);

    public GameCleanupListener(GameCleanupService gameCleanupService, GameEventService gameEventService) {
        this.gameCleanupService = gameCleanupService;
        this.gameEventService = gameEventService;
    }

    @EventListener
    public void onGameCleanup(GameCleanupEvent event) {
        Game game = event.getGame();
        if (game != null) {
            System.out.println("Preparing game end data");
            // Prepare game end data before cleanup
            Map<Long, GameStateDTO> gameStateDTOs = gameCleanupService.prepareGameEndData(game);

            System.out.println("About to begin cleaning");
            gameCleanupService.cleanupGameData(game);

            System.out.println("Notifying players about game end");
            // Pass the pre-prepared DTOs for notification
            gameEventService.handleGameEnd(new GameEndEvent(this, game, game.getWinner(), game.getLoser(), gameStateDTOs));
        } else {
            logger.error("Received cleanup event for a null game.");
        }
    }
}
