package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> centralCardPile = new ArrayList<Card>();

    @ElementCollection
    private List<String> squareColors = new ArrayList<String>(9);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> placedCards = new ArrayList<Card>(9);

    public Board() {
        initializeCentralCardPile();
        initializeSquareColors();
        initializePlacedCards();
    }

    private void initializeCentralCardPile() {
        String[] colors = {"red", "green", "blue", "yellow", "black", "white"};
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);
            String color = colors[random.nextInt(colors.length)];
            Card card = new Card(color, points);
            centralCardPile.add(card);
        }
    }

    private void initializeSquareColors() {
        for (int i = 0; i < 9; i++) {
            squareColors.add("Default Color"); // Defaulting all to "Default Color" for simplicity
        }
    }

    private void initializePlacedCards() {
        for (int i = 0; i < 9; i++) {
            placedCards.add(null); // Initialize all positions with null indicating no card is placed
        }
    }

    public Long getId() {
        return id;
    }

    public List<Card> getCentralCardPile() {
        return centralCardPile;
    }

    public String getSquareColor(int index) {
        return squareColors.get(index);
    }

    public boolean isSquareOccupied(int position) {
        return placedCards.get(position) != null;
    }

    public void setCardAtPosition(Card card, int position) {
        placedCards.set(position, card);
    }

    public List<Card> getPlacedCards() {
        return this.placedCards;
    }

    public Card drawCardFromPile() {
        System.out.println(getCentralCardPile().size());
        if (!centralCardPile.isEmpty()) {
            return centralCardPile.remove(0);
        }
        return null; // or throw an exception if you prefer
    }


}
