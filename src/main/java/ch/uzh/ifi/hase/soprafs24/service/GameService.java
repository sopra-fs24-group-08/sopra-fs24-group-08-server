package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.BoardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import org.apache.catalina.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private final BoardRepository boardRepository;
    private final ScoreRepository scoreRepository;
    private final Map<Long, Player> players;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 返回给定ID的Player对象
    public Player getPlayerById(Long playerId) {
        return players.get(playerId);
    }

    // 包级别方法，只在同一个包内可见
    void addPlayer(Long playerId, Player player) {
        this.players.put(playerId, player);
    }

    @Autowired
    public GameService(BoardRepository boardRepository, ScoreRepository scoreRepository) {
        this.boardRepository = boardRepository;
        this.scoreRepository = scoreRepository;
        this.players = new HashMap<>();
        // 初始化玩家
        this.players.put(1L, new Player(new CardPile(), new User()));
        this.players.put(2L, new Player(new CardPile(), new User()));
    }

    public void saveScore(Long playerId, Long gameId, Integer scoreValue) {
        Score score = new Score();
        score.setPlayerId(playerId);
        score.setGameId(gameId);
        score.setScore(scoreValue);
        score.setTimestamp(LocalDateTime.now());
        scoreRepository.save(score);
    }

    private int calculateScore(Card card, GridSquare square) {
        int baseScore = card.getPoints();
        String squareColor = square.getColor();
        String cardColor = card.getColor();

        if (squareColor == null || cardColor == null) {
            return 0;  // 如果颜色信息不完整，则返回0分
        }

        if ("white".equals(squareColor)) {
            return baseScore;  // 白色格子特殊规则
        } else if (squareColor.equals(cardColor)) {
            return baseScore * 2;  // 颜色匹配
        } else {
            return 0;  // 不匹配
        }
    }

    public Board createNewGame() {
        Board board = new Board();  // 初始化棋盘，包括玩家分配和棋格设置
        board.setPlayer1Id(1L); //
        board.setPlayer2Id(2L);
        return boardRepository.save(board);
    }

    public Board tossCoin(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.setCoinTossResult(Math.random() < 0.5); // 随机硬币决定谁先行
        board.setAwaitingPlayerChoice(true);
        return boardRepository.save(board);
    }

    public Board tossCoinAndDecideAutomatically(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.setCoinTossResult(Math.random() < 0.5);
        board.setAwaitingPlayerChoice(true);
        boardRepository.save(board);

        // Schedule a task to automatically choose the starting player if no choice is made within 15 seconds
        scheduler.schedule(() -> {
            if (board.getAwaitingPlayerChoice()) {  // Check if still awaiting player's decision
                boolean autoDecision = Math.random() < 0.5;  // Random decision
                chooseStartingPlayer(boardId, autoDecision);
            }
        }, 15, TimeUnit.SECONDS);

        return board;
    }


    public Board chooseStartingPlayer(Long boardId, boolean player1Starts) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        if (!board.getAwaitingPlayerChoice()) {

            return board;  // Return if the choice has already been made

        }
        board.setPlayer1sTurn(player1Starts);
        board.setAwaitingPlayerChoice(false);
        return boardRepository.save(board);
    }

    private void dealInitialCards(boolean player1Starts, Board board) {
        Player player1 = players.get(board.getPlayer1Id());
        Player player2 = players.get(board.getPlayer2Id());

        CardPile sharedPile = new CardPile(); // 每个玩家共享一个牌堆，或者各自有自己的牌堆

        if (player1Starts) {
            for (int i = 0; i < 2; i++) {
                player1.drawCard(sharedPile); // 先手玩家抽2张牌
            }
            for (int i = 0; i < 3; i++) {
                player2.drawCard(sharedPile); // 后手玩家抽3张牌
            }
        } else {
            for (int i = 0; i < 2; i++) {
                player2.drawCard(sharedPile); // 先手玩家抽2张牌
            }
            for (int i = 0; i < 3; i++) {
                player1.drawCard(sharedPile); // 后手玩家抽3张牌
            }
        }
    }



    // 检查游戏是否结束
    public boolean isGameOver(Board board) {
        // 棋盘满时游戏结束
        return board.isFull();
    }

    // 游戏结束后的行为 待实现
    private void handleGameOver(Board board) {
        // ......
    }

    public boolean isPlayerTurn(Long playerId, Board board) {
        return (board.getPlayer1sTurn() && board.getPlayer1Id().equals(playerId)) ||
                (!board.getPlayer1sTurn() && board.getPlayer2Id().equals(playerId));
    }


    public Board placeCard(Long boardId, Long playerId, Card card, int position) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalStateException("Player not found");
        }

        // 检查是否轮到当前玩家
        boolean isPlayerTurn = (board.getPlayer1sTurn() && board.getPlayer1Id().equals(playerId)) ||
                (!board.getPlayer1sTurn() && board.getPlayer2Id().equals(playerId));
        if (!isPlayerTurn) {
            throw new IllegalStateException("It's not your turn");
        }

        // 尝试放置卡牌并计算分数
        if (!player.playCard(board, card, position)) {
            throw new IllegalStateException("Invalid move");
        }

        GridSquare square = board.getGrid(position);
        int score = calculateScore(card, square);
        saveScore(playerId, boardId, score);

        // 保存棋盘状态
        boardRepository.save(board);

        // 检查游戏是否结束
        if (isGameOver(board)) {
            handleGameOver(board);
        } else {
            // 如果游戏未结束，轮换到下一个玩家
            switchPlayerTurn(board);
            // 保存更新后的棋盘状态
            boardRepository.save(board);
        }

        return board;
    }


    // 轮换玩家
    private void switchPlayerTurn(Board board) {
        board.setPlayer1sTurn(!board.getPlayer1sTurn());
    }

}
