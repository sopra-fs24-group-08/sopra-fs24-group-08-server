package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
public class GridSquareRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GridSquareRepository gridSquareRepository;

    @AfterEach
    public void teardown() {
        gridSquareRepository.deleteAll();
    }

    @Test
    public void countByBoardIdAndIsOccupiedFalse_success() {
        Board board = new Board();
        entityManager.persist(board);
        entityManager.flush();

        GridSquare occupiedGridSquare = new GridSquare();
        occupiedGridSquare.setBoard(board);
        occupiedGridSquare.setCardPile(false);
        Card card = new Card();
        card.setSquare(occupiedGridSquare);
        occupiedGridSquare.getCards().add(card);
        entityManager.persist(occupiedGridSquare);

        GridSquare emptyGridSquare = new GridSquare();
        emptyGridSquare.setBoard(board);
        emptyGridSquare.setCardPile(false);
        entityManager.persist(emptyGridSquare);

        entityManager.flush();

        long emptySquaresCount = gridSquareRepository.countByBoardIdAndIsOccupiedFalse(board.getId());
        assertEquals(1, emptySquaresCount);
    }

    @Test
    public void countCardsInCardPileGridSquare_success() {
        Board board = new Board();
        entityManager.persist(board);
        entityManager.flush();

        GridSquare cardPileGridSquare = new GridSquare();
        cardPileGridSquare.setBoard(board);
        cardPileGridSquare.setCardPile(true);
        Card card1 = new Card();
        card1.setSquare(cardPileGridSquare);
        Card card2 = new Card();
        card2.setSquare(cardPileGridSquare);
        cardPileGridSquare.getCards().add(card1);
        cardPileGridSquare.getCards().add(card2);
        entityManager.persist(cardPileGridSquare);

        entityManager.flush();

        int cardsCount = gridSquareRepository.countCardsInCardPileGridSquare(board.getId());
        assertEquals(2, cardsCount);
    }
}
