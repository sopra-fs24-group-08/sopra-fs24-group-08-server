package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.GameNotFinishedException;
import ch.uzh.ifi.hase.soprafs24.exceptions.PlayerNotFoundException;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
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
    private final GridSquareRepository gridSquareRepository;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate,
                       BoardRepository boardRepository, ChatRoomRepository chatRoomRepository, GridSquareRepository gridSquareRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.boardRepository = boardRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.gridSquareRepository = gridSquareRepository;
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
    public Game startGame(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("About to create new game");

        Game game = initializeNewGame(user1, user2);

        System.out.println("Game and chat room initialized");
        return game;
    }

    private Game initializeNewGame(User user1, User user2) {
        System.out.println("About to create new game");
        Game game = new Game();
        game.setGameStatus(GameStatus.STARTING);


        Board board = initializeBoard();
        boardRepository.save(board);
        game.setBoard(board);

        // Saving game here to ensure it has an ID before linking Players
        gameRepository.saveAndFlush(game); // Use saveAndFlush to immediately commit to the database

        ChatRoom chatRoom = createAndLinkChatRoom(game);
        game.setChatRoom(chatRoom);

        Player player1 = convertUserToPlayer(user1, game);
        Player player2 = convertUserToPlayer(user2, game);

        game.addPlayer(player1);
        game.addPlayer(player2);

        gameRepository.save(game);

        game.setGameStatus(GameStatus.COINFLIP);
        performCoinFlipAndInitializeGame(player1, player2, game);

        game.setGameStatus(GameStatus.ONGOING);
        gameRepository.save(game); // Final save to ensure all changes are committed

        return game;
    }

    private Board initializeBoard() {
       System.out.println("About to create board");
       Board board = new Board();
       board.initializeBoard();
       return board;
   }
    private ChatRoom createAndLinkChatRoom(Game game) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setGame(game);
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }



    private Player convertUserToPlayer(User user, Game game) {
        System.out.println("Converting User to player");
        user.setInGame(true);
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        userRepository.save(user);
        playerRepository.save(player);
        return player;
    }

    private void performCoinFlipAndInitializeGame(Player player1, Player player2, Game game) {
        System.out.println("About to do a coin flip for the game with id "+game.getGameId());
        boolean isFirstPlayerPlayer1 = Math.random() < 0.5;
        Player firstPlayer = isFirstPlayerPlayer1 ? player1 : player2;

        game.setCurrentTurnPlayerId(firstPlayer.getId());
        System.out.println("Coinflipped; winner is player with ID: " + firstPlayer.getId());

        dealInitialCards(firstPlayer, isFirstPlayerPlayer1 ? player2 : player1, game);
    }

    private void dealInitialCards(Player firstPlayer, Player secondPlayer, Game game) {
        game.setCurrentTurnPlayerId(firstPlayer.getId());
        drawCardsForPlayer(firstPlayer, 2, game);
        drawCardsForPlayer(secondPlayer, 3, game);
    }

    private void drawCardsForPlayer(Player player, int numberOfCards, Game game) {
        for (int i = 0; i < numberOfCards; i++) {
            Card card = game.getBoard().drawCardFromPile();
            if (card != null) {
                player.addCardToHand(card);
            } else {
                //  handle the case where there are no more cards to draw
                System.out.println("No more cards to draw from the pile.");
                break;
            }
        }
        playerRepository.save(player);
    }

    //If a player clicks on EXIT/SURR then we should have a proper way to apply the changes.
    public void handlePlayerSurrender(Long gameId, Long surrenderingPlayerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getGameStatus() == GameStatus.FINISHED) {
            throw new IllegalStateException("Game is already finished");
        }
        // Identify the surrendering player and the opponent

        Player surrenderingPlayer = game.getPlayers().stream()
                .filter(p -> p.getId().equals(surrenderingPlayerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Surrendering Player not found"));

        Player winningPlayer = game.getPlayers().stream()
                .filter(p -> !p.getId().equals(surrenderingPlayerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Automatically winning Player not found"));
        Long winningPlayerId = winningPlayer.getId();
        // Update the game state
        game.setWinner(winningPlayer);
        game.setLoser(surrenderingPlayer);
        game.setGameStatus(GameStatus.FINISHED);
        game.setCurrentTurnPlayerId(null); // No current turn necessary
        // perhaps if we decide to store everything on external DB
        // we could also start storing more data and not delete everything all the time unless necessary.
        revertPlayerToUser(surrenderingPlayer);
        revertPlayerToUser(winningPlayer);
        // Save changes
        gameRepository.save(game);
        GameStateDTO stateForLoser = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, surrenderingPlayerId);
        GameStateDTO stateForWinner = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, winningPlayerId);

        notifyGameEnd(stateForLoser,surrenderingPlayerId);
        notifyGameEnd(stateForWinner, winningPlayerId);
        playerRepository.delete(surrenderingPlayer);
        playerRepository.deleteById(winningPlayer.getId());
        //notifyWinner(winningPlayer);
        //notifyLoser(surrenderingPlayer);
    }

    private void notifyGameEnd(GameStateDTO finalGameState,Long playerId) {
        messagingTemplate.convertAndSend("/topic/game/" + finalGameState.getGameId()+"/"+playerId, finalGameState);
    }
    // If we delete
    private void revertPlayerToUser(Player player) {
        player.getUser().setInGame(false);
        userRepository.save(player.getUser());

    }

    /*private void notifyWinner(Player winner) {
        messagingTemplate.convertAndSend("/topic/game/" + winner.getGame().getId(),
                new GameNotification("Game Finished", "You won!", GameStatus.FINISHED, winner.getId()));
    }
    //Combine into one function alter notifyPlayers
    private void notifyLoser(Player loser) {
        messagingTemplate.convertAndSend("/topic/game/" + loser.getGame().getId(),
                new GameNotification("Game Finished", "You lost.", GameStatus.FINISHED, loser.getId()));
    }*/


    public GameResultRequest verifyResult(Long gameId,String playerName, Long userId){
        Game game = gameRepository.findByGameId(gameId);
        if (game == null) {
            throw new EntityNotFoundException("Game not found with ID: " + gameId);
        }

        Player player = game.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found in the specified game"));

        if (game.getGameStatus() != GameStatus.FINISHED) {
            throw new GameNotFinishedException("The game has not finished yet");
        }
        boolean participated = true;
        //Normally there shouldn't be a need to check if the given username is the actual username, once we have refined everything I will adjust this.
        //Right now it's more for debugging purposes to prevent from errors
        // that stem from user editing username and then sending old one to check.
        System.out.println(player.getUser().getUsername().equals(playerName));
        boolean isWinner = game.getWinner() != null && game.getWinner().getId().equals(player.getId());

        return new GameResultRequest(participated,isWinner, player.getUser().getUsername());



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


    public void processMove(Long gameId, MoveDTO move, Long playerId) {
        System.out.println("Move is being processed");
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        Board board = game.getBoard();

        switch (move.getMoveType()) {
            case DRAW:
                if (board != null) {
                    Card drawnCard = board.drawCardFromPile();  // Updated to use the new draw method
                    if (drawnCard != null) {
                        player.addCardToHand(drawnCard);
                    } else {
                        throw new RuntimeException("No more cards left to draw");
                    }
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
                    //player.removeCardFromHand(card);
                } else {
                    throw new RuntimeException("Square is occupied or does not exist");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid move type");
        }
        switchTurns(game,playerId);
        playerRepository.save(player);  // Ensure player changes are persisted
        gameRepository.save(game);//// Save changes to the game
        if (gridSquareRepository.countByBoardIdAndIsOccupiedFalse(game.getBoard().getId())==0) {
            checkGameOverConditions(game);
        }else{
        broadcastGameState(game);
        }
    }

    private void broadcastGameState(Game game) {
        game.getPlayers().forEach(player -> {
            GameStateDTO gameState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game,player.getId());
            messagingTemplate.convertAndSend("/topic/game/"+game.getGameId()+"/"+player.getUser().getId().toString(),gameState);
        }); //`/topic/game/${gameId}/${currUser.id}`
    }

    //Give it the id of the currentPlayer(do validation before)
    private void switchTurns(Game game, Long currentPlayerId) {
        game.getPlayers().forEach(p -> {
            if (!p.getId().equals(currentPlayerId)){
                game.setCurrentTurnPlayerId(p.getId());
            }
        });
    }


    @Transactional(readOnly = true)
    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(
                () -> new EntityNotFoundException("Game not found with ID: " + gameId));
    }

    public Game retrieveGameState(Long gameId){
        return gameRepository.findById(gameId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Game was not found"));
    }


    private boolean validateMove(MoveDTO move) {
        // Your validation logic here
        return true; // Simplified for example purposes
    }



   // @Transactional(rollbackFor = {RuntimeException.class, DataIntegrityViolationException.class})

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
        if (game.getGameStatus() == GameStatus.FINISHED){
            return getWinningPlayer(gameId);
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Game isn't finished yet");
    }

    public void endGame(Long gameId, Long winnerPlayerId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player winner = playerRepository.findById(winnerPlayerId).orElseThrow();

        game.setGameStatus(GameStatus.FINISHED);
        game.setWinner(winner);

        gameRepository.save(game);
    }

}
