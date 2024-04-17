package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameSurrenderDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

public class MoveController {
    private final Logger log = LoggerFactory.getLogger(MoveController.class);


    @MessageMapping("/game/{gameId}/players/{playerId}/move")
    @SendTo("/topic/game/{gameId}/players/{playerId}/move")
    public MoveDTO RegisterMove(@DestinationVariable long gameId, @DestinationVariable long playerId, MoveDTO moveDTO) {
        return moveDTO;
    }

    @MessageMapping("/game/{gameId}/players/{playerId}/surrender")
    @SendTo("/topic/players/{playerId}/surrender")
    public GameSurrenderDTO Game(@DestinationVariable long gameId, @DestinationVariable long playerId, GameSurrenderDTO gameSurrenderDTO) {
        log.warn("Player has surrender:"+ playerId);
        return gameSurrenderDTO;
    }
}
