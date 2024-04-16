package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class GridSquareDTO {
    private String color;
    private Boolean occupied;

    public GridSquareDTO() {
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }
}
