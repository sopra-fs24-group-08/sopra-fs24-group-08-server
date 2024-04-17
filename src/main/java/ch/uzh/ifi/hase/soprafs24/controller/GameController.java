package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @MessageMapping("/move")
    @SendTo("/topic/gameState")
    public Game makeMove(MoveDTO move) {
        // The controller calls GameService to handle the game logic.
        //convert Package to actual moves, then update
        gameService.updateGameState(move);
        Long gameId = 1L;
        // After updating the state, it retrieves the latest game state and sends it to subscribers.
        return gameService.getGame(gameId);
    }
}
