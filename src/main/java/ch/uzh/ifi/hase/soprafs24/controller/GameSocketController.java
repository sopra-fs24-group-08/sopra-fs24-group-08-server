package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.SurrenderConfirmation;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;

@Controller
public class GameSocketController {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    public GameSocketController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/game/update")
    @SendTo("/topic/game/{gameId}")
    public Game updateGame(@Payload Game game) {
        gameService.updateGame(game);
        return game;
    }

    /**Place MoveDTO= {
     "playerId": "X", -> Has to match up with game.getCurrentTurnPlayerId()
     "cardId": "X",
     "position": "X",0-8,boardService converts into actual GridSquareId
     "moveType": "PLACE"
     }
     * */

    /**Place MoveDTO= {
     "playerId": "X", -> Has to match up with game.getCurrentTurnPlayerId()
     "cardId": "",
     "position": "X"0-8, ->Has to match up with  board.getCardPileSquare().getId()
     "moveType": "Draw"
     }
     * */
    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload MoveDTO move, SimpMessageHeaderAccessor headerAccessor) {
        String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");
        System.out.println("Player with token: " + sessionToken + "is making a move");
        gameService.processMove(Long.parseLong(gameId), move);
        System.out.println("Move has been Processed and broadcast to all players");

    }
    /** SurrenderConfirmation:
     *  private Long playerId;
     *  private Boolean surrender;
     * */

    @MessageMapping("/game/{gameId}/surrender")
    public void handlePlayerSurrender(@DestinationVariable Long gameId, @Payload SurrenderConfirmation surrenderConfirmation) {
        Long surrenderingPlayerId = surrenderConfirmation.getPlayerId();
        gameService.handlePlayerSurrender(gameId, surrenderingPlayerId);
    }

}