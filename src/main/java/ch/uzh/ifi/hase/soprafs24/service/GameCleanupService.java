package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;

@Service
public class GameCleanupService {


    private final BoardService boardService;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final CardRepository cardRepository;

    @Autowired
    public GameCleanupService(BoardService boardService,UserRepository userRepository, CardRepository cardRepository, ChatService chatService) {
        this.boardService = boardService;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.cardRepository = cardRepository;
    }

    public void cleanupGameData(Game game) {
        if (game.getChatRoom() != null) {
            chatService.cleanupChatRoom(game.getChatRoom());
        }
        cleanupPlayers(game.getPlayers());
        if (game.getBoard() != null) {
            boardService.cleanup(game.getBoard());
        }
        System.out.println("Game ID " + game.getGameId() + " and all related entities successfully cleaned up");
    }

    private void cleanupPlayers(List<Player> players) {
        players.forEach(this::cleanupPlayerData);
    }

    private void cleanupPlayerData(Player player) {
        if(!player.getHand().isEmpty()) {
            cardRepository.deleteAll(player.getHand());
            player.getHand().clear();
        }
        revertPlayerToUser(player);
    }

    private void revertPlayerToUser(Player player) {
        User user = player.getUser();
        user.setInGame(false);
        userRepository.save(user);
    }
}
