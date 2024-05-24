package ch.uzh.ifi.hase.soprafs24.service.util;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameEndEvent;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.IncompleteGameDataException;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameEventServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameEventService gameEventService;

    @Mock
    private Game game;

    @Mock
    private GameStateDTO winnerState;

    @Mock
    private GameStateDTO loserState;


    @Test
    public void testHandleGameEnd_IncompleteGameDataThrowsCustomException() throws Exception {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("username2");
        Player player2 = new Player();
        player2.setId(2L);
        when(game.getWinnerUser()).thenReturn(null); // Set winner to null for incomplete data

        Map<Long, GameStateDTO> gameStateMap = new HashMap<>();
        gameStateMap.put(1L, winnerState);
        gameStateMap.put(2L, loserState);
        GameEndEvent event = new GameEndEvent(this, game, null, player2, gameStateMap);

        // Use assertThrows to verify the exception is thrown
        Exception exception = assertThrows(IncompleteGameDataException.class, () -> {
            gameEventService.handleGameEnd(event);
        });
        assertEquals("Game data is incomplete when handling game end.", exception.getMessage());
    }

    @Test
    public void testHandleGameEnd_SendsMessagesToCorrectTopics() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("username1");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("username2");
        Player player1 = new Player();
        player1.setId(1L);
        Player player2 = new Player();
        player2.setId(2L);


        when(game.getWinnerUser()).thenReturn(user1);
        when(game.getLoserUser()).thenReturn(user2);

        when(game.getGameId()).thenReturn(30L);

        Map<Long, GameStateDTO> gameStateMap = new HashMap<>();
        gameStateMap.put(1L, winnerState);
        gameStateMap.put(2L, loserState);
        GameEndEvent event = new GameEndEvent(this, game, player1, player2, gameStateMap);

        gameEventService.handleGameEnd(event);

        // Verify message sending with expected topics and content
        verify(messagingTemplate).convertAndSend("/topic/game/30/1", winnerState);
        verify(messagingTemplate).convertAndSend("/topic/game/30/2", loserState);
    }
}

