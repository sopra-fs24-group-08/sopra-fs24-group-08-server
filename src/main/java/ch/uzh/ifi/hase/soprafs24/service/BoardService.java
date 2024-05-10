package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.exceptions.NoCardsLeftException;
import ch.uzh.ifi.hase.soprafs24.exceptions.SquareOccupiedException;
import ch.uzh.ifi.hase.soprafs24.repository.BoardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GridSquareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final GridSquareRepository gridSquareRepository;

    @Autowired
    public BoardService(GridSquareRepository gridSquareRepository, BoardRepository boardRepository, CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
        this.gridSquareRepository = gridSquareRepository;
    }

    @Transactional
    public Board initializeAndSaveBoard() {
        Board board = new Board();
        board.initializeBoard();
        boardRepository.save(board);
        return board;
    }

    public Card drawCardFromPile(Board board) throws NoCardsLeftException {
        if (board != null && !board.getCardPileSquare().getCards().isEmpty()) {
            Card card = board.getCardPileSquare().getCards().remove(0);
            cardRepository.save(card);
            return card;
        } else {
            throw new NoCardsLeftException("Card pile is empty");
        }
    }

    public void placeCardOnSquare(Card card, GridSquare square) throws SquareOccupiedException {
        if (square != null && !square.isOccupied()) {
            square.addCard(card);
            card.setSquare(square);
            cardRepository.save(card);
            gridSquareRepository.save(square);
        } else {
            throw new SquareOccupiedException("Square is occupied or does not exist at position: " + square.getId());
        }
    }

    @Transactional
    public void cleanup(Board board) {
        if (board.getCardPileSquare() != null) {
            cleanupCardPileFromGridSquare(board.getCardPileSquare().getId());
        }
        for (GridSquare square : (board.getGridSquares())) {
            if(square != board.getCardPileSquare()) {
                List<Card> cards = square.getCards();
                if (cards != null) {
                    cardRepository.deleteAll(cards);
                }
                gridSquareRepository.delete(square);

            }
        }
        boardRepository.delete(board);
    }

    public void cleanupCardPileFromGridSquare(Long gridSquareId) {
        GridSquare cardPileSquare = gridSquareRepository.findById(gridSquareId).orElseThrow(() -> new IllegalArgumentException("GridSquare not found for ID: " + gridSquareId));
        cardRepository.deleteAll(cardPileSquare.getCards());
        cardPileSquare.getCards().clear();
    }


    public boolean isAllSquaresOccupied(Board board) {
        return gridSquareRepository.countByBoardIdAndIsOccupiedFalse(board.getId()) == 0;
    }

    public GridSquare getGridSquareById(Board board, int index) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null.");
        }
        if (index < 0 || index >= board.getGridSquares().size()) {
            throw new IllegalArgumentException("Grid square index " + index + " is out of bounds.");
        }
        return board.getGridSquares().get(index);
    }
}
