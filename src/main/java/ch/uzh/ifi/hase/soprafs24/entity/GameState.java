package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;

import java.util.List;

public class GameState {
    private Board board;
    private int scorePlayer1;
    private int scorePlayer2;
    private boolean isPlayer1Turn;
    private List<CardDTO> player1Cards;
    private List<CardDTO> player2Cards;

    public GameState(Game game) {
    }

    public List<CardDTO> getPlayer1Cards() {
        return player1Cards;
    }

    public void setPlayer1Cards(List<CardDTO> player1Cards) {
        this.player1Cards = player1Cards;
    }

    public List<CardDTO> getPlayer2Cards() {
        return player2Cards;
    }

    public void setPlayer2Cards(List<CardDTO> player2Cards) {
        this.player2Cards = player2Cards;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public void setPlayer1Turn(boolean player1Turn) {
        isPlayer1Turn = player1Turn;
    }

    public int getScorePlayer2() {
        return scorePlayer2;
    }

    public void setScorePlayer2(int scorePlayer2) {
        this.scorePlayer2 = scorePlayer2;
    }

    public int getScorePlayer1() {
        return scorePlayer1;
    }

    public void setScorePlayer1(int scorePlayer1) {
        this.scorePlayer1 = scorePlayer1;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

}
