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
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, BoardRepository boardRepository, ChatRoomRepository chatRoomRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.boardRepository = boardRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
    }

    public void updateGame(Game game) {
        gameRepository.save(game);
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

    public Long coinFlip(Long playerId1,Long playerId2 ){
        boolean firstPlayerStarts = new Random().nextBoolean();
        return firstPlayerStarts ? playerId1 : playerId2;
    }

    /*public Game startGame(Long userId1, Long userId2) {
        User user1 = userRepository.findByid(userId1);
        User user2 = userRepository.findByid(userId2);
        Game game = createGame();

        //Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        initializeMatchedFriends(game,user1,user2);
        performCoinFlip(game);

        gameRepository.save(game);
        return game;
    }*/
    //new
    public Game startGame(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("about to create new game");
        Game game = new Game();

        System.out.println("about to create board");
        Board board = new Board();
        board.initializeBoard();
        game.setBoard(board);

        // Create the chat room here, after initializing the board and before adding players
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setGame(game);  // Link the chat room to the game
        chatRoomRepository.save(chatRoom);  // Save the new chat room
        game.setChatRoom(chatRoom);  // Set the chat room for the game

        System.out.println("Converting User to player");
        Player player1 = convertUserToPlayer(user1, game);
        Player player2 = convertUserToPlayer(user2, game);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.setGameStatus(GameStatus.COINFLIP);

        performCoinFlipAndInitializeGame(player1, player2, game);

        gameRepository.save(game);  // Save the game after all initial setups

        System.out.println("Game and chat room initialized");
        return game;
    }

    //new

    private Player convertUserToPlayer(User user, Game game) {
        user.setInGame(true);   //Player can only be player if user counterpart is actually ingame otherwise it shouldnt' be allowed.
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        // No need to set player ID as it uses @MapsId with User ID
        userRepository.save(user);
        playerRepository.save(player);
        return player;
    }

    //new
    private void performCoinFlipAndInitializeGame(Player player1, Player player2, Game game) {
        System.out.println("about to play coin flip" + player1.getId() + " " + player2.getId()+ player1);
        boolean isFirstPlayerPlayer1 = Math.random() < 0.5;
        Player firstPlayer = isFirstPlayerPlayer1 ? player1 : player2;

        // Ensure you are setting the current turn player ID
        game.setCurrentTurnPlayerId(firstPlayer.getId());

        // Make sure you persist any changes to the game entity

        gameRepository.save(game);

        // Proceed to deal initial cards
        dealInitialCards(firstPlayer, player1 == firstPlayer ? player2 : player1, game);
    }
    //new
    private void dealInitialCards(Player firstPlayer, Player secondPlayer, Game game) {
        // Assuming each player draws three cards as an example
        game.getBoard().drawCard(firstPlayer);
        game.getBoard().drawCard(firstPlayer);

        game.getBoard().drawCard(secondPlayer);
        game.getBoard().drawCard(secondPlayer);
        game.getBoard().drawCard(secondPlayer);

        // Save the players to persist card changes
        playerRepository.save(firstPlayer);
        playerRepository.save(secondPlayer);

    }




    private void initializeMatchedFriends(Game game, User user1, User user2) {
        Player player1 = new Player();
        player1.setUser(user1);
        player1.setGame(game);

        Player player2 = new Player();
        player2.setUser(user2);
        player2.setGame(game);
        playerRepository.saveAll(Arrays.asList(player1, player2));
        game.setPlayers(Arrays.asList(player1, player2));
        gameRepository.save(game);
    }


    private Game initializeMatchedUsers(Game game, User user1, User user2) {
        Player player1 = new Player();
        player1.setUser(user1);
        player1.setGame(game);

        Player player2 = new Player();
        player2.setUser(user2);
        player2.setGame(game);
        playerRepository.saveAll(Arrays.asList(player1, player2));
        game.setPlayers(Arrays.asList(player1, player2));
        game.setGameStatus(GameStatus.ONGOING);
        gameRepository.save(game);
        return game;

    }

    public boolean verifyTurnDecision(Long gameId,Long userId,Long user2Id) {
        Game flipgame = gameRepository.findByGameId(gameId);
        //check if they are ingame
        return (Objects.equals(flipgame.getGameStatus().toString(), "COINFLIP"));
    }

    public Game startMatchedGame(Long gameId,Long userToStart, Long user2){
        User userW = userRepository.findByid(userToStart);
        User userL = userRepository.findByid(user2);
        Game game = gameRepository.findByGameId(gameId);
        return initializeMatchedUsers(game,userW, userL);
    }

    private void performCoinFlip(Game game) {
        boolean firstPlayerStarts = new Random().nextBoolean();
        Player startingPlayer = firstPlayerStarts ? game.getPlayers().get(0) : game.getPlayers().get(1);
        Player secondPlayer = firstPlayerStarts ? game.getPlayers().get(1) : game.getPlayers().get(0);

        // Decide who starts and send initial game state
        game.setGameStatus(GameStatus.COINFLIP);
        game.setCurrentTurnPlayerId(startingPlayer.getId());
        gameRepository.save(game);
        sendInitialGameState(game);
    }



    private void sendInitialGameState(Game game) {
        GameStateDTO gameState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, game.getCurrentTurnPlayerId());
        game.getPlayers().forEach(player -> {
            GameStateDTO playerSpecificState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, player.getUser().getId());
            messagingTemplate.convertAndSendToUser(player.getUser().getId().toString(), "/queue/game-state", playerSpecificState);
        });
    }



    public List<Player> getPlayersbygameId(Long gameId) {
        return gameRepository.findPlayersByGameId(gameId);

    }
  /*  public GameState getGameStateForPlayer(Long gameId, String username) {
        GameState fullGameState = gameRepository.findGameStateByGameId(gameId);
        // Clone or modify the GameState to create a player-specific view
        GameState playerGameState = new GameState();
        playerGameState.setCommonElements(fullGameState.getCommonElements());  // e.g., board state, scores

        // Filter out only the cards that belong to the player
        playerGameState.setPlayerHand(fullGameState.getHands().get(username));

        return playerGameState;
    }*/


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
