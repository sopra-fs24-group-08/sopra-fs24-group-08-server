/*
package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BoardService boardService;

    private User user1, user2;
    private Game game;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void testStartGame_Success() {
        Game startedGame = gameService.startGame(1L, 2L);
        assertNotNull(startedGame);
        String gameStatus = startedGame.getGameStatus().toString();
        assertEquals("initializeNewGame", "ONGOING", gameStatus);
        Mockito.verify(gameRepository, Mockito.times(2)).save(any(Game.class));
        Mockito.verify(gameRepository).saveAndFlush(any(Game.class));
    }


    @Test
    public void testGetWinCountByPlayer() {
        Long playerId = 1L;
        long expectedCount = 5L;
        Mockito.when(gameRepository.countByWinnerId(playerId)).thenReturn(expectedCount);

        long actualCount = gameService.getWinCountByPlayer(playerId);
        Assertions.assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testStartGame_UserNotFound() {
        assertThrows(RuntimeException.class, () -> gameService.startGame(3L, 4L));
    }

    @Test
    public void testGameInitialization() {
        gameService.startGame(1L, 2L);

        Mockito.verify(boardService).initializeAndSaveBoard();
        Mockito.verify(gameRepository).saveAndFlush(any(Game.class)); // Ensuring game is immediately committed
    }
}
*/
