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

    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId, @Payload MoveDTO move, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("Checking"+ headerAccessor);
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        gameService.processMove(gameId, move, Long.parseLong(userId));
        System.out.println("Move has been Processed and broadcast to all players");
       /* List<Player> players = gameService.getPlayersbygameId(gameId);
        for (Player player : players) {
            Long playerId =   player.getId();
            GameStateDTO gameStateDTO = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(updatedGame,playerId);

            messagingTemplate.convertAndSend(playerId.toString(), "//game", gameStateDTO);
        }*/

    }

    @MessageMapping("/game/{gameId}/surrender")
    public void handlePlayerSurrender(@DestinationVariable Long gameId, @Payload SurrenderConfirmation surrenderConfirmation) {
        Long surrenderingPlayerId = surrenderConfirmation.getPlayerId();
        gameService.handlePlayerSurrender(gameId, surrenderingPlayerId);
    }

}