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
    @JoinColumn(name = "grid_square_id")
    private GridSquare square;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = true)  // Nullable if the card is not in a player's hand
    private Player player;

    public Card() {}

    public Card(String color, int points) {
        this.color = color;
        this.points = points;
    }

    // Standard getters and setters

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

    public GridSquare getSquare() {
        return square;
    }

    public void setSquare(GridSquare square) {
        this.square = square;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
