package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class GridSquareDTO {
    private Long id;
    private String color;
    private boolean occupied;
    private Object cards;  // This can be a List<CardDTO> or CardDTO or null

    public GridSquareDTO() {
    }

    public GridSquareDTO(Long id, String color, boolean occupied, Object cards) {
        this.id = id;
        this.color = color;
        this.occupied = occupied;
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "GridSquareDTO{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", occupied=" + occupied +
                ", cards=" + cards +
                '}';
    }

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

    public Object getCards() {
        return cards;
    }

    public void setCards(Object cards) {
        this.cards = cards;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }
}
