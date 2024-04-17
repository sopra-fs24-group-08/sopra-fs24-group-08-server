package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.entity.GameElements.*;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.Random;

public class GameService {

    public Board board;
    public CardPile cardPile;
    private Player player1;
    private Player player2;
    boolean isOnTurn;

    public void initializeGame(Player player1, Player player2){
        this.cardPile = new CardPile();
        this.player1 = player1;
        this.player2 = player2;
        board = new Board();
        Random random = new Random();
        this.isOnTurn = random.nextBoolean();
        //handCards initialization
        for (int i = 0; i < 5; i++) {
            Card card1 = cardPile.drawCard();
            Card card2 = cardPile.drawCard();
            player1.handCards.add(card1);
            player2.handCards.add(card2);
        }
    }
    public void setTurn(){
        isOnTurn = !isOnTurn;
    }
    public boolean isPlayerTurn(Player player) {
        return (isOnTurn && player.equals(player1)) || (!isOnTurn && player.equals(player2));
    }
    public Integer checkOccupied(){
        return board.occupiedGridSquare;
    }
    public String checkWinner() {
        if (player1.getScore() > player2.getScore()) {
            return "Player 1 is the winner with a score of " + player1.getScore() + "!";
        } else if (player2.getScore() > player1.getScore()) {
            return "Player 2 is the winner with a score of " + player2.getScore() + "!";
        } else {
            return "It's a tie with both players scoring " + player1.getScore() + "!";
        }
    }

    public void updateScore(Player player, Card card, Integer position){
        GridSquare cup = board.getGrid(position);
        if (cup.getColor().equals("white")) {
            player.score += card.getPoints();
        }else if(cup.getColor().equals(card.getColor())){
            player.score += 2*card.getPoints();
        }else{
            player.score += 0;
        }
    }
    public boolean placeCard(Player player, Card card, Integer position) {
        if (!isPlayerTurn(player)) {
            throw new IllegalStateException("It's not your turn");
        }
        if (position < 0 || position >= board.grid.size()) {
            throw new IllegalStateException("Invalid position");
        }
        GridSquare square = board.getGrid(position);
        if (!square.getOccupied() && player.handCards.contains(card)) {
            updateScore(player, card, position);
            square.setOccupied();
            player.handCards.remove(card);
            board.occupiedGridSquare += 1;
            setTurn();

            if(checkOccupied() == 8) {
                checkWinner();
                return false;
            }
            return true;
        }else {
            throw new IllegalStateException("Invalid move or card not in hand");
        }
    }
    public void pickThreeCards(Player player){
        if (!isPlayerTurn(player)) {
            throw new IllegalStateException("It's not your turn");}
        if (player.handCards.size() > 7 ){
                throw new IllegalStateException("You can't hold 3 more cards!");
        }
        for (int i = 0; i < 3; i++) {
             Card card = cardPile.drawCard();
             if (card != null){
                   player.handCards.add(card);
             }
        }
        setTurn();
    }
}