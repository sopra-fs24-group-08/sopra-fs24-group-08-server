package ch.uzh.ifi.hase.soprafs24.eventlistener;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameCleanupEvent;
import ch.uzh.ifi.hase.soprafs24.EventListener.GameCleanupListener;
import ch.uzh.ifi.hase.soprafs24.EventListener.GameEndEvent;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameCleanupService;
import ch.uzh.ifi.hase.soprafs24.service.util.GameEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameCleanupListenerTest {

    @Mock
    private GameCleanupService gameCleanupService;

    @Mock
    private GameEventService gameEventService;

    @Mock
    private SimpMessagingTemplate messagingTemplate; // Inject if used by gameEventService

    @InjectMocks
    private GameCleanupListener gameCleanupListener;

    @Mock
    private Game game;

    @Mock
    private Player winner;

    @Mock
    private Player loser;

    @Mock
    private GameStateDTO winnerState;

    @Mock
    private GameStateDTO loserState;

    @Mock
    private Map<Long, GameStateDTO> gameStateMap;


    @Test
    public void testOnGameCleanup_NullGame_LogsError() {
        GameCleanupEvent event = new GameCleanupEvent(this,game); // No game object passed
        ArgumentCaptor<GameEndEvent> eventCaptor = ArgumentCaptor.forClass(GameEndEvent.class);

        gameCleanupListener.onGameCleanup(event);

        verify(gameEventService).handleGameEnd(eventCaptor.capture());

        assertEquals(0, eventCaptor.getValue().getGame().getGameId());// Assert that the gameId is  0
    }

    @Test
    public void testOnGameCleanup_SuccessfulExecution() throws Exception {
        when(game.getWinner()).thenReturn(winner);
        when(game.getLoser()).thenReturn(loser);
        when(gameCleanupService.prepareGameEndData(game)).thenReturn(gameStateMap);

        GameCleanupEvent event = new GameCleanupEvent(this, game); // Set game in the constructor

        gameCleanupListener.onGameCleanup(event);

        verify(gameCleanupService).prepareGameEndData(game);
        verify(gameCleanupService).cleanupGameData(game);
        verify(gameEventService).handleGameEnd(new GameEndEvent(gameCleanupListener, game, winner, loser, gameStateMap));
    }
}
