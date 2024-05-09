package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.exceptions.NoCardsLeftException;
import ch.uzh.ifi.hase.soprafs24.exceptions.SquareOccupiedException;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {


    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final GridSquareRepository gridSquareRepository;


    @Autowired
    public BoardService(SimpMessagingTemplate messagingTemplate, GridSquareRepository gridSquareRepository, BoardRepository boardRepository,CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
        this.gridSquareRepository = gridSquareRepository;
    }

        @Transactional
        public Board initializeAndSaveBoard () {
            // Create and save the board first to generate its ID
            Board board = new Board();
            board.initializeBoard();
            return board;
        }

        public Card drawCardFromPile (Board board) throws NoCardsLeftException {
            if (board != null && !board.getCardPileSquare().getCards().isEmpty()) {
                return board.getCardPileSquare().getCards().remove(0);
            }
            else {
                throw new NoCardsLeftException("Card pile is empty");
            }
        }

        public void placeCardOnSquare (Card card, GridSquare square) throws SquareOccupiedException {
            if (square != null && !square.isOccupied()) {
                square.addCard(card);
                card.setSquare(square);  // Proper linking
            }
            else {
                throw new SquareOccupiedException("Square is occupied or does not exist at position: " + square.getId());
            }
        }
        @Transactional
        public void cleanup (Board board){
            // Check and cleanup the card pile square first if there is any special handling
            if (board.getCardPileSquare() != null) {
                cleanupCardPileFromGridSquare(board.getCardPileSquare().getId());
            }

            // Proceed to clean up all other grid squares and the board itself
            for (GridSquare square : board.getGridSquares()) {
                cardRepository.deleteAll(square.getCards()); // Clear all cards from each grid square
                gridSquareRepository.delete(square);  // Delete each grid square
            }
            boardRepository.delete(board);  // Delete the board itself
            System.out.println("Board and all related squares and cards cleared");
        }
        @Transactional
        public void cleanupCardPileFromGridSquare (Long gridSquareId){
            GridSquare cardPileSquare = gridSquareRepository.findById(gridSquareId).orElse(null);
            if (cardPileSquare != null) {
                cardRepository.deleteAll(cardPileSquare.getCards());
                cardPileSquare.getCards().clear();
                gridSquareRepository.save(cardPileSquare);
            }
        }
        @Transactional
        public void cleanupCards (List < Card > cards) {
            cardRepository.deleteAll(cards);
        }
        public boolean isAllSquaresOccupied (Board board){
            return gridSquareRepository.countByBoardIdAndIsOccupiedFalse(board.getId()) == 0;
        }

        /**
         * Retrieves a GridSquare from a Board based on its index.
         * This method assumes that the position corresponds directly to the index in the list of grid squares.
         *
         * @param board The board containing the grid squares.
         * @param index The index of the grid square in the board's list.
         * @return The GridSquare at the specified index.
         * @throws IllegalArgumentException if the index is out of range or the board is null.
         */

        public GridSquare getGridSquareById (Board board,int index){
            if (board == null) {
                throw new IllegalArgumentException("Board cannot be null.");
            }

            List<GridSquare> squares = board.getGridSquares();
            if (index < 0 || index >= squares.size()) {
                throw new IllegalArgumentException("Grid square index " + index + " is out of bounds.");
            }
            return squares.get(index);
        }
    }
