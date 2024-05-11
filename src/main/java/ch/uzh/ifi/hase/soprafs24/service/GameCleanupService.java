package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameCleanupListener;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class GameCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(GameCleanupListener.class);
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final CardRepository cardRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    @Autowired
    public GameCleanupService(BoardService boardService, UserRepository userRepository, CardRepository cardRepository, ChatService chatService, PlayerRepository playerRepository, GameRepository gameRepository) {
        this.boardService = boardService;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.cardRepository = cardRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    public Map<Long, GameStateDTO> prepareGameEndData(Game game) {
        Map<Long, GameStateDTO> gameStateDTOs = new HashMap<>();
        game.getPlayers().forEach(player -> gameStateDTOs.put(player.getId(), DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, player.getId())));
        return gameStateDTOs;
    }

    public void cleanupGameData(Game game) {
        if (game == null) {
            logger.error("Attempted to clean up a null game.");
            return;
        }

        try {
            if (game.getChatRoom() != null) {
                chatService.cleanupChatRoom(game.getChatRoom());
                game.setChatRoom(null);
            }

            if (game.getBoard() != null) {
                boardService.cleanup(game.getBoard());
                game.setBoard(null);
            }

            cleanupPlayers(game.getPlayers());
            game.getPlayers().clear();

            if (game.getWinner() != null) {
                game.setWinnerUser(game.getWinner().getUser());
            }
            if (game.getLoser() != null) {
                game.setLoserUser(game.getLoser().getUser());
            }

            // Nullify player references after moving details to User entities
            game.setWinner(null);
            game.setLoser(null);

            gameRepository.save(game);
            logger.info("Game ID {} and all related entities successfully cleaned up", game.getGameId());
        } catch (Exception e) {
            logger.error("Error during game cleanup: ", e);
        }
    }

    private void cleanupPlayers(List<Player> players) {
        players.forEach(player -> {
            playerRepository.delete(player);
            revertPlayerToUser(player);
        });
    }

    private void revertPlayerToUser(Player player) {
        if (player.getUser() != null) {
            User user = player.getUser();
            user.setInGame(false);
            userRepository.save(user);
        }
    }
}
