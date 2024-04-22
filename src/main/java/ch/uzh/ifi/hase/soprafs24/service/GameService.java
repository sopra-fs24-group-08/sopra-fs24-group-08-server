package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
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
    private final BoardRepository boardRepository;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, BoardRepository boardRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.boardRepository = boardRepository;
    }
    //for gameId
    public Game createGame() {
        Game game = new Game();
        Board board = new Board();
        game.setBoard(board);
        gameRepository.save(game);
        gameRepository.flush();
        return game;
    }

    //Leaving in encase it's used for Polling or if we want to add different beh. to friendly games
    public Game startFriendsGame(Long userId1, Long userId2){
        return startGame(userId1,userId2);
    }

    public Game startGame(Long userId1, Long userId2) {
        User user1 = userRepository.findByid(userId1);
        User user2 = userRepository.findByid(userId2);
        Game game = createGame();

        //Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        initializePlayers(game,user1,user2);
        performCoinFlip(game);

        gameRepository.save(game);
        return game;
    }

    private void initializePlayers(Game game, User user1, User user2) {
        Player player1 = new Player();
        player1.setUser(user1);
        player1.setGame(game);

        Player player2 = new Player();
        player2.setUser(user2);
        player2.setGame(game);
        playerRepository.saveAll(Arrays.asList(player1, player2));
        game.setPlayers(Arrays.asList(player1, player2));
        game.setGameStatus(GameStatus.ONGOING);
    }

    private void performCoinFlip(Game game) {
        boolean firstPlayerStarts = new Random().nextBoolean();
        Player startingPlayer = firstPlayerStarts ? game.getPlayers().get(0) : game.getPlayers().get(1);
        Player secondPlayer = firstPlayerStarts ? game.getPlayers().get(1) : game.getPlayers().get(0);

        // Decide who starts and send initial game state
        game.setCurrentTurnPlayerId(startingPlayer.getId());
        sendInitialGameState(game);
    }

    private void sendInitialGameState(Game game) {
        GameStateDTO gameState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, game.getCurrentTurnPlayerId());
        game.getPlayers().forEach(player -> {
            GameStateDTO playerSpecificState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, player.getUser().getId());
            messagingTemplate.convertAndSendToUser(player.getUser().getId().toString(), "/queue/game-state", playerSpecificState);
        });
    }

    private void dealInitialCards(Board board, Player firstPlayer, Player secondPlayer) {
        board.drawCard(firstPlayer);
        board.drawCard(firstPlayer);
        board.drawCard(secondPlayer);
        board.drawCard(secondPlayer);
        board.drawCard(secondPlayer);


        playerRepository.saveAll(Arrays.asList(firstPlayer, secondPlayer));
    }


    public Game processMove(Long gameId, MoveDTO move,Long playerId) {
        System.out.println("Move is being processed");
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        Board board = game.getBoard();
        switch (move.getMoveType()) {
            case DRAW:
                if (board != null) {
                    board.drawCard(player);
                }
                break;
            case PLACE:
                Card card = player.getHand().stream()
                        .filter(c -> c.getId().equals(move.getCardId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Card not found in player's hand"));

                GridSquare square = board.getGridSquares().get(move.getPosition());
                if (square != null && !square.isOccupied()) {
                    player.placeCard(square, card);
                }
                else {
                    throw new RuntimeException("Square is occupied or does not exist");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid move type");
        }
        gameRepository.save(game);
        return game;
    }



    /*public void placeCard(Long gameId, MoveDTO move) {
        System.out.println(move+ "trying to placeCard");
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Player player = playerRepository.findById(move.getPlayerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        Card card = player.getCardFromHand(move.getCardId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        Board board = game.getBoard();
        int position = move.getPosition();
        String squareColor = board.getSquareColor(position);

        if (!board.getGridSquares().isOccupied(position)) {
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
  }*/

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



    private boolean validateMove(MoveDTO move) {
        // Your validation logic here
        return true; // Simplified for example purposes
    }


    private void broadcastGameState(Game game) {
        game.getPlayers().forEach(player -> {
            GameStateDTO gameState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game,player.getId());
            messagingTemplate.convertAndSendToUser(player.getUser().getId().toString(), "/queue/game", gameState);
        });
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
