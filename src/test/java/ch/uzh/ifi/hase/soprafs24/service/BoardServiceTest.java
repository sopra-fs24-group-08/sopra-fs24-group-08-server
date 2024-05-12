package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.repository.BoardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GridSquareRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.exceptions.NoCardsLeftException;
import ch.uzh.ifi.hase.soprafs24.exceptions.SquareOccupiedException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
public class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @MockBean
    private BoardRepository boardRepository;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private GridSquareRepository gridSquareRepository;

    @Test
    public void initializeBoard_saveBoard_returnBoard() {
      Board mockBoard = new Board();
      when(boardRepository.save(any(Board.class))).thenReturn(mockBoard);
      Board result = boardService.initializeAndSaveBoard();
      verify(boardRepository).save(any(Board.class));
      assertEquals("The returned board should be the same as the mocked board.", mockBoard, result);
    }

    @Test
    public void drawCard_validBoard_nonEmptyPile_returnCard() {
      Board mockBoard = new Board();
      mockBoard.initializeBoard();
      Card card = mockBoard.getCardPileSquare().getCards().get(0);
      Card result = boardService.drawCardFromPile(mockBoard);
      assertEquals("You should draw the first card.", card, result);
    }

    @Test
    public void drawCard_invalidBoard_throwException() {
      Board mockBoard = null;
      assertThrows(NoCardsLeftException.class, () -> boardService.drawCardFromPile(mockBoard));
    }

    @Test
    public void drawCard_EmptyPile_throwException() {
      Board mockBoard = new Board();
      mockBoard.initializeBoard();
      mockBoard.getCardPileSquare().getCards().clear();
      assertThrows(NoCardsLeftException.class, () -> boardService.drawCardFromPile(mockBoard));
    }

    @Test
    public void placeCard_validCard_validSquare() {
      Board mockBoard = new Board();
      mockBoard.initializeBoard();
      Card card = new Card();
      GridSquare square = mockBoard.getGridSquares().get(0);
      boardService.placeCardOnSquare(card, square);
      verify(cardRepository).save(eq(card));
      verify(gridSquareRepository).save(eq(square));
    }

    @Test
    public void placeCard_invalidCard_throwException() {
      Board mockBoard = new Board();
      mockBoard.initializeBoard();
      Card card = null;
      GridSquare square = mockBoard.getGridSquares().get(0);
      assertThrows(IllegalArgumentException.class, () -> boardService.placeCardOnSquare(card, square));
    }

    @Test
    public void placeCard_invalidSquare_throwException() {
      Board mockBoard = new Board();
      mockBoard.initializeBoard();
      Card card = new Card();
      List<Card> cards = new ArrayList<>();
      cards.add(card);
      GridSquare square = mockBoard.getGridSquares().get(0);
      square.setCards(cards);
      assertThrows(SquareOccupiedException.class, () -> boardService.placeCardOnSquare(card, square));
    }

    @Test
    public void cleanupGridSquare_validCardId_returnUpdatedGridSquare() {
        Long gridSquareId = 1L;
        GridSquare mockSquare = new GridSquare();
        mockSquare.setCards(new ArrayList<>(Arrays.asList(new Card(), new Card())));
        when(gridSquareRepository.findById(gridSquareId)).thenReturn(Optional.of(mockSquare));
        boardService.cleanupCardPileFromGridSquare(gridSquareId);
        verify(cardRepository).deleteAll(Mockito.any());
        assertTrue(mockSquare.getCards().isEmpty());
        verify(gridSquareRepository).save(any(GridSquare.class));
    }

    @Test
    public void cleanupGridSquare_invalidCardId_throwException() {
        Long gridSquareId = 1L;
        when(gridSquareRepository.findById(gridSquareId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> boardService.cleanupCardPileFromGridSquare(gridSquareId));
    }

    @Test
    public void cleanup() {
        Board board = new Board();
        List<GridSquare> squares = new ArrayList<>();
        GridSquare cardPileSquare = new GridSquare();
        cardPileSquare.setCardPile(true);
        cardPileSquare.setId(1L);
        cardPileSquare.setCards(new ArrayList<>(Arrays.asList(new Card(), new Card())));
        squares.add(cardPileSquare);
        
        GridSquare normalSquare = new GridSquare();
        normalSquare.setCardPile(false);
        normalSquare.setId(2L);
        normalSquare.setCards(new ArrayList<>(Arrays.asList(new Card())));
        squares.add(normalSquare);
        board.setGridSquares(squares);

        when(gridSquareRepository.findById(eq(1L))).thenReturn(Optional.of(cardPileSquare));
        boardService.cleanup(board);
        verify(cardRepository, times(2)).deleteAll(anyList());
        verify(gridSquareRepository, times(1)).save(any());
        assertTrue(cardPileSquare.getCards().isEmpty());
        assertTrue(normalSquare.getCards().isEmpty());
    } 
    
    @Test
    public void getGridSquareById_invalidBoard_throwException() {
      Board board = null;
      assertThrows(IllegalArgumentException.class, () -> boardService.getGridSquareById(board, 0));
    }

    @Test
    public void getGridSquareById_invalidIndex_throwException() {
      Board board = new Board();
      board.initializeBoard();
      assertThrows(IllegalArgumentException.class, () -> boardService.getGridSquareById(board, 9));
    }


}

