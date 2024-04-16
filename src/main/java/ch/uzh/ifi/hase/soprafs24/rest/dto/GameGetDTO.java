package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class GameGetDTO {

    private Long id;
    private List<GridSquareDTO> grid; // Grid representation with DTO
    private Long player1Id;
    private Long player2Id;
    private Boolean coinTossResult;
    private Boolean player1sTurn;
    private Boolean awaitingPlayerChoice;

    // Constructors, getters, and setters

    public GameGetDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<GridSquareDTO> getGrid() {
        return grid;
    }

    public void setGrid(List<GridSquareDTO> grid) {
        this.grid = grid;
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
}
