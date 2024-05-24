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
        board = boardRepository.save(board);
        return board;
    }
    @Transactional
    public Card drawCardFromPile(Board board) throws NoCardsLeftException {
        if (board != null && !board.getCardPileSquare().getCards().isEmpty()) {
            Card card = board.getCardPileSquare().getCards().remove(0);
            cardRepository.save(card);
            return card;
        } else {
            throw new NoCardsLeftException("Card pile is empty");
        }
    }
    @Transactional
    public void placeCardOnSquare(Card card, GridSquare square) throws SquareOccupiedException {
        if (card != null && square != null && !square.isOccupied()) {
            square.addCard(card);
            cardRepository.save(card);
            gridSquareRepository.save(square);
        } else if(card ==  null) {
            throw new IllegalArgumentException("Card cannot be null.");
        } else{
            assert square != null;
            throw new SquareOccupiedException("Square is occupied or does not exist at position: " + square.getId());
        }
    }

    @Transactional
    public void cleanup(Board board) {
        // Directly cleanup all GridSquares including cardPileSquare if cascades are properly set
        for (GridSquare square : board.getGridSquares()) {
            if(square.isCardPile()){
                cleanupCardPileFromGridSquare(square.getId());
                continue;
            }
            cardRepository.deleteAll(square.getCards()); // Ensure all cards are deleted
            square.getCards().clear(); // Clear in-memory references immediately
        }
        gridSquareRepository.deleteAll(board.getGridSquares());
        boardRepository.delete(board);


    }


    @Transactional
    public void cleanupCardPileFromGridSquare(Long gridSquareId) {
        GridSquare cardPileSquare = gridSquareRepository.findById(gridSquareId).orElseThrow(() -> new IllegalArgumentException("GridSquare not found for ID: " + gridSquareId));
        cardRepository.deleteAll(cardPileSquare.getCards());  // Deleting cards
        cardPileSquare.getCards().clear();  // Clearing in-memory references
        gridSquareRepository.save(cardPileSquare);  // Saving the updated state
    }

    @Transactional(readOnly = true)
    public boolean isAllSquaresOccupied(Board board) {
        return gridSquareRepository.countByBoardIdAndIsOccupiedFalse(board.getId()) == 0;
    }

    @Transactional(readOnly = true)
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
