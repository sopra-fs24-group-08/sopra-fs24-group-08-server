package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private void checkForMatches() {
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

    private void broadcastMatchmakingUpdate(Long playerId, String action) {
        String message = String.format("Player%d has %s the matchmaking.", playerId, action);
        messagingTemplate.convertAndSend("/topic/matchmaking/"+playerId, message);
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

    @Transactional
    public void startGameWithFriend(Long senderId ,Long receiverId) {
        if (senderId == null || receiverId == null) {
            System.err.println("Invalid player IDs provided for starting game.");
            return;
        }

        // Start the game
        Game game = gameService.startGame(senderId, receiverId);
        Long gameId = game.getGameId();
        Long firstPlayerId = game.getCurrentTurnPlayerId();

        if (firstPlayerId == null) {
            System.err.println("Error: Current turn player ID is null after game start.");
            return;
        }

        // Fetch player names
        String senderName = playerRepository.findUsernameByPlayerId(senderId);
        String receiverName = playerRepository.findUsernameByPlayerId(receiverId);
        boolean isInviterFirstPlayer = firstPlayerId.equals(senderId);

        MatchmakingResult resultForReceiver = new MatchmakingResult(true, gameId, !isInviterFirstPlayer, senderId, senderName);
        MatchmakingResult resultForSender = new MatchmakingResult(true, gameId, isInviterFirstPlayer, receiverId, receiverName);

        // Debug output
        System.out.println("Preparing to send game start notifications:");
        System.out.println(" - Receiver (" + receiverId + "): " + resultForReceiver);
        System.out.println(" - Sender (" + senderId + "): " + resultForSender);

        // Send notifications
        try {
            messagingTemplate.convertAndSend("/topic/"+receiverId+"/game-notifications", resultForReceiver);
            messagingTemplate.convertAndSend("/topic/"+senderId+"/game-notifications", resultForSender);
            System.out.println("Game " + gameId + " notifications sent to both players.");
        } catch (Exception e) {
            System.err.println("Failed to send game notifications: " + e.getMessage());
        }
    }

}