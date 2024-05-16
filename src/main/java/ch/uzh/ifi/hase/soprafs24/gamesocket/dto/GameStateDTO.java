package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GridSquareDTO;

import java.util.List;

public class GameStateDTO {

    private Long gameId;
    private List<CardDTO> playerHand; // Ensure this is filtered per the specific player
    private List<GridSquareDTO> gridSquares;
    private int currentScore;
    private int opponentScore;
    private GameStatus gameStatus;
    private Long currentTurnPlayerId;
    private int cardPileSize;
    private Long winnerId;// ID of the winner, null if no winner yet
    private Long loserId;

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }
    public Long getLoserId() {
        return loserId;
    }

    public void setLoserId(Long loserId) {
        this.loserId = loserId;
    }

    @Override
    public String toString() {
        return "GameStateDTO{" +
                "gameId=" + gameId +
                ", playerHand=" + playerHand +
                ", gridSquares=" + gridSquares +
                ", currentScore=" + currentScore +
                ", opponentScore=" + opponentScore +
                ", gameStatus=" + gameStatus +
                ", currentTurnPlayerId=" + currentTurnPlayerId +
                ", cardPileSize=" + cardPileSize +
                ", winnerId=" + winnerId +
                ", loserId=" +loserId+
                '}';
    }

    public GameStateDTO() {}

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

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
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

    public int getCardPileSize() {
        return cardPileSize;
    }

    public void setCardPileSize(int cardPileSize) {
        this.cardPileSize = cardPileSize;
    }
}
