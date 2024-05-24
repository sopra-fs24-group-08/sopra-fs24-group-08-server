
package ch.uzh.ifi.hase.soprafs24.service;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
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
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        // This ensures all dependencies are reset before each test
        gameService = new GameService(gameRepository, userRepository, playerRepository, messagingTemplate, chatRoomRepository, boardService, eventPublisher,boardRepository, entityManager);
    }

    @Test
    void testGetGameFound() {
        Long gameId = 1L;
        Game expectedGame = new Game();
        expectedGame.setGameId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(expectedGame));

        Game actualGame = gameService.getGame(gameId);

        assertNotNull(actualGame);
        assertEquals(expectedGame, actualGame);
    }

    @Test
    void testGetGameNotFound() {
        Long gameId = 1L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> {
            gameService.getGame(gameId);
        });
    }

    @Test
    void testStartGame() {
        Long userId1 = 1L, userId2 = 2L;
        User user1 = new User(), user2 = new User();
        Game mockGame = new Game();
        Board mockBoard = new Board();
        ChatRoom mockChatRoom = new ChatRoom();

        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(boardService.initializeAndSaveBoard()).thenReturn(mockBoard);
        when(gameRepository.save(any(Game.class))).thenReturn(mockGame);

        Game startedGame = gameService.startGame(userId1, userId2);

        assertNotNull(startedGame);
        verify(userRepository).findById(userId1);
        verify(userRepository).findById(userId2);
        verify(gameRepository, times(2)).save(any(Game.class));
    }

    @Test
    void testHandlePlayerSurrender() {
        Long gameId = 1L;
        Long surrenderingPlayerId = 1L;
        Game game = new Game();
        game.setGameId(gameId);
        game.setGameStatus(GameStatus.ONGOING);
        Player surrenderingPlayer = new Player();
        surrenderingPlayer.setId(surrenderingPlayerId);
        Player winningPlayer = new Player();
        winningPlayer.setId(2L);

        game.addPlayer(surrenderingPlayer);
        game.addPlayer(winningPlayer);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.handlePlayerSurrender(gameId, surrenderingPlayerId);

        verify(gameRepository).save(game);
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
        assertNotNull(game.getWinner());
    }

    @Test
    void testGetGameStateForPlayer() {
        // Setup
        Long playerId = 1L;
        Long opponentId = 2L;
        Game game = new Game();
        game.setBoard(new Board());
        Player player = new Player();
        player.setId(playerId);
        player.setScore(10);
        player.setHand(new ArrayList<>());

        Player opponent = new Player();
        opponent.setId(opponentId);
        opponent.setScore(5);
        opponent.setHand(new ArrayList<>());

        game.addPlayer(player);
        game.addPlayer(opponent);

        GameStateDTO expectedGameState = new GameStateDTO();
        expectedGameState.setCurrentScore(player.getScore());
        expectedGameState.setOpponentScore(opponent.getScore());

        GameStateDTO actualGameState = gameService.getGameStateForPlayer(game, playerId);

        // Verification
        assertNotNull(actualGameState);
        assertEquals(expectedGameState.getCurrentScore(), actualGameState.getCurrentScore());
        assertEquals(expectedGameState.getOpponentScore(), actualGameState.getOpponentScore());
    }

    @Test
    void testGetPlayerByIdFound() {
        Long playerId = 1L;
        Player expectedPlayer = new Player();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(expectedPlayer));

        Player actualPlayer = gameService.getPlayerById(playerId);

        assertNotNull(actualPlayer);
        assertEquals(expectedPlayer, actualPlayer);
    }

    @Test
    void testGetPlayerByIdNotFound() {
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> gameService.getPlayerById(playerId));
    }

    @Test
    void testRetrieveOnlyCommittedGame() {
        Long gameId = 1L;
        Game expectedGame = new Game();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(expectedGame));
        Game actualGame = gameService.retrieveOnlyCommittedGame(gameId);

        assertNotNull(actualGame);
        assertEquals(expectedGame, actualGame);
        verify(entityManager).refresh(expectedGame);  // Verify refresh was called with the correct game
    }

    @Test
    void testFinishGame() {
        Game game = new Game();
        Player winner = new Player();
        Player loser = new Player();
        winner.setId(1L);
        loser.setId(2L);

        gameService.finishGame(game, winner, loser);

        assertEquals(GameStatus.FINISHED, game.getGameStatus());
        assertEquals(winner, game.getWinner());
        assertEquals(loser, game.getLoser());
        verify(gameRepository).save(game);
    }


    @Test
    void testValidateTurn_ValidScenario() {
        Long gameId = 1L;
        Long playerId = 1L;
        Game game = new Game();
        game.setGameId(gameId);
        game.setCurrentTurnPlayerId(playerId);

        when(gameRepository.findByGameId(gameId)).thenReturn(game);

        assertDoesNotThrow(() -> gameService.validateTurn(gameId, playerId, playerId));
    }

    @Test
    void testValidateTurn_InvalidUserId() {
        Long gameId = 1L;
        Long playerId = 1L;
        Long wrongUserId = 2L;
        Game game = new Game();
        game.setGameId(gameId);
        game.setCurrentTurnPlayerId(playerId);


        assertThrows(RuntimeException.class, () -> gameService.validateTurn(gameId, playerId, wrongUserId));
    }

    @Test
    void testValidateTurn_GameNotFound() {
        Long gameId = 1L;
        Long playerId = 1L;
        when(gameRepository.findByGameId(gameId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> gameService.validateTurn(gameId, playerId, playerId));
    }

    @Test
    void testValidateTurn_NotPlayersTurn() {
        Long gameId = 1L;
        Long playerId = 1L;
        Long otherPlayerId = 2L;
        Game game = new Game();
        game.setGameId(gameId);
        game.setCurrentTurnPlayerId(otherPlayerId);

        when(gameRepository.findByGameId(gameId)).thenReturn(game);

        assertThrows(NotYourTurnException.class, () -> gameService.validateTurn(gameId, playerId, playerId));
    }

    @Test
    void testProcessMoveInvalidPlayer() {
        Long gameId = 1L;
        MoveDTO move = new MoveDTO();
        move.setPlayerId(2L); // Wrong player ID, not the current turn's player
        move.setMoveType(MoveType.DRAW);

        Game game = new Game();
        game.setGameId(gameId);
        game.setCurrentTurnPlayerId(1L); // It's player 1's turn, not player 2

        Player player = new Player();
        player.setId(2L);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player));

        // Expect an exception because it's not player 2's turn
        assertThrows(RuntimeException.class, () -> gameService.processMove(gameId, move));
    }
    @Test
    void getWinnerValidAttempt(){
        Long gameId = 1L;
    }

    @Test
    void GetGameMatchResultValid() {
        Long gameId = 1L;
        Game game = mock(Game.class);
        when(game.getGameStatus()).thenReturn(GameStatus.FINISHED);
        when(game.getWinnerUser()).thenReturn(new User());
        when(game.getLoserUser()).thenReturn(new User());
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        assertDoesNotThrow(() -> gameService.getGameMatchResult(gameId));
    }
    @Test
    void testGetGameMatchResultGameNotFinished() {
        Long gameId = 1L;
        Game game = mock(Game.class);
        when(game.getGameStatus()).thenReturn(GameStatus.ONGOING);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        assertThrows(GameNotFinishedException.class, () -> gameService.getGameMatchResult(gameId));
    }
    @Test
    void testGetGameMatchResultIncompleteData() {
        Long gameId = 1L;
        Game game = mock(Game.class);
        when(game.getGameStatus()).thenReturn(GameStatus.FINISHED);
        when(game.getWinnerUser()).thenReturn(null);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        assertThrows(IncompleteGameDataException.class, () -> gameService.getGameMatchResult(gameId));
    }
    @Test
    void testValidateTurnInvalidUser() {
        Long gameId = 1L, playerId = 1L, wrongUserId = 2L;
        Game game = new Game();
        game.setCurrentTurnPlayerId(playerId);
        assertThrows(RuntimeException.class, () -> gameService.validateTurn(gameId, playerId, wrongUserId));
    }

    @Test
    void testValidateTurnCorrectPlayer() {
        Long gameId = 1L, playerId = 1L;
        Game game = new Game();
        game.setCurrentTurnPlayerId(playerId);
        when(gameRepository.findByGameId(gameId)).thenReturn(game);
        assertDoesNotThrow(() -> gameService.validateTurn(gameId, playerId, playerId));
    }



}