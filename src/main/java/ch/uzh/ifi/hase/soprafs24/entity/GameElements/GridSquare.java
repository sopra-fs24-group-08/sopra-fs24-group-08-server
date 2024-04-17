package ch.uzh.ifi.hase.soprafs24.entity.GameElements;
import java.util.Random;

public class GridSquare {
    private String color;
    private Boolean occupied;

    private static final String[] COLORS = {"red", "green", "blue","white"};
    public GridSquare() {
        Random rand = new Random();
        this.color = COLORS[rand.nextInt(COLORS.length)];
        this.occupied = false;
    }

    // Getters and setters
    public String getColor() {
        return color;
    }

    public Boolean getOccupied() {
        return occupied;
    }
    public void setOccupied() {
        this.occupied = true;
    }
}
