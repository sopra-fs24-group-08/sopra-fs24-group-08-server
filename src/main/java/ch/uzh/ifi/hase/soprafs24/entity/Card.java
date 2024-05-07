package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;


@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;
    private int points;

    // Relationship with GridSquare
    @ManyToOne
    @JoinColumn(name = "grid_square_id")
    private GridSquare square;

    // Relationship with Player
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Constructors
    public Card() {}

    public Card(String color, int points) {
        this.color = color;
        this.points = points;
    }

    // Getter and Setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and Setter for color
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Getter and Setter for points
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    // Getter and Setter for GridSquare
    public GridSquare getSquare() {
        return square;
    }

    public void setSquare(GridSquare square) {
        this.square = square;
        this.player = null; // Ensure that setting a square unlinks the player
    }

    // Getter and Setter for Player
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.square = null; // Ensure that setting a player unlinks the square
    }
}
