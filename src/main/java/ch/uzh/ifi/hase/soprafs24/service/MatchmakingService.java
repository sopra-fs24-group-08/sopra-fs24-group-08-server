package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {

    private final Map<Long, String> playerQueue = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public MatchmakingService(SimpMessagingTemplate messagingTemplate, GameService gameService, UserRepository userRepository, PlayerRepository playerRepository) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public void addToQueue(Long playerId) {
        if (playerId == null) {
            System.out.println("Attempted to add null playerId to queue");
            return; // Handle or log the error as needed
        }
        System.out.println("User with id " + playerId + " added to queue");
        playerQueue.put(playerId, ""); // Use an empty string or a specific placeholder if the value must not be null
        checkForMatches();
    }


    public void removeFromQueue(Long playerId) {
        System.out.println("User with id " + playerId + " removed to queue");
        playerQueue.remove(playerId);
    }

    private void broadcastMatchmakingUpdate(Long playerId, String action) {
        String message = String.format("Player%d has %s the matchmaking.", playerId, action);
        messagingTemplate.convertAndSend("/topic/matchmaking/"+playerId, message);
    }

    private void checkForMatches() {
        Iterator<Long> iterator = playerQueue.keySet().iterator();
        if (iterator.hasNext()) {
            Long playerOneId = iterator.next();
            if (iterator.hasNext()) {
                Long playerTwoId = iterator.next();

                // Start a new game for the two players
                Game game = gameService.startGame(playerOneId, playerTwoId);
                Long gameId = game.getGameId();

                // Notify both players of the match result
                playerQueue.remove(playerOneId);
                playerQueue.remove(playerTwoId);
                notifyMatchedPlayers(playerOneId, playerTwoId, gameId, game);

            }
        }
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