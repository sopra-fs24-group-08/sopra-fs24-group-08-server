package ch.uzh.ifi.hase.soprafs24.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveController {
    private final Logger log = LoggerFactory.getLogger(MoveController.class);

    /*//duplicate of whats in GameController, ignore for now
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
    }*/
}
