package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;

public class GridSquareDTO {
    private Long id;
    private String color;
    private boolean occupied;
    private CardDTO card; // Add a CardDTO object to represent the card on the square

    public GridSquareDTO() {}

    public GridSquareDTO(Long id, String color, boolean occupied, CardDTO card) {
        this.id = id;
        this.color = color;
        this.occupied = occupied;
        this.card = card;
    }



    @Override
    public String toString() {
        return "GridSquareDTO{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", occupied=" + occupied +
                ", card=" + (card != null ? card.toString() : "null") +
                '}';
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

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public CardDTO getCard() {
        return card;
    }

    public void setCard(CardDTO card) {
        this.card = card;
    }
}
