package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Game createGame() {
        Game game = new Game();
        Board board = new Board();
        game.setBoard(board);
        gameRepository.save(game);
        gameRepository.flush();
        return game;
    }

    //Leaving in encase it's used for Polling or if we want to add different beh. to friendly games
    public Game startFriendsGame(Long gameId,Long userId1, Long userId2){
        return startGame(gameId,userId1,userId2);
    }

    public Game startGame(Long gameId, Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Player player1 = new Player(user1, game);
        Player player2 = new Player(user2, game);

        boolean firstPlayerStarts = new Random().nextBoolean();

        if (firstPlayerStarts) {
            dealInitialCards(game.getBoard(), player1, player2, true);
            game.setCurrentTurnPlayerId(player1.getId());
        } else {
            dealInitialCards(game.getBoard(), player1, player2, false);
            game.setCurrentTurnPlayerId(player2.getId());
        }


        playerRepository.saveAll(Arrays.asList(player1, player2));

        game.setPlayers(Arrays.asList(player1, player2));
        game.setGameStatus(GameStatus.ONGOING);
        gameRepository.save(game);
        broadcastGameState(gameId, game);
        return game;
    }

    private void dealInitialCards(Board board, Player player1, Player player2, boolean firstPlayerStarts) {
        Player firstPlayer = firstPlayerStarts ? player1 : player2;
        Player secondPlayer = firstPlayerStarts ? player2 : player1;

        giveCards(firstPlayer, board, 2); // First player gets 2 cards
        giveCards(secondPlayer, board, 3); // Second player gets 3 cards
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
        System.out.println("Move is being process");
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
        System.out.println(move+ "trying to placeCard");
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

    public void processMove(Long gameId, MoveDTO move, Long playerId) {
        /*Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        validateMove(game, move, player);

        if (move.getMoveType() == MoveType.PLACE) {
            placeCard(game, move, player);
        } else if (move.getMoveType() == MoveType.DRAW) {
            drawCard(game, move, player);
        }*/
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        gameRepository.save(game);
        broadcastGameState(gameId, game);  // Broadcast updated game state after move
    }

    private boolean validateMove(MoveDTO move) {
        // Your validation logic here
        return true; // Simplified for example purposes
    }


    private void broadcastGameState(Long gameId, Game game) {
        messagingTemplate.convertAndSend("/topic/gamestate/" + gameId, game);
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
