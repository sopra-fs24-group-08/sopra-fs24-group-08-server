package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class PlayerDTO {
    private Long id;
    // private Long gameId;
    private String playerName; // Optional, based on what you want to show
    private int score;
    private List<CardDTO> cards;

    public List<CardDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // public Long getGameId() {
    //     return gameId;
    // }

    // public void setGameId(Long gameId) {
    //     this.gameId = gameId;
    // }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPlayerName() {
      return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

}
