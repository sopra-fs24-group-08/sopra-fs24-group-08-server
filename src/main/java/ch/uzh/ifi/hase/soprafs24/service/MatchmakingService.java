package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;

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
        this.playerQueueService = playerQueueService;
    }

    public void addToQueue(Long playerId) {
        if (playerId == null) {
            throw new UserNotFoundException("Attempted to add null playerId to queue");
        }
        playerQueueService.addToQueue(playerId);
        checkForMatches();
    }

    public void checkStatusBeforeMatch(Long userId){
      Player player = playerRepository.findById(userId).orElse(null);
      if (player != null){
        Game game = player.getGame();
        gameService.handlePlayerSurrender(game.getGameId(), userId);
      }
    }

    public void checkForMatches() {
        if (playerQueueService.isEligibleForMatch()) {
            Iterator<Long> iterator = playerQueueService.getQueue().keySet().iterator();
            Long playerOneId = iterator.next();
            Long playerTwoId = iterator.next();

            // Create a game
            Game game = gameService.startGame(playerOneId, playerTwoId);

            // Notify matched players
            notifyMatchedPlayers(playerOneId, playerTwoId, game.getGameId(), game);
            playerQueueService.removeFromQueue(playerOneId);
            playerQueueService.removeFromQueue(playerTwoId);
        }
    }


    public void removeFromQueue(Long playerId) {
        System.out.println("User with id " + playerId + " removed to queue");
        playerQueueService.removeFromQueue(playerId);
    }

    public void notifyMatchedPlayers(Long playerOneId, Long playerTwoId, Long gameId, Game game) {
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
            return;
        }

        // Start the game
        Game game = gameService.startGame(senderId, receiverId);
        Long gameId = game.getGameId();
        Long firstPlayerId = game.getCurrentTurnPlayerId();

        if (firstPlayerId == null) {
            return;
        }

        // Fetch player names
        String senderName = playerRepository.findUsernameByPlayerId(senderId);
        String receiverName = playerRepository.findUsernameByPlayerId(receiverId);
        boolean isInviterFirstPlayer = firstPlayerId.equals(senderId);

        MatchmakingResult resultForReceiver = new MatchmakingResult(true, gameId, !isInviterFirstPlayer, senderId, senderName);
        MatchmakingResult resultForSender = new MatchmakingResult(true, gameId, isInviterFirstPlayer, receiverId, receiverName);

        messagingTemplate.convertAndSend("/topic/"+receiverId+"/game-notifications", resultForReceiver);
        messagingTemplate.convertAndSend("/topic/"+senderId+"/game-notifications", resultForSender);
    }

}