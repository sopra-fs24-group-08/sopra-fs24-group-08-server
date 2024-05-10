package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;

@Service
public class GameCleanupService {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private BoardService boardService;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatService chatService;
    @Autowired
    private CardRepository cardRepository;

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
            boardService.cleanupCards(player.getHand());
        }
        revertPlayerToUser(player);
    }

    private void revertPlayerToUser(Player player) {
        User user = player.getUser();
        user.setInGame(false);
        userRepository.save(user);
    }
}
