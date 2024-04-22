package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;



public class TurnDecisionRequestDTO {
    private Long gameId;
    private String message;

    public TurnDecisionRequestDTO(Long gameId, String message) {
        this.gameId = gameId;
        this.message = message;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
