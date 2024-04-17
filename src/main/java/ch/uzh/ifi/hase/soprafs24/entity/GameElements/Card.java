package ch.uzh.ifi.hase.soprafs24.entity.GameElements;

public class Card {
    private final String color;
    private final int points;

    public Card(String color, int points) {
        this.color = color;
        this.points = points;
    }
    public String getColor() {
        return this.color;
    }

    // Method to get the points of the card
    public int getPoints() {
        return this.points;
    }
}