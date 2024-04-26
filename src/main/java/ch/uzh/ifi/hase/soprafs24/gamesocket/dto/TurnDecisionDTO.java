package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;

public class TurnDecisionDTO {

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

    public Long getOtherPlayerId() {
        return otherPlayerId;
    }

    public void setOtherPlayerId(Long otherPlayerId) {
        this.otherPlayerId = otherPlayerId;
    }

    private Long otherPlayerId;  // True if the player decides to go first

}