package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class ChooseStartDTO {

    private boolean player1Starts;

    public ChooseStartDTO() {
        // Default constructor for frameworks (like Spring) that require it
    }

    public boolean isPlayer1Starts() {
        return player1Starts;
    }

    public void setPlayer1Starts(boolean player1Starts) {
        this.player1Starts = player1Starts;
    }
}
