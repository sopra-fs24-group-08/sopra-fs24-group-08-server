package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChooseStartDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaceCardDTO;

@RestController
@RequestMapping("/games") // Base path for game-related actions
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameGetDTO createGame() {
        Game game = gameService.createNewGame();
        return DTOMapper.INSTANCE.convertGameToGameGetDTO(game);
    }

    @PostMapping("/{gameId}/placeCard")
    public ResponseEntity<?> placeCard(@PathVariable Long gameId, @RequestBody PlaceCardDTO placeCardDTO) {
        try {
            Game updatedGame = gameService.placeCard(gameId, placeCardDTO.getPlayerId(), placeCardDTO.getCardId(), placeCardDTO.getRow(), placeCardDTO.getColumn());
            return ResponseEntity.ok(DTOMapper.INSTANCE.convertGameToGameGetDTO(updatedGame));
        } catch (NotYourTurnException e) {
            // If it's not the player's turn, catch the exception and return to the 403 forbidden state
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/tossCoin")
    public GameGetDTO tossCoin(@PathVariable Long gameId) {
        Game game = gameService.tossCoin(gameId);
        return DTOMapper.INSTANCE.convertGameToGameGetDTO(game);
    }

    @PostMapping("/{gameId}/chooseStartingPlayer")
    public GameGetDTO chooseStartingPlayer(@PathVariable Long gameId, @RequestBody ChooseStartDTO chooseStartDTO) {
        Game game = gameService.chooseStartingPlayer(gameId, chooseStartDTO.isPlayer1Starts());
        return DTOMapper.INSTANCE.convertGameToGameGetDTO(game);
    }


}

