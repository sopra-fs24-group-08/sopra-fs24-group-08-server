package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GAME")
public class Game {
    @Id
    @GeneratedValue
    private Long id;

    private Long player1Id;
    private Long player2Id;

    @ElementCollection
    private List<GridRow> grid = new ArrayList<>();

    private Boolean coinTossResult; // true for heads, false for tails
    private Boolean player1sTurn= false;; // true if it's player 1's turn, false otherwise
    private Boolean awaitingPlayerChoice; // true if waiting for player to choose who starts

    public Game() {
        // Initialize the grid with all empty slots (-1 for empty, 0 for blocked)
        this.grid.add(new GridRow(-1, -1, -1));
        this.grid.add(new GridRow(-1, 0, -1));
        this.grid.add(new GridRow(-1, -1, -1));
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }

    public Boolean getCoinTossResult() {
        return coinTossResult;
    }

    public void setCoinTossResult(Boolean coinTossResult) {
        this.coinTossResult = coinTossResult;
    }

    public Boolean getPlayer1sTurn() {
        return player1sTurn;
    }

    public void setPlayer1sTurn(Boolean player1sTurn) {
        this.player1sTurn = player1sTurn;
    }

    public Boolean getAwaitingPlayerChoice() {
        return awaitingPlayerChoice;
    }

    public void setAwaitingPlayerChoice(Boolean awaitingPlayerChoice) {
        this.awaitingPlayerChoice = awaitingPlayerChoice;
    }

    public List<GridRow> getGrid() {
        return grid;
    }
    public void setGrid(List<GridRow> grid) {
        this.grid = grid;
    }
}
