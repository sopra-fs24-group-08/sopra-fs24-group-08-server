package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class GridSquareDTO {
    private Long id;
    private String color;
    private CardDTO card;  // Optionally include simplified card details

    public GridSquareDTO() {}

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

    public CardDTO getCard() {
        return card;
    }

    public void setCard(CardDTO card) {
        this.card = card;
    }
}
