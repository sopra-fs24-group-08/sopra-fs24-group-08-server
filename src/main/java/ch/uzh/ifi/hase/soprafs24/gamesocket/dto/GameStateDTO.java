package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GridSquareDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GameStateDTO {
    private Long gameId;
    private List<CardDTO> playerHand;
    private List<GridSquareDTO> gridSquares;
    private int currentScore;
    private Long currentPlayerId;
    private GameStatus gameStatus;
    private Long currentTurnPlayerId;
    private Long winnerId;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public List<CardDTO> getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(List<CardDTO> playerHand) {
        this.playerHand = playerHand;
    }


    public List<GridSquareDTO> getGridSquares() {
        return gridSquares;
    }

    public void setGridSquares(List<GridSquareDTO> gridSquares) {
        this.gridSquares = gridSquares;
    }


    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Long getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(Long currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }



    // Getters and Setters
}
