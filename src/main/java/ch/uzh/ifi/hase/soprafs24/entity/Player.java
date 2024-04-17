package ch.uzh.ifi.hase.soprafs24.entity;

import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.hase.soprafs24.entity.GameElements.*;

public class Player {
    private User user;
    public int score = 0;
    public List<Card> handCards;
    private Boolean isHinted;
    private Boolean isQuited;
    private ChatBox chatBox;
    public Player() {
        // Initialize
        //this.user = user;
        score = 0;
        handCards = new ArrayList<>();
        isHinted = false;
        isQuited = false;
        chatBox = new ChatBox();
    }

    public int getScore() {
        return score;
    }

    public void setHintStatus(){
        isHinted = !isHinted;
    }

    public boolean quit() {
        isQuited = true;
        return isQuited;
    }
}

