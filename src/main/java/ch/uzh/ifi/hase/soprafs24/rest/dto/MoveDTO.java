package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.MoveType;

public class MoveDTO {
    private MoveType moveType;
    private String player;
    private int position; // Game logic stuff


    //see waht exactly to pass
    public MoveDTO(String player, int position) {
        this.player = player;
        this.position = position;
    }

    // Getters and Setters
    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
