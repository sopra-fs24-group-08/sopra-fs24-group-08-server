package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.TurnDecisionDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @MessageMapping("/game/update")
    @SendTo("/topic/game/{gameId}")
    public Game updateGame(@Payload Game game) {
        gameService.updateGame(game);
        return game;
    }

//    @PutMapping("/game/{gameId}/testStart")
//    @ResponseBody
//    public GameStateDTO startTestGame(@PathVariable("gameId") Long gameId, @RequestBody GameStartRequestDTO request) {
//        Long userId1 = request.getUserId1();
//        Long userId2 = request.getUserId2();
//        System.out.println("TestStartIDsrequestworked");
//        Game game = gameService.startGame(gameId,userId1, userId2);
//        // The GameStateDTO should include all the necessary information to start the game on the client side
//        // GameStateDTO gameStateDTO  = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
//        GameStateDTO gameStateDTO = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
//        return gameStateDTO;
//    }


//    @MessageMapping("/game/{gameId}/start")
//    public void startGame(@DestinationVariable Long gameId, @Payload Long userId1, @Payload Long userId2) {
//        Game game = gameService.startGame(gameId,userId1, userId2);
//        // The GameStateDTO should include all the necessary information to start the game on the client side
//       // GameStateDTO gameStateDTO  = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
//        GameStateDTO gameStateDTO = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
//        // Broadcast the initial game state to all players in the game
//        messagingTemplate.convertAndSend("/topic/game/gameState/" + game.getGameId(), gameStateDTO);
//    }
        //"/topic/game/{gameId}/gameState,gameStateDTO)
    /*@MessageMapping("/game/start")
    public void startGame(@Payload GameStartRequestDTO request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        Game game = gameService.startGame(request.getGameId(), Long.parseLong(userId));
        GameStateDTO gameState = new GameStateDTO(game);  // Assuming a method to create DTOs
        messagingTemplate.convertAndSendToUser(userId, "/queue/game-state", gameState);
    }*/

    /*@MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId, @Payload MoveDTO move) {

        // should be secured to ensure that only players from the specific game can make moves
        gameService.processMove(gameId,move);
        Game updatedGame = gameService.retrieveGameState(gameId);
        //Game game = gameService.playCard(gameId, move.getPlayerId(), move.getCardId(), move.getPosition());

        GameStateDTO gameStateDTO  = DTOMapper.INSTANCE.convertEntityToGameStateDTO(updatedGame);
        // Update all clients with the new game state after a move has been made
        messagingTemplate.convertAndSend("/topic/game/gameUpdate/" + gameId, gameStateDTO);
    }
*/
  @PostMapping("/game/{gameId}/start")
  @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Long gameId,TurnDecisionDTO decision) {
      if (!gameService.verifyTurnDecision(gameId, decision.getStarterPlayerId(), decision.getOtherPlayerId())) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid game start decision");
      }

      Game game = gameService.startMatchedGame(gameId, decision.getStarterPlayerId(), decision.getOtherPlayerId());
      // Broadcast the initial game state to all players in the game
      game.getPlayers().forEach(player -> {
          GameStateDTO playerSpecificState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, player.getUser().getId());
          messagingTemplate.convertAndSendToUser(player.getUser().getId().toString(), "/queue/game-state", playerSpecificState);
      });
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


    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId, @Payload MoveDTO move, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("Checking"+ headerAccessor);
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        Game updatedGame = gameService.processMove(gameId, move, Long.parseLong(userId));
        List<Player> players = gameService.getPlayersbygameId(gameId);
        for (Player player : players) {
            Long playerId =   player.getId();
            GameStateDTO gameStateDTO = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(updatedGame,playerId);

            messagingTemplate.convertAndSendToUser(playerId.toString(), "/queue/game", gameStateDTO);
        }

    }

    /*@MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId,@Payload MoveDTO move, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        gameService.processMove(gameId, move, Long.parseLong(userId));
        Game updatedGame = gameService.retrieveGameState(gameId);
        GameStateDTO gameState = new GameStateDTO(updatedGame); // Assuming a method to create DTOs
        messagingTemplate.convertAndSendToUser(userId, "/queue/game-update", gameState);
    }*/

    // Additional methods like endGame, surrender, etc. can be added here.

    // Helper methods
}