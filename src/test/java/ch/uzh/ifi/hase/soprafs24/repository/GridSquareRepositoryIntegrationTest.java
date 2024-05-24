package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class GridSquareRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GridSquareRepository gridSquareRepository;
    @Autowired
    private BoardRepository boardRepository;

    @AfterEach
    public void teardown() {
        gridSquareRepository.deleteAll();
    }

    @Test
    public void countByBoardIdAndIsOccupiedFalse_success() {
        Board board = new Board();
        boardRepository.saveAndFlush(board);

        GridSquare occupiedGridSquare = new GridSquare();
        occupiedGridSquare.setBoard(board);
        occupiedGridSquare.setCardPile(false);
        Card card = new Card();
        card.setSquare(occupiedGridSquare);
        occupiedGridSquare.getCards().add(card);
        gridSquareRepository.saveAndFlush(occupiedGridSquare);

        GridSquare emptyGridSquare = new GridSquare();
        emptyGridSquare.setBoard(board);
        emptyGridSquare.setCardPile(false);
        gridSquareRepository.saveAndFlush(emptyGridSquare);

        long emptySquaresCount = gridSquareRepository.countByBoardIdAndIsOccupiedFalse(board.getId());
        assertEquals(1, emptySquaresCount);
    }

    @Test
    public void countCardsInCardPileGridSquare_success() {
        Board board = new Board();
        boardRepository.saveAndFlush(board);


        GridSquare cardPileGridSquare = new GridSquare();
        cardPileGridSquare.setBoard(board);
        cardPileGridSquare.setCardPile(true);
        Card card1 = new Card();
        card1.setSquare(cardPileGridSquare);
        Card card2 = new Card();
        card2.setSquare(cardPileGridSquare);
        cardPileGridSquare.getCards().add(card1);
        cardPileGridSquare.getCards().add(card2);
        gridSquareRepository.saveAndFlush(cardPileGridSquare);


        int cardsCount = gridSquareRepository.countCardsInCardPileGridSquare(board.getId());
        assertEquals(2, cardsCount);
    }
}
