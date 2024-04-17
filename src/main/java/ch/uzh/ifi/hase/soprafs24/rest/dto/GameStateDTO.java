package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;

//add details later, template stuff now so it doesn't fail
public class GameStateDTO {
    private List<PlayerDTO> players;
    private String[] board;
    private boolean gameWon;
    private String winner;


    public GameStateDTO() {
        this.players = null;
        this.board = new String[9];
        this.gameWon = false;
        this.winner = "";
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }


    public String[] getBoard() {
        return board;
    }

    public void setBoard(String[] board) {
        this.board = board;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}

