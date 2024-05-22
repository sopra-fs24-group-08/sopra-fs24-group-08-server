package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class MatchmakingServiceTest {

    @Mock
    private GameService gameService;
    @Mock
    private PlayerQueueService playerQueueService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MatchmakingService matchmakingService;

    @Test
    public void testAddToQueueWithNullPlayerIdThrowsUserNotFoundException() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            matchmakingService.addToQueue(null);
        });

        assertEquals("Attempted to add null playerId to queue", exception.getMessage());
    }
    @Test
    void addToQueue_withValidPlayerId_callsAddToQueueAndCheckForMatches() {
        MatchmakingService spyMatchmakingService = spy(matchmakingService);
        Long playerId = 1L;
        spyMatchmakingService.addToQueue(playerId);
        verify(playerQueueService).addToQueue(playerId);
        verify(spyMatchmakingService).checkForMatches();
    }

    @Test
    void removeFromQueue_callsPlayerQueueService() {
        Long playerId = 1L;
        matchmakingService.addToQueue(playerId);
        matchmakingService.removeFromQueue(playerId);
        verify(playerQueueService).removeFromQueue(playerId);
    }

    @Test
    void testCheckForMatches() {
        MatchmakingService spyMatchmakingService = spy(matchmakingService);
        when(playerQueueService.isEligibleForMatch()).thenReturn(true);
        Map<Long, String> queue = new LinkedHashMap<>();
        queue.put(1L, "User1");
        queue.put(2L, "User2");
        when(playerQueueService.getQueue()).thenReturn(queue);

        Game game = new Game();
        game.setGameId(10L);
        game.setCurrentTurnPlayerId(1L);
        when(gameService.startGame(1L, 2L)).thenReturn(game);
        when(playerRepository.findUsernameByPlayerId(1L)).thenReturn("User1");
        when(playerRepository.findUsernameByPlayerId(2L)).thenReturn("User2");

        spyMatchmakingService.checkForMatches();

        verify(gameService).startGame(1L, 2L);
        verify(spyMatchmakingService).notifyMatchedPlayers(1L, 2L, 10L, game);
        verify(playerQueueService).removeFromQueue(1L);
        verify(playerQueueService).removeFromQueue(2L);
    }
    @Test
    void notifyMatchedPlayers_sendsMessagesCorrectly() {
        Long playerOneId = 1L, playerTwoId = 2L, gameId = 10L;
        Game game = mock(Game.class);
        when(game.getCurrentTurnPlayerId()).thenReturn(playerOneId);
        when(playerRepository.findUsernameByPlayerId(playerOneId)).thenReturn("User1");
        when(playerRepository.findUsernameByPlayerId(playerTwoId)).thenReturn("User2");

        matchmakingService.notifyMatchedPlayers(playerOneId, playerTwoId, gameId, game);

        ArgumentCaptor<MatchmakingResult> captor = ArgumentCaptor.forClass(MatchmakingResult.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/matchmaking/" + playerOneId.toString()), captor.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/matchmaking/" + playerTwoId.toString()), captor.capture());

        List<MatchmakingResult> results = captor.getAllValues();
        assertTrue(results.stream().anyMatch(result -> result.getGameId().equals(gameId) && result.getOpponentId().equals(playerTwoId)));
        assertTrue(results.stream().anyMatch(result -> result.getGameId().equals(gameId) && result.getOpponentId().equals(playerOneId)));
    }

    @Test
    void startGameWithFriend_withNullIds_doesNotStartGame() {
        matchmakingService.startGameWithFriend(null, null);
        verify(gameService, never()).startGame(anyLong(), anyLong());
    }
    @Test
    void startGameWithFriend_withValidIds_startsGameAndNotifiesPlayers() {
        Long senderId = 1L, receiverId = 2L;
        Game game = mock(Game.class);
        when(game.getGameId()).thenReturn(10L);
        when(game.getCurrentTurnPlayerId()).thenReturn(senderId);
        when(gameService.startGame(senderId, receiverId)).thenReturn(game);
        when(playerRepository.findUsernameByPlayerId(senderId)).thenReturn("Alice");
        when(playerRepository.findUsernameByPlayerId(receiverId)).thenReturn("Bob");

        matchmakingService.startGameWithFriend(senderId, receiverId);

        verify(gameService).startGame(senderId, receiverId);
        verify(playerRepository).findUsernameByPlayerId(senderId);
        verify(playerRepository).findUsernameByPlayerId(receiverId);
        verify(messagingTemplate).convertAndSend(eq("/topic/" + receiverId + "/game-notifications"), any(MatchmakingResult.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/" + senderId + "/game-notifications"), any(MatchmakingResult.class));
    }
    @Test
    void startGameWithFriend_withNullCurrentTurnPlayerId_doesNotSendNotifications() {
        Long senderId = 1L, receiverId = 2L;
        Game game = mock(Game.class);
        when(game.getGameId()).thenReturn(10L);
        when(game.getCurrentTurnPlayerId()).thenReturn(null);
        when(gameService.startGame(senderId, receiverId)).thenReturn(game);

        matchmakingService.startGameWithFriend(senderId, receiverId);
        verify(gameService).startGame(senderId, receiverId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(MatchmakingResult.class));
    }
}