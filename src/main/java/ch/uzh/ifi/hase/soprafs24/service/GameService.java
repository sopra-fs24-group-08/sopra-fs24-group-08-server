package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public Game createGame() {
        Game game = new Game();
        Board board = new Board();
        game.setBoard(board);
        gameRepository.save(game);
        return game;
    }

    //Leaving in encase it's used for Polling or if we want to add different beh. to friendly games
    public Game startFriendsGame(Long gameId,Long userId1, Long userId2){
        return startGame(gameId,userId1,userId2);
    }

    public Game startGame(Long gameId, Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Player player1 = new Player(user1, game);
        Player player2 = new Player(user2, game);

        boolean firstPlayerStarts = new Random().nextBoolean();
        dealInitialCards(game.getBoard(), player1, player2, firstPlayerStarts);

        playerRepository.saveAll(Arrays.asList(player1, player2));

        game.setPlayers(Arrays.asList(player1, player2));
        game.setGameStatus(GameStatus.ONGOING);
        gameRepository.save(game);
        return game;
    }

    private void dealInitialCards(Board board, Player player1, Player player2, boolean firstPlayerStarts) {
        Player firstPlayer = firstPlayerStarts ? player1 : player2;
        Player secondPlayer = firstPlayerStarts ? player2 : player1;
        giveCards(firstPlayer, board, 2);
        giveCards(secondPlayer, board, 3);
    }

    private void giveCards(Player player, Board board, int count) {
        for (int i = 0; i < count; i++) {
            Card card = board.drawCardFromPile();
            if (card != null) {
                player.addCardToHand(card);
            }
        }
    }

    public void processMove(Long gameId, MoveDTO move) {
        if (move.getMoveType() == MoveType.PLACE) {
            placeCard(gameId, move);
        } else if (move.getMoveType() == MoveType.DRAW) {
            drawCard(gameId, move);
        } else {
            throw new IllegalArgumentException("Unsupported move type: " + move.getMoveType());
        }
    }

    private void drawCard(Long gameId, MoveDTO move) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(move.getPlayerId()).orElseThrow();
        Board board = game.getBoard();
        if (board.getCentralCardPile().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No more cards to draw");
        }
        Card card = board.drawCardFromPile();
        player.addCardToHand(card);
        playerRepository.save(player);
        gameRepository.save(game);

        // Logic to switch the turn to the other player
        switchTurns(game, player);
    }

    public void placeCard(Long gameId, MoveDTO move) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Player player = playerRepository.findById(move.getPlayerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        Card card = player.getCardFromHand(move.getCardId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        Board board = game.getBoard();
        int position = move.getPosition();
        String squareColor = board.getSquareColor(position);

        if (!board.isSquareOccupied(position)) {
            int points = attemptToPlaceCardOnBoard(card, squareColor, board, position);
            if (points > 0) {
                player.addScore(points);
                player.removeCardFromHand(card);
                playerRepository.save(player);
                gameRepository.save(game);
                switchTurns(game, player);
                // Optional: Send WebSocket update to all clients
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot place card on the chosen square");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Square already occupied");
        }
    }

    private void switchTurns(Game game, Player currentPlayer) {
        game.getPlayers().forEach(p -> {
            if (!p.getId().equals(currentPlayer.getId())) {
                game.setCurrentTurnPlayerId(p.getId());
            }
        });
    }

    public Game retrieveGameState(Long gameId){
        return gameRepository.findById(gameId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Game was not found"));
    }

    private int attemptToPlaceCardOnBoard(Card card, String squareColor, Board board, int position) {
        int points = card.getPoints();
        if (squareColor.equals(card.getColor())) {
            points *= 2; // Double the points if colors match
        }
        board.setCardAtPosition(card, position);
        return points;
    }

    private void updateStateAfterPlay(Game game, Player player, Card card, GridSquare square) {

    }

    private void checkGameOverConditions(Game game) {
        // Logic to check if the game should end
    }

    public Player getWinningPlayer(Long gameId){
        Game game = retrieveGameState(gameId);

        if (game.getPlayers().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No players inside of this game");
        }
        return Collections.max(game.getPlayers(),new Comparator<Player>(){
            public int compare(Player player1,Player player2){
                return player1.getScore() - player2.getScore();
            }
        });

    }

    public Player getWinner(Long gameId){
        Game game = retrieveGameState(gameId);
        if (game.getGameStatus() == GameStatus.WINNER){
            return getWinningPlayer(gameId);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Game isn't finished yet");
    }

    public void endGame(Long gameId, Long winnerPlayerId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player winner = playerRepository.findById(winnerPlayerId).orElseThrow();

        game.setGameStatus(GameStatus.WINNER);
        game.setWinner(winner);

        gameRepository.save(game);
    }

}
