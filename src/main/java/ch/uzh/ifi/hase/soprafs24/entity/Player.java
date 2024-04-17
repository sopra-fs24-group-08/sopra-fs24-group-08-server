package ch.uzh.ifi.hase.soprafs24.entity;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private User user;
    private int score = 0;
    private List<Card> handCards;
    private Boolean isHinted;
    private Boolean isOnTurn;
    private Boolean isQuited;
    private ChatBox chatBox;
    public Player(CardPile cardPile, User user) {
        // Initialize
        this.user = user;
        score = 0;
        handCards = new ArrayList<>();
        isHinted = false;
        isOnTurn = false;
        isQuited = false;
        chatBox = new ChatBox();
        for (int i = 0; i < 10; i++) {
            Card card = cardPile.drawCard();
            handCards.add(card);
        }
    }

    public Player() {

    }

    public boolean drawCard(CardPile cardPile){
        if (handCards.size() > 5){
            return false;
        } else {
            for (int i = 0; i < 3; i++) {
                Card card = cardPile.drawCard();
                if (card != null){
                    handCards.add(card);
                }
            }
            return true;
        }
    }
    public boolean playCard(Board board, Card card, Integer position) {
        if (!handCards.contains(card) || !board.placeCard(card,position)){
            return false;
        }else {
            handCards.remove(card);
            return true;
        }
    }
    public void updateScore(Card card, GridSquare cup) {
        if (cup.getColor().equals("white")) {
            this.score += card.getPoints();
        }else if(cup.getColor().equals(card.getColor())){
            this.score += 2*card.getPoints();
        }else{
            this.score += 0;
        }
    }
    public int getScore() {
        return score;
    }
    public void setOnTurn(){
        isOnTurn = !isOnTurn;
    }
    public void setHintStatus(){
        isHinted = !isHinted;
    }

    public boolean quit() {
        isQuited = true;
        return isQuited;
    }
    // 通过User对象获取ID
    public Long getId() {
        return user.getId();
    }

}

