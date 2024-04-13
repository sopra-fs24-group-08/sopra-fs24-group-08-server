package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;

public class GameService {
    private Board board;
    public GameService() {
        this.board = new Board();
    }


    public void startGameSession(Long userId1, Long userId2){
        //.............
    }
    public boolean placeCardAndCalculateScore(Player player, Card card, Integer position) {
        GridSquare square = board.getGrid(position);
        if (player.playCard(board, card, position) && board.placeCard(card, position)){
            player.updateScore(card, square);
        }else{
            throw new IllegalStateException("Invalid move");
        }
        return true;
    }


}
