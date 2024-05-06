package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class CardDTO {
    private Long id;
    private String color;
    private int points;

    public CardDTO() {
        // no-arg constructor for MapStruct complains
    }

    public CardDTO(Long id, String color, int points) {
        this.id = id;
        this.color = color;
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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

}
