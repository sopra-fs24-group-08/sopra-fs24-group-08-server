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
    public String inviteFriend(Long userId, Long friendId) {
        // This method should send an invitation to the friend.
        // For now, let's assume we have a method to check if the friend accepts the invitation.
        boolean accepted = sendInvitationAndGetResponse(friendId);

        if (accepted) {
            startNewGame(userId, friendId);
            return "Game started!";
        } else {
            return "Invitation rejected!";
        }
    }
    private boolean sendInvitationAndGetResponse(Long friendId) {
        // web socket part
        return true;
    }

    private void startNewGame(Long userId, Long friendId) {
        // This should contain the actual logic to initialize a new game with both users.
        // ...
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
