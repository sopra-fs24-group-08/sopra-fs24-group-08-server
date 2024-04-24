package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;

public class TurnDecisionDTO {
    public Boolean getStarterChoice() {
        return starterChoice;
    }

    public void setStarterChoice(Boolean starterChoice) {
        this.starterChoice = starterChoice;
    }

    public Long getStarterPlayerId() {
        return starterPlayerId;
    }

    public void setStarterPlayerId(Long starterPlayerId) {
        this.starterPlayerId = starterPlayerId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    private Long gameId;
    private Long starterPlayerId;
    private Boolean starterChoice;  // True if the player decides to go first

}