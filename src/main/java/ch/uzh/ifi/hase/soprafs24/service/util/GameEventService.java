package ch.uzh.ifi.hase.soprafs24.service.util;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameEndEvent;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.exceptions.IncompleteGameDataException;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void handleGameEnd(GameEndEvent event) {
        Game game = event.getGame();
        Map<Long, GameStateDTO> gameStateDTOs = event.getGameStateDTOs();

        if (game.getWinnerUser() == null || game.getLoserUser() == null) {
            throw new IncompleteGameDataException("Game data is incomplete when handling game end.");
        }

        GameStateDTO stateForWinner = gameStateDTOs.get(game.getWinnerUser().getId());
        GameStateDTO stateForLoser = gameStateDTOs.get(game.getLoserUser().getId());

        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/" + game.getWinnerUser().getId(), stateForWinner);
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/" + game.getLoserUser().getId(), stateForLoser);
    }
}