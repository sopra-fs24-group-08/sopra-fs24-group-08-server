package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public class MatchmakingService {
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final PlayerQueueService playerQueueService;

    @Autowired
    public MatchmakingService(SimpMessagingTemplate messagingTemplate, GameService gameService, PlayerRepository playerRepository,PlayerQueueService playerQueueService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.playerRepository = playerRepository;
        this.playerQueueService = new PlayerQueueService();
    }

    public void addToQueue(Long playerId) {
        if (playerId == null) {
            System.out.println("Attempted to add null playerId to queue");
            return;
        }
        playerQueueService.addToQueue(playerId);
        checkForMatches();
    }

    public void checkForMatches() {
        if (playerQueueService.isEligibleForMatch()) {
            Iterator<Long> iterator = playerQueueService.getQueue().keySet().iterator();
            Long playerOneId = iterator.next();
            Long playerTwoId = iterator.next();

            // Create a game
            Game game = gameService.startGame(playerOneId, playerTwoId);
            Long gameId = game.getGameId();

            // Notify matched players
            notifyMatchedPlayers(playerOneId, playerTwoId, gameId, game);
            playerQueueService.removeFromQueue(playerOneId);
            playerQueueService.removeFromQueue(playerTwoId);
        }
    }


    public void removeFromQueue(Long playerId) {
        System.out.println("User with id " + playerId + " removed to queue");
        playerQueueService.removeFromQueue(playerId);
    }


    private void notifyMatchedPlayers(Long playerOneId, Long playerTwoId, Long gameId, Game game) {
        Long firstPlayerId = game.getCurrentTurnPlayerId();
        if (firstPlayerId == null) {
            System.err.println("Error: Current turn player ID is null.");
            return;
        }
        String playerTwoName = playerRepository.findUsernameByPlayerId(playerTwoId);
        String playerOneName = playerRepository.findUsernameByPlayerId(playerOneId);
        boolean isFirstPlayerPlayerOne = firstPlayerId.equals(playerOneId);
        MatchmakingResult resultForPlayerOne = new MatchmakingResult(true, gameId, isFirstPlayerPlayerOne, playerTwoId,playerTwoName);
        MatchmakingResult resultForPlayerTwo = new MatchmakingResult(true, gameId, !isFirstPlayerPlayerOne, playerOneId,playerOneName);
        System.out.println(resultForPlayerOne+" Result for PlayerOne");

        System.out.println(resultForPlayerTwo+" Result for PlayerTwo");

        messagingTemplate.convertAndSend("/topic/matchmaking/" + playerOneId.toString(), resultForPlayerOne);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + playerTwoId.toString(), resultForPlayerTwo);
    }
}