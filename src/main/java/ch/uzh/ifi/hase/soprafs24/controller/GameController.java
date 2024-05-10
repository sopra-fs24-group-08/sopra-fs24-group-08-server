package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameResultRequest;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.TurnDecisionDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GameController {

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

    @GetMapping("/game/{gameId}/{playerId}/start/test")
    @ResponseStatus(HttpStatus.OK)
    public GameStateDTO startTestGame(@PathVariable Long gameId, @PathVariable Long playerId) {
        Game game = gameService.getGame(gameId);
        GameStateDTO playerSpecificState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, playerId);

        // Print the state for debugging purposes
        System.out.println("You are about to receive this player-specific-state!! " + playerSpecificState);
        // messagingTemplate.convertAndSend("/topic/game/" + gameId + "/" + playerId, playerSpecificState);

        // Return the GameStateDTO for the REST call response
        return playerSpecificState;
    }

    @PostMapping("/game/{gameId}/{playerId}/start")
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId, @PathVariable Long playerId) {
        Game game = gameService.getGame(gameId);
        GameStateDTO playerSpecificState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, playerId);
        // Send state update to all clients (or just the relevant client) via WebSocket
        System.out.println("You are about to receive this player-specific-state!!" + playerSpecificState);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/" + playerId, playerSpecificState);
    }


    @PostMapping("/game/{gameId}/{userId}/move/test")
    @ResponseStatus(HttpStatus.OK)
    public GameStateDTO handleMove(@PathVariable Long gameId, @RequestBody MoveDTO move, @PathVariable Long userId) {
        System.out.println("Received move from userId: " + userId + " for gameId: " + gameId);

        // Process the move in the game service
        gameService.processMove(gameId, move);

        // Retrieve the updated game state for all players
        Game game = gameService.getGame(gameId);
        GameStateDTO gameStateDTO = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTO(game);

        System.out.println("Move has been processed and the updated game state is being returned.");
        return gameStateDTO;
    }



    //Use for Winner/Page directly after game but also for players to perhaps get some game result view of other people's game's
    @GetMapping("/game/{gameId}/result")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameResultRequest requestGameResult(@PathVariable Long gameId,@RequestHeader("Authorization") String authorization) {
        //userService.authorizeUser(authorization);
        System.out.println("Received verify result for game id: " + gameId+"by user with bearer token: " + authorization);
        GameResultRequest result = gameService.getGameMatchResult(gameId);
        System.out.println("Game result has been returned1: " + result);
        return result;
    }


    // /app/game/{gameId}/accept
   /* @MessageMapping("/game/{gameId}/accept")
    public void acceptGameInvitation(Long userId, @DestinationVariable Long gameId,) {
        gameService.acceptInvitation(userId, gameId);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/responses", "Game invitation accepted!");
    }   ///user/{userId}/queue/responses

    // /app/game/{gameId}/accept
    @MessageMapping("/game/{gameId}/decline")
    public void declineGameInvitation(Long userId, @DestinationVariable Long gameId) {
        gameService.declineInvitation(userId, gameId);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/responses", "Game invitation declined!");
    }   ///user/{userId}/queue/responses*/

    public void sendRequestWithGameId(FriendRequestDTO friendRequestDTO, Long gameId, String destination) {
        Map<String, Object> data = new HashMap<>();
        data.put("friendRequest", friendRequestDTO);
        data.put("gameId", gameId);
        data.put("toastId", gameId.toString());

        messagingTemplate.convertAndSend(destination, data);
    }

}