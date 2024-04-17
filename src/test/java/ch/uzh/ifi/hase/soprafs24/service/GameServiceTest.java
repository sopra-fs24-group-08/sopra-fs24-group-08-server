package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.GameElements.Card;
import ch.uzh.ifi.hase.soprafs24.entity.GameElements.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private Player player1;
    private Player player2;
    private Card card;
    private int position;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
        player1 = new Player();
        player2 = new Player();
        card = new Card("Red", 5);
        position = 6;
        gameService.initializeGame(player1, player2);
        player1.handCards.add(card);
        gameService.isOnTurn = true;
    }
    @Test
    void testPlaceCard() {
        // 现在尝试放置一张卡牌
        assertTrue(gameService.placeCard(player1, card, position));

        // 验证卡牌是否被放置
        GridSquare square = gameService.board.getGrid(position);
        assertTrue(square.getOccupied());

        // 验证分数是否正确更新
        // 假设该位置的颜色为红色，因此分数应该翻倍
        assertEquals(10, player1.getScore());

        // 验证轮次是否已切换到player2
        assertFalse(gameService.isPlayerTurn(player1));
        assertTrue(gameService.isPlayerTurn(player2));
    }
}
