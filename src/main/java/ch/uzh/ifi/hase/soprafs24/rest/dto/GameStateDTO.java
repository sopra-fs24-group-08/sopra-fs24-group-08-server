package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;

public class GameStateDTO {
    private Long gameId;
    private List<PlayerDTO> players;
    private BoardDTO board;
    private GameStatus gameStatus;
    private Long winnerId;
    private Long currentTurnPlayerId;
    private int cardPileSize;

    private Long chatBoxId;
    public Long getChatBoxId() {return chatBoxId;}
    public void setChatBoxId(Long chatBoxId) {this.chatBoxId = chatBoxId;}
    public int getCardPileSize() {
        return cardPileSize;
    }

    public void setCardPileSize(int cardPileSize) {
        this.cardPileSize = cardPileSize;
    }


    public GameStateDTO() {
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public BoardDTO getBoard() {
        return board;
    }

    public void setBoard(BoardDTO board) {
        this.board = board;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public Long getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(Long currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }
}