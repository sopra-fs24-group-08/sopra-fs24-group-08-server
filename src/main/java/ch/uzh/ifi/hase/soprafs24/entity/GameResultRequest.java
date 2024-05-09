package ch.uzh.ifi.hase.soprafs24.entity;

public class GameResultRequest {

    private Long gameId;
    private Long winnerId;
    private String winnerUsername;
    private Long loserId;
    private String loserUsername;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }

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

    public String getLoserUsername() {
        return loserUsername;
    }

    public void setLoserUsername(String loserUsername) {
        this.loserUsername = loserUsername;
    }
}
