package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.entity.GridRow;
import java.util.List;

@Service
public class GameService {

    private static final int BLOCKED = 0;
    private static final int EMPTY = -1;

    @Autowired
    private GameRepository gameRepository;

    public Game createNewGame() {
        Game game = new Game(); // New game with default grid
        return gameRepository.save(game);
    }

    public Game tossCoin(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        game.setCoinTossResult(Math.random() < 0.5); // Randomly determining the heads and tails of coins
        game.setAwaitingPlayerChoice(true); // Setting the game to wait for player selection
        return gameRepository.save(game);
    }

    public Game chooseStartingPlayer(Long gameId, boolean player1Starts) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getAwaitingPlayerChoice()) {
            throw new IllegalStateException("Not awaiting player choice");
        }

        game.setPlayer1sTurn(player1Starts);
        game.setAwaitingPlayerChoice(false); // Players have made their choice and are no longer waiting
        return gameRepository.save(game);
    }

    public Game placeCard(Long gameId, Long playerId, int cardId, int row, int col) {
        // 获取游戏实例
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // 检查是否轮到当前玩家
        boolean isPlayer1 = game.getPlayer1Id() != null && game.getPlayer1Id().equals(playerId);
        boolean isPlayer2 = game.getPlayer2Id() != null && game.getPlayer2Id().equals(playerId);
        if (!((game.getPlayer1sTurn() != null && game.getPlayer1sTurn() && isPlayer1) ||
                (game.getPlayer1sTurn() != null && !game.getPlayer1sTurn() && isPlayer2))) {
            throw new IllegalStateException("It's not your turn");
        }

        // Get the current grid
        List<GridRow> currentGrid = game.getGrid();

        // Validate the selected cell
        GridRow gridRow = currentGrid.get(row);
        if (col == 1 && gridRow.getCell2() == BLOCKED) {
            throw new IllegalArgumentException("Cannot place a card in a blocked slot");
        }

        Integer cellValue = col == 0 ? gridRow.getCell1() : (col == 1 ? gridRow.getCell2() : gridRow.getCell3());
        if (cellValue != null && cellValue != EMPTY) {
            throw new IllegalArgumentException("Slot is already occupied");
        }

        // Place the card
        if (col == 0) {
            gridRow.setCell1(cardId);
        } else if (col == 1) {
            gridRow.setCell2(cardId);
        } else if (col == 2) {
            gridRow.setCell3(cardId);
        }

        // Save the updated game
        return gameRepository.save(game);
    }
}
