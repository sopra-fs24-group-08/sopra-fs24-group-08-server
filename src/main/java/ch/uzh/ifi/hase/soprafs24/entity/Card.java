package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String color;
    private int points;

    @ManyToOne
    @JoinColumn(name = "card_pile_id", nullable = false)
    private CardPile cardPile;

    public Card() {}

    public Card(String color, int points) {
        this.color = color;
        this.points = points;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public CardPile getCardPile() {
        return cardPile;
    }

    public void setCardPile(CardPile cardPile) {
        this.cardPile = cardPile;
    }
}