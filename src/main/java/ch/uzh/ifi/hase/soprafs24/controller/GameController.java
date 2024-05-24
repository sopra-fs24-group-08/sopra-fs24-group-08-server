package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameResultRequest;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.exceptions.GameNotFoundException;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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



    @PostMapping("/game/{gameId}/{playerId}/start")
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId, @PathVariable Long playerId) {
        Game game = gameService.getGame(gameId);
        gameService.getPlayerById(playerId);
        GameStateDTO playerSpecificState = gameService.getGameStateForPlayer(game,playerId);
        // Send state update to all clients (or just the relevant client) via WebSocket
        System.out.println("You are about to receive this player-specific-state!!" + playerSpecificState);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/" + playerId, playerSpecificState);
    }





    //Use for Winner/Page directly after game but also for players to perhaps get some game result view of other people's game's
    @GetMapping("/game/{gameId}/result")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<GameResultRequest> requestGameResult(@PathVariable Long gameId, @RequestHeader("Authorization") String authorization) {
        try {
            // userService.authorizeUser(authorization); // Uncomment later and ensure it throws an appropriate exception if unauthorized
            System.out.println("Received verify result for game id: " + gameId + " by user with bearer token: " + authorization);
            GameResultRequest result = gameService.getGameMatchResult(gameId);
            System.out.println("Game result will be returned to the player: " + result);

            return ResponseEntity.ok(result);

        } catch (GameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalStateException ex) {
            if (ex.getMessage().contains("not in an ongoing state")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            } else if (ex.getMessage().contains("incomplete")) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(null);
            }
            return ResponseEntity.internalServerError().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build(); // General catch-all for other unhandled errors
        }
    }

    @GetMapping("/winCount/{userId}")
    public Long getWinCount(@PathVariable Long userId) {
        return gameService.getWinCountForUser(userId);
    }

}