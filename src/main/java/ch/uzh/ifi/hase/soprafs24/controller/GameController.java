package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChooseStartDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaceCardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games") // Base path for game-related actions
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameGetDTO createGame() {
        Board board = gameService.createNewGame();
        return DTOMapper.INSTANCE.convertBoardToGameGetDTO(board);
    }

    @PostMapping("/{gameId}/placeCard")
    public ResponseEntity<?> placeCard(@PathVariable Long gameId, @RequestBody PlaceCardDTO placeCardDTO) {
        try {
            Card card = new Card(placeCardDTO.getColor(), placeCardDTO.getPoints());
            Board updatedBoard = gameService.placeCard(gameId, placeCardDTO.getPlayerId(), card, placeCardDTO.getPosition());
            return ResponseEntity.ok(DTOMapper.INSTANCE.convertBoardToGameGetDTO(updatedBoard));
        } catch (NotYourTurnException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/tossCoin")
    public GameGetDTO tossCoin(@PathVariable Long gameId) {
        Board board = gameService.tossCoin(gameId);
        return DTOMapper.INSTANCE.convertBoardToGameGetDTO(board);
    }

    @PostMapping("/{gameId}/chooseStartingPlayer")
    public GameGetDTO chooseStartingPlayer(@PathVariable Long gameId, @RequestBody ChooseStartDTO chooseStartDTO) {
        Board board = gameService.chooseStartingPlayer(gameId, chooseStartDTO.isPlayer1Starts());
        return DTOMapper.INSTANCE.convertBoardToGameGetDTO(board);
    }
}
