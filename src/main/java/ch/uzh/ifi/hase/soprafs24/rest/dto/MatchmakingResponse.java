package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class MatchmakingResponse {
    private String status;
    private Long gameId;

    public MatchmakingResponse(String status) {
        this.status = status;
        this.gameId = null;
    }

    public MatchmakingResponse(String status, Long gameId) {
        this.status = status;
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public Long getGameId() {
        return gameId;
    }
}
