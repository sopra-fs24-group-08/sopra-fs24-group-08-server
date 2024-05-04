package ch.uzh.ifi.hase.soprafs24.entity;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "board")
    private List<GridSquare> gridSquares = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "card_pile_square_id")  // Explicitly referencing the special grid square for the card pile
    private GridSquare cardPileSquare;

    // Represents the card pile directly
    @OneToMany(cascade = CascadeType.ALL)
    private List<Card> cards = new ArrayList<>();

    public void initializeBoard() {
        initializeSquares();
        initializeCardPile();
    }

    // Getters and setters for all fields including new cardPileSquare
    public GridSquare getCardPileSquare() {
        return cardPileSquare;
    }

    public void setCardPileSquare(GridSquare cardPileSquare) {
        this.cardPileSquare = cardPileSquare;
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

    public void initializeSquares() {
        String[] colors = {"Red", "Blue", "Green", "Yellow", "Black", "Orange"};
        int index = 0;
        boolean whiteUsed = false;

        for (int i = 0; i < 9; i++) {
            GridSquare square = new GridSquare();
            square.setBoard(this);

            if (i == 4) {
                this.cardPileSquare = square;
                square.setCardPile(true);
                gridSquares.add(square);
                continue;
            }

            if (!whiteUsed && i == 8) {  // Ensuring at least one white square, we can alter it depending on if we add special events or not
                square.setColor("White");
                whiteUsed = true;
            } else {
                square.setColor(colors[index % colors.length]);
                index++;
            }
            gridSquares.add(square);
        }
    }


    private void initializeCardPile() {
        Random random = new Random();
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Black", "White"};
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);  // Points between 1 and 5
            String color = colors[random.nextInt(colors.length)];
            Card card = new Card(color, points);
            this.cards.add(card);
        }
        Collections.shuffle(this.cards);  // Shuffle the card pile
    }


    public Card drawCard(Player player) {
        if (!this.cards.isEmpty()) {
            Card card = this.cards.remove(0);
            player.addCardToHand(card);
            return card;
        }
        return null;
    }
}




