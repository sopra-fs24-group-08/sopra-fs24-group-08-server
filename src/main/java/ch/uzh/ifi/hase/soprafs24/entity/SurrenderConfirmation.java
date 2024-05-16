package ch.uzh.ifi.hase.soprafs24.entity;

public class SurrenderConfirmation {
    private Long playerId;
    private Boolean surrender;

    // Getters and Setters
    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Boolean getSurrender() {
        return surrender;
    }

    public void setSurrender(Boolean surrender) {
        this.surrender = surrender;
    }
}
