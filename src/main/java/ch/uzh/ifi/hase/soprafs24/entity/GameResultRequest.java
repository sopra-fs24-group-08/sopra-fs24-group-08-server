package ch.uzh.ifi.hase.soprafs24.entity;

public class GameResultRequest {

    private boolean participated;
    private boolean won;
    private String playerName;

    public GameResultRequest(boolean participated, boolean won, String playerName) {
        this.participated = participated;
        this.won = won;
        this.playerName = "";
    }

    public boolean isParticipated() {
        return participated;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setParticipated(boolean participated) {
        this.participated = participated;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}
