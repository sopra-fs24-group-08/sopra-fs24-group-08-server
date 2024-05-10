package ch.uzh.ifi.hase.soprafs24.service.util;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameEndEvent;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;

    // Constructor injection is recommended because it allows for immutable field declarations and
    // enforces that dependencies are specified clearly
    public GameEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void handleGameEnd(GameEndEvent event) {
        // Ensure that this method only processes fully updated and consistent game states
        Game game = event.getGame();
        if (game.getWinner() == null || game.getLoser() == null) {
            throw new IllegalStateException("Game data is incomplete when handling game end.");
        }

        GameStateDTO stateForWinner = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, game.getWinner().getId());
        GameStateDTO stateForLoser = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, game.getLoser().getId());

        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/" + game.getWinner().getId(), stateForWinner);
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/" + game.getLoser().getId(), stateForLoser);
    }
}