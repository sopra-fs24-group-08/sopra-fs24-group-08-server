/*
package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private BoardService boardService;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private GameService gameService;

    private User user1, user2;
    private Game game;

    @BeforeEach
    public void setup() {
        user1 = new User();
        user1.setId(1L);
        user2 = new User();
        user2.setId(2L);

        game = new Game();
        game.setGameId(1L);
        game.setPlayers(new ArrayList<>(Arrays.asList(new Player(), new Player())));
    }

    @Test
    public void testCreateGame() {
        when(boardService.initializeAndSaveBoard()).thenReturn(new Board());
        when(gameRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Game createdGame = gameService.createGame();

        assertNotNull(createdGame);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    public void testStartGame() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        when(boardService.initializeAndSaveBoard()).thenReturn(new Board());

        Game startedGame = gameService.startGame(user1.getId(), user2.getId());

        assertNotNull(startedGame);
        assertEquals(GameStatus.ONGOING, startedGame.getGameStatus());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    public void testHandlePlayerSurrender() {
        game.setGameStatus(GameStatus.ONGOING);
        Player player1 = new Player();
        player1.setId(user1.getId());
        player1.setUser(user1);
        Player player2 = new Player();
        player2.setId(user2.getId());
        player2.setUser(user2);

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        assertDoesNotThrow(() -> gameService.handlePlayerSurrender(game.getGameId(), player1.getId()));
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
        verify(gameRepository, times(2)).save(game);
    }

    @Test
    public void testCheckGameOverConditions_NoConditionMet() {
        when(boardService.isAllSquaresOccupied(any())).thenReturn(false);
        assertFalse(gameService.checkGameOverConditions(game));
    }

    @Test
    public void testCheckGameOverConditions_GameOver() {
        when(boardService.isAllSquaresOccupied(any())).thenReturn(true);
        when(gameRepository.save(any())).thenReturn(game);
        assertTrue(gameService.checkGameOverConditions(game));
    }

    // TODO Additional tests should be created for other methods such as processMove, finishGame, etc.

}
*/
