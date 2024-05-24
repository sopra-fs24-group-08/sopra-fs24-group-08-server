package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GridSquareDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DTOSocketMapperTest {

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    public void setup() {
        game = new Game();
        game.setGameId(1L);

        player1 = new Player();
        player1.setId(1L);
        player1.setHand(Arrays.asList(new Card(), new Card()));
        player1.setScore(10);

        player2 = new Player();
        player2.setId(2L);
        player2.setHand(List.of(new Card()));
        player2.setScore(5);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        game.setPlayers(players);

        Board board = new Board();
        board.setGridSquares(Arrays.asList(new GridSquare(), new GridSquare()));
        game.setBoard(board);
    }

    @Test
    public void testConvertEntityToGameStateDTO() {
        Game game = new Game();
        game.setGameId(1L);

        //NOT DONE YET ADD MORE ATTRIBUTES TO CHECK
        GameStateDTO gameStateDTO = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTO(game);

        assertEquals(game.getGameId(), gameStateDTO.getGameId());
    }

    @Test
    public void testConvertEntityToCardDTO() {
        Card card = new Card();
        card.setId(1L);
        card.setPoints(3);
        card.setColor("Red");

        CardDTO cardDTO = DTOSocketMapper.INSTANCE.convertEntityToCardDTO(card);
        //NOT DONE YET ADD MORE ATTRIBUTES TO CHECK

        assertEquals(card.getId(), cardDTO.getId());
        assertEquals(card.getColor(), cardDTO.getColor());
        assertEquals(card.getPoints(), cardDTO.getPoints());
    }

    @Test
    public void ConvertEntityToGridSquareDTO() {
        GridSquare square = new GridSquare();
        square.setId(1L);
        square.setColor("Blue");
        square.setCardPile(true);  // This should trigger the card pile logic
        square.setCards(Arrays.asList(new Card(), new Card()));

        GridSquareDTO squareDTO = DTOSocketMapper.INSTANCE.convertEntityToGridSquareDTO(square);
        //NOT DONE YET ADD MORE ATTRIBUTES TO CHECK

        assertEquals(square.getId(), squareDTO.getId());
        assertEquals(square.getColor(), squareDTO.getColor());
        assertEquals(square.isOccupied(), squareDTO.isOccupied());
    }

    @Test
    public void ConvertEntityToGameStateDTOForPlayer() {
        GameStateDTO gameStateDTO = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, player1.getId());
        //NOT DONE YET ADD MORE ATTRIBUTES TO CHECK

        assertNotNull(gameStateDTO);
        assertEquals(2, gameStateDTO.getPlayerHand().size());
        assertEquals(player2.getScore(), gameStateDTO.getOpponentScore());
    }
}
