package ch.uzh.ifi.hase.soprafs24.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "board")
    private Game game;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "board", fetch = FetchType.LAZY)
    private List<GridSquare> gridSquares = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "card_pile_square_id", referencedColumnName = "id")
    private GridSquare cardPileSquare;

    public void initializeBoard() {
        initializeSquares();
        initializeCardPile();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<GridSquare> getGridSquares() {
        return gridSquares;
    }

    public void setGridSquares(List<GridSquare> gridSquares) {
        this.gridSquares = gridSquares;
    }

    private void initializeSquares() {
        System.out.println("Initializing squares");
        String[] colors = {"red", "blue", "green", "white"};
        int index = 0;
        boolean whiteUsed = false;

        for (int i = 0; i < 9; i++) {
            GridSquare square = new GridSquare();
            square.setBoard(this);
            if (i == 4) { // Center square is the card pile
                cardPileSquare = square;
                square.setCardPile(true);
                square.setColor(null);
                System.out.println("Card Pile Initialized at Index: " + i);
            } else {
                if (!whiteUsed && i == 8) {
                    square.setColor("white");
                    whiteUsed = true;
                } else {
                    square.setColor(colors[index % colors.length]);
                    index++;
                }
            }
            gridSquares.add(square);
        }
    }


    private void initializeCardPile() {
        Logger logger = LoggerFactory.getLogger(Board.class);
        Random random = new Random();
        String[] colors = {"red", "blue", "green"};
        List<Card> pileCards = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5); // Points between 1 and 5
            String color = colors[random.nextInt(colors.length)];
            Card newCard = new Card(color, points);
            pileCards.add(newCard);
        }

        Collections.shuffle(pileCards); // Shuffle the list of cards to randomize the order

        if (cardPileSquare != null) {
            cardPileSquare.setCards(pileCards); // Assign the shuffled cards to the card pile square
            logger.info("Card pile initialized with 30 cards.");
        } else {
            logger.error("Card pile square not initialized.");
        }
    }

    public GridSquare getCardPileSquare() {
        return cardPileSquare;
    }

    public void setCardPileSquare(GridSquare cardPileSquare) {
        this.cardPileSquare = cardPileSquare;
    }
}




