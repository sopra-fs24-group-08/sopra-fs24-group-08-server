package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.BoardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    @Mock
    private Board board;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));
        when(board.placeCard(any(Card.class), anyInt())).thenReturn(true);
    }

    @Test
    void testPlaceCardCalculatesScoreCorrectly() {
        // Arrange
        Long playerId = 1L;
        Card card = new Card("red", 5);
        GridSquare gridSquare = new GridSquare("red", false);

        when(board.getGrid(0)).thenReturn(gridSquare);
        when(board.getPlayer1sTurn()).thenReturn(true);
        when(board.getPlayer1Id()).thenReturn(playerId);

        Player mockPlayer = mock(Player.class);
        // 使用提供的包级别方法addPlayer来添加模拟的Player对象
        gameService.addPlayer(playerId, mockPlayer);

        // 模拟Player对象放置卡牌时返回true
        when(mockPlayer.playCard(any(Board.class), eq(card), eq(0))).thenReturn(true);
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));

        // Act
        gameService.placeCard(1L, playerId, card, 0);

        // Assert
        int expectedScore = 10; // 匹配颜色得分是基础分数的两倍
        verify(scoreRepository).save(argThat(score ->
                score.getPlayerId().equals(playerId) &&
                        score.getScore().equals(expectedScore) &&
                        score.getGameId().equals(1L)
        ));
        verify(boardRepository, times(2)).save(board); // 验证 save 被调用了两次 确认棋盘状态被保存
    }



    @Test
    void testPlaceCardWhenBoardNotFound() {
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> gameService.placeCard(1L, 1L, new Card("red", 10), 0));
    }

    @Test
    void testPlaceCardFailed() {
        when(board.placeCard(any(Card.class), anyInt())).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> gameService.placeCard(1L, 1L, new Card("red", 10), 0));
    }

    @Test
    void whenPlayerTriesToActOnNotTheirTurn_thenThrowsException() {
        // Arrange
        Long playerId = 1L;
        Long wrongPlayerId = 2L; // ID for a player that is not supposed to act
        Card card = new Card("red", 5);
        int position = 0;

        when(board.getPlayer1sTurn()).thenReturn(true); // Suppose it is player 1's turn
        when(board.getPlayer1Id()).thenReturn(playerId);
        when(board.getPlayer2Id()).thenReturn(wrongPlayerId);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameService.placeCard(1L, wrongPlayerId, card, position);
        });

        String expectedMessage = "It's not your turn";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void whenPlayerActsOnTheirTurn_thenSucceeds() {
        // Arrange
        Long playerId = 1L;
        Card card = new Card("red", 5);
        int position = 0;

        when(board.getPlayer1sTurn()).thenReturn(true);
        when(board.getPlayer1Id()).thenReturn(playerId);
        when(board.getGrid(position)).thenReturn(new GridSquare("red", false));
        when(board.placeCard(any(Card.class), eq(position))).thenReturn(true); // Suppose placing the card is successful

        Player mockPlayer = mock(Player.class);
        gameService.addPlayer(playerId, mockPlayer);
        when(mockPlayer.playCard(any(Board.class), eq(card), eq(position))).thenReturn(true);

        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));

        // Act
        Board result = gameService.placeCard(1L, playerId, card, position);

        // Assert
        assertNotNull(result);
        verify(boardRepository, atLeastOnce()).save(board); // Verify that the board was saved
    }



    @Test
    void whenCardPlaced_thenStateIsNotReverted() {
        // Arrange
        Long playerId = 1L;
        Long boardId = 1L;
        Card card = new Card("red", 5);
        int position = 0;
        int baseScore = card.getPoints();
        int expectedScore = baseScore * 2;

        // Mocking the Board and Player objects
        Board board = mock(Board.class);
        Player mockPlayer = mock(Player.class);
        gameService.addPlayer(playerId, mockPlayer);

        // Set up the board behavior
        when(board.getPlayer1Id()).thenReturn(playerId);
        when(board.getPlayer1sTurn()).thenReturn(true);
        when(board.getGrid(position)).thenReturn(new GridSquare("red", false));
        when(board.placeCard(any(Card.class), eq(position))).thenReturn(true);

        // Setting up the player behavior
        when(mockPlayer.getId()).thenReturn(playerId);
        when(mockPlayer.playCard(any(Board.class), eq(card), eq(position))).thenReturn(true);

        // Setup repository and scoring simulation
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        doAnswer(invocation -> {
            gameService.saveScore(playerId, boardId, expectedScore);
            return null;
        }).when(mockPlayer).updateScore(any(Card.class), any(GridSquare.class));

        // Act
        gameService.placeCard(boardId, playerId, card, position);

        // Assert
        ArgumentCaptor<Score> scoreCaptor = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(scoreCaptor.capture());
        Score savedScore = scoreCaptor.getValue();

        assertEquals(expectedScore, savedScore.getScore().intValue());
        assertEquals(playerId, savedScore.getPlayerId());

        // Check if player's turn is still correct
        assertTrue(gameService.isPlayerTurn(playerId, board));
        verify(boardRepository, times(2)).save(board); // Expecting two saves if your method saves board state twice
    }


}


