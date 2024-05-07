package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.repository.BoardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GridSquareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private GridSquareRepository gridSquareRepository;
    @Autowired
    private CardRepository cardRepository;

    @Transactional
    public Board initializeAndSaveBoard() {
        // Create and save the board first to generate its ID
        Board board = new Board();
        board.initializeBoard();
        return board;
    }}

    /*private List<GridSquare> initializeSquares(Board board) {
        String[] colors = {"Red", "Blue", "Green", "White", "Black", "Orange"};
        int index = 0;
        boolean whiteUsed = false;
        List<GridSquare> squares = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            GridSquare square = new GridSquare();
            square.setBoard(board); // Set the reference to the saved board

            if (i == 4) {
                square.setCardPile(true);
                board.setCardPileSquare(square); // Set the card pile square in the board
            } else {
                if (!whiteUsed && i == 8) {
                    square.setColor("White");
                    whiteUsed = true;
                } else {
                    square.setColor(colors[index % colors.length]);
                    index++;
                }
            }
            squares.add(square);
        }
        return squares;
    }

    private void setupCardPile(Board board) {
        GridSquare cardPileSquare = board.getCardPileSquare();
        if (cardPileSquare == null) {
            throw new IllegalStateException("Card pile square not initialized correctly.");
        }
        cardPileSquare.setCards(createPileCards(cardPileSquare));  // Properly create and set the pile cards
        cardRepository.saveAll(cardPileSquare.getCards());
    }

    private List<Card> createPileCards(GridSquare cardPileSquare) {
        Random random = new Random();
        String[] colors = {"Red", "Green", "Blue", "Orange", "Black", "White"};
        List<Card> pileCards = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);
            String color = colors[random.nextInt(colors.length)];
            Card card = new Card(color, points);
            card.setSquare(cardPileSquare);
            pileCards.add(card);
        }
        return pileCards;
    }
}*/
