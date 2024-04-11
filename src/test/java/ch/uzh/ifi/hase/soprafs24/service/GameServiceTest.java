package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GridRow;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setPlayer1sTurn(true); // 设置当前回合为玩家1
        game.setPlayer1Id(1L); // 假设玩家1的ID为1L
        // 初始化游戏格子
        List<GridRow> grid = Arrays.asList(
                new GridRow(null, null, null),
                new GridRow(null, 0, null), // 第二行中间的格子是blocked
                new GridRow(null, null, null)
        );
        game.setGrid(grid);
    }
    @Test
    public void placeCardOnGrid_ShouldPlaceCard() {
        // Arrange
        Long gameId = game.getId(); // 从setUp中获取游戏ID
        Long playerId = game.getPlayer1Id(); // 从setUp中获取玩家ID
        int cardId = 1; // Example card ID as int
        int row = 0; // 第一行
        int column = 0; // 第一列

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Act
        Game updatedGame = gameService.placeCard(gameId, playerId, cardId, row, column);

        // Assert
        GridRow gridRow = updatedGame.getGrid().get(row);
        assertEquals(Integer.valueOf(cardId), gridRow.getCell1()); // 验证卡牌是否被放置
    }

    @Test
    void tossCoin_shouldSetAwaitingPlayerChoice() {
        given(gameRepository.findById(1L)).willReturn(java.util.Optional.of(game));
        given(gameRepository.save(game)).willReturn(game);

        Game updatedGame = gameService.tossCoin(1L);

        assertTrue(updatedGame.getAwaitingPlayerChoice());
    }

    @Test
    void chooseStartingPlayer_shouldUpdatePlayer1sTurn() {
        game.setAwaitingPlayerChoice(true);
        given(gameRepository.findById(1L)).willReturn(java.util.Optional.of(game));
        given(gameRepository.save(game)).willReturn(game);

        Game updatedGame = gameService.chooseStartingPlayer(1L, true);

        assertFalse(updatedGame.getAwaitingPlayerChoice());
        assertTrue(updatedGame.getPlayer1sTurn());
    }

    @Test
    void placeCard_whenNotPlayersTurn_shouldThrowException() {
        game.setPlayer1sTurn(true);
        Long playerIdTryingToPlaceCard = 2L;
        given(gameRepository.findById(1L)).willReturn(java.util.Optional.of(game));

        // 尝试以玩家2的身份放置卡牌，预期抛出异常
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameService.placeCard(1L, playerIdTryingToPlaceCard, 1, 0, 0);
        }, "应该抛出异常因为不是玩家的回合");


        assertEquals("It's not your turn", exception.getMessage(), "异常信息应当匹配");


        verify(gameRepository, never()).save(any(Game.class));
    }

}