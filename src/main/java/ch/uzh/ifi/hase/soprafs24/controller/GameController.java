package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/game/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Long createGame() {
        Game game = gameService.createGame();
        return game.getGameId();
    }

    @MessageMapping("/game/{gameId}/start")
    public void startGame(@DestinationVariable Long gameId, @Payload Long userId1, @Payload Long userId2) {
        Game game = gameService.startGame(gameId,userId1, userId2);
        // The GameStateDTO should include all the necessary information to start the game on the client side
       // GameStateDTO gameStateDTO  = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
        GameStateDTO gameStateDTO = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
        // Broadcast the initial game state to all players in the game
        messagingTemplate.convertAndSend("/topic/game/gameState/" + game.getGameId(), gameStateDTO);
    }

    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId, @Payload MoveDTO move) {
        // should be secured to ensure that only players from the specific game can make moves
        gameService.processMove(gameId,move);
        Game updatedGame = gameService.retrieveGameState(gameId);
        //Game game = gameService.playCard(gameId, move.getPlayerId(), move.getCardId(), move.getPosition());

        GameStateDTO gameStateDTO  = DTOMapper.INSTANCE.convertEntityToGameStateDTO(updatedGame);
        // Update all clients with the new game state after a move has been made
        messagingTemplate.convertAndSend("/topic/game/gameUpdate/" + gameId, gameStateDTO);
    }

    // Additional methods like endGame, surrender, etc. can be added here.

    // Helper methods
}