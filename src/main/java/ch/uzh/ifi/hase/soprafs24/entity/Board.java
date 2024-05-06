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
        String[] colors = {"Red", "Blue", "Green", "White","Black", "Orange"};
        int index = 0;
        boolean whiteUsed = false;

        for (int i = 0; i < 9; i++) {
            GridSquare square = new GridSquare();
            square.setBoard(this);
            if (i == 4) { // Center square is the card pile
                cardPileSquare = square;
                square.setCardPile(true);
                System.out.println("Card Pile Initialized at Index: " + i);
            } else {
                if (!whiteUsed && i == 8) {
                    square.setColor("White");
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
        Random random = new Random();
        String[] colors = {"Red", "Green", "Blue", "Orange", "Black", "White"};
        List<Card> pileCards = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);
            String color = colors[random.nextInt(colors.length)];
            pileCards.add(new Card(color, points));
        }
        Collections.shuffle(pileCards);
        if (cardPileSquare != null) {
            cardPileSquare.setCards(pileCards);
        }
    }

    public Card drawCardFromPile() {
        if (cardPileSquare != null && !cardPileSquare.getCards().isEmpty()) {
            return cardPileSquare.getCards().remove(0);
        }
        return null;
    }

    public GridSquare getCardPileSquare() {
        return cardPileSquare;
    }

    public void setCardPileSquare(GridSquare cardPileSquare) {
        this.cardPileSquare = cardPileSquare;
    }
}




