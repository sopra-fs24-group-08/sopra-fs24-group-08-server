package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameMatchResultDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class GameService {

    private final Logger logger = LoggerFactory.getLogger(GameService.class);


    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final GridSquareRepository gridSquareRepository;
    private final BoardService boardService;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatService chatService;
    private final BoardRepository boardRepository;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate,
                       ChatRoomRepository chatRoomRepository, GridSquareRepository gridSquareRepository, BoardService boardService, ApplicationEventPublisher eventPublisher, ChatService chatService, BoardRepository boardRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRepository = chatRoomRepository;
        this.gridSquareRepository = gridSquareRepository;
        this.boardService = boardService;
        this.eventPublisher = eventPublisher;
        this.chatService = chatService;
        this.boardRepository = boardRepository;
    }


    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
    }

    public void updateGame(Game game) {
        gameRepository.save(game);
    }//Leaving this in for existing functions of others, should be removed eventually
    //for gameId
    public Game createGame() {
        Game game = new Game();
        Board board = new Board();
        game.setBoard(board);
        gameRepository.save(game);
        gameRepository.flush();
        return game;
    }

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

        Board board = boardService.initializeAndSaveBoard();
        //Not needed boardRepository.save(board);
        game.setBoard(board);
        // Saving game here to ensure it has an ID before linking Players
        gameRepository.saveAndFlush(game); // Use saveAndFlush to immediately commit to the database for ChatRoom

        ChatRoom chatRoom = createAndLinkChatRoom(game);
        game.setChatRoom(chatRoom);

        Player player1 = convertUserToPlayer(user1, game);
        Player player2 = convertUserToPlayer(user2, game);

        game.addPlayer(player1);
        game.addPlayer(player2);
        gameRepository.save(game);
        //Not needed gameRepository.save(game);
        performCoinFlipAndInitializeGame(player1, player2, game);

        game.setGameStatus(GameStatus.ONGOING);
        gameRepository.save(game); // Final save to ensure all changes are committed

        return game;
    }

    private ChatRoom createAndLinkChatRoom(Game game) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setGame(game);
        chatRoomRepository.save(chatRoom); //If we stick with only chat ingame , use cascading and remove this save
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
            System.out.println("Drawing Card " + i + " of " + numberOfCards);
            Card card = boardService.drawCardFromPile(game.getBoard());
            System.out.println(card);
            if (card != null) {
                player.addCardToHand(card);
                card.setPlayer(player);  // Set the player as the owner of the card
                card.setSquare(null);
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
        finishGame(game,winningPlayer,surrenderingPlayer);
    }




    public void finishGame(Game game, Player winner,Player loser) {
        game.setGameStatus(GameStatus.FINISHED);
        System.out.println("Game is finished");
        if (winner != null) {
            game.setWinner(winner);
        }
        if (loser != null) {
            game.setLoser(loser);
        }
        System.out.println("winner is " + winner);
        System.out.println("loser is "+ loser);
        game.setCurrentTurnPlayerId(null);
        // No current turn necessary
        gameRepository.save(game);

        if (winner != null && loser != null) {//game.getLoser().getId() can be changed later, helpful now to spot earlier if something not saving
            GameStateDTO stateForWinner = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, winner.getUser().getId());
            GameStateDTO stateForLoser = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, loser.getUser().getId());

            notifyGameEnd(stateForWinner, winner.getId());
            notifyGameEnd(stateForLoser, loser.getId());
            cleanupGameData(game);

        }
    }

    public void cleanupGameData(Game game) {
        if (game.getGameStatus() == GameStatus.FINISHED) {
            System.out.println("Game Status is set to Finished");
            // Clean chat room first to ensure messages are handled before game deletion
            chatService.cleanupChatRoom(game.getChatRoom());

            // Clean up players next, ensuring that any cards they hold are also cleared
            cleanupPlayers(game.getPlayers());

            // Clean up the board, which includes the card pile as part of its grid squares
            cleanupBoard(game.getBoard());

            System.out.println("Game and all related entities successfully cleaned up");
        }else{
        System.out.println("Something went wrong while cleaning up Game Data"+game.getGameStatus());
        }
    }

    private void cleanupBoard(Board board) {
        boardService.cleanup(board);
    }

    private void cleanupPlayers(List<Player> players) {
        for (Player player : players) {
            cleanupPlayerData(player);
            System.out.println("Player " + player.getId() + " has been cleaned up and removed from the game");
            System.out.println("//////////");
        }
    }

    private void cleanupPlayerData(Player player) {
        revertPlayerToUser(player);  // Reset user state to not in-game
        //playerRepository.delete(player);  // Delete player
        System.out.println("Player data and user state reverted");
    }




    private void revertPlayerToUser(Player player) {
        player.getUser().setInGame(false);
        userRepository.save(player.getUser());
    }

    public GameResultRequest getGameMatchResult(Long gameId) {
        // Fetch the finished game based on gameId
        Game finishedGame = gameRepository.findByGameId(gameId);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Request was done at this moment "+timestamp);
        // Fetch the winner details
        Player winner = finishedGame.getWinner();
        System.out.println(winner+"getWinner");
        User user1 = userRepository.findByid(winner.getId());
        String winnerUsername = user1.getUsername();


        // Fetch the loser details
        Player loser = finishedGame.getLoser();
        User user2 = userRepository.findByid(loser.getId());

        String loserUsername = user2.getUsername();

        // Create a new GameResultRequest object
        GameResultRequest gameResultRequest = new GameResultRequest();

        // Set all the details in gameResultRequest
        gameResultRequest.setGameId(gameId);
        System.out.println("//////");
        gameResultRequest.setWinnerId(winner.getId());
        System.out.println("//////RESULT WANTED/////");

        gameResultRequest.setWinnerUsername(winnerUsername);
        gameResultRequest.setLoserId(loser.getId());
        gameResultRequest.setLoserUsername(loserUsername);
        System.out.println(gameResultRequest.getGameId()+"||"+gameResultRequest.getWinnerId()+"||"+gameResultRequest.getLoserId());

        // Return the filled gameResultRequest
        return gameResultRequest;
    }
/*
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
    }*/


    /**
     * Validates that the move is being made by the correct player and that it is indeed this player's turn.
     *
     * @param gameId the ID of the game
     * @param playerId the ID of the player who should currently be making a move
     * @param moveDTOUserId the ID of the user attempting to make a move
     * @throws NotYourTurnException if it is not the player's turn
     * @throws RuntimeException if the move request is invalid or if the game or player does not exist
     */
    private void validateTurn(Long gameId, Long playerId, Long moveDTOUserId) {
        if (moveDTOUserId == null || !moveDTOUserId.equals(playerId)) {
            throw new RuntimeException("Invalid move request: User ID does not match Player ID");
        }

        Game game = gameRepository.findByGameId(gameId);
        if (game == null) {
            throw new RuntimeException("Game not found for ID: " + gameId);
        }

        if (!game.getCurrentTurnPlayerId().equals(playerId)) {
            throw new NotYourTurnException("Not your turn, current turn is for player ID: " + game.getCurrentTurnPlayerId());
        }
    }


    public void processMove(Long gameId, MoveDTO move) {
        logger.info("Move is being processed for game: {}, player: {}", gameId, move.getPlayerId());
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found for ID: " + gameId));
        Long playerId = move.getPlayerId();
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new PlayerNotFoundException("Player not found for ID: " + playerId));
        validateTurn(gameId, playerId,move.getPlayerId());

        try {
            switch (move.getMoveType()) {
                case DRAW:
                    handleDrawMove(game, player);
                    break;
                case PLACE:
                    handlePlaceMove(game, player, move);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid move type: " + move.getMoveType());
            }
        } catch (Exception ex) {
            logger.error("Error processing move: {}", ex.getMessage(), ex);
            throw ex;  // Re-throwing to maintain the transactional rollback
        }
        switchTurns(game,playerId);
        updateGameState(game);
    }

    private void handleDrawMove(Game game, Player player) {
        Card card = boardService.drawCardFromPile(game.getBoard());
        if (card != null) {
            player.addCardToHand(card);
            card.setPlayer(player);  // Ensure linking
            playerRepository.save(player);
        } else {
            throw new NoCardsLeftException("No more cards left to draw in game: " + game.getGameId());
        }
    }

    private void handlePlaceMove(Game game, Player player, MoveDTO move) {
        Card card = findCardInPlayerHand(player, move.getCardId());
        GridSquare square = boardService.getGridSquareById(game.getBoard(), move.getPosition());
        boardService.placeCardOnSquare(card, square);
        player.removeCardFromHand(card);
        int points = calculatePoints(square, card);
        player.setScore(player.getScore() + points);
        playerRepository.save(player);
    }

    private Card findCardInPlayerHand(Player player, Long cardId) throws CardNotFoundException {
        return player.getHand().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Card not found in player's hand: " + cardId));
    }

    private int calculatePoints(GridSquare square, Card card) {
        if (square.getColor().equalsIgnoreCase(card.getColor())) {
            // Colors match, points are doubled
            return card.getPoints() * 2;
        } else if (square.getColor().equalsIgnoreCase("white")) {
            // White square, 1 point regardless of card color
            return 1;
        } else {
            // Colors do not match and square is not white, 0 points
            return 0;
        }
    }


    private void updateGameState(Game game) {
        logger.debug("Updating game state for game: {}", game.getGameId());

        gameRepository.save(game);
        boardRepository.save(game.getBoard());
        if (boardService.isAllSquaresOccupied(game.getBoard())) {
            System.out.println(game.getBoard());
            checkGameOverConditions(game);
        } else {
            System.out.println(game.getBoard()+"broadcasting PlayerspecificDTO for this board");
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



    private void checkGameOverConditions(Game game) {
        List<Player> players = game.getPlayers();

        // Condition 1: All squares occupied
        boolean allSquaresOccupied = game.getBoard().getGridSquares().stream().allMatch(GridSquare::isOccupied);
        if (allSquaresOccupied) {
            players.stream().max(Comparator.comparingInt(Player::getScore))
                    .ifPresent(winner -> {
                        Player loser = players.stream().filter(p -> !p.equals(winner)).findFirst().orElse(null);
                        finishGame(game, winner, loser);
                    });
            return;  // Exit after handling this condition to avoid overlapping conditions.
        }

        // Condition 2: Any player with 10 or more cards
        players.forEach(player -> {
            if (player.getHand().size() >= 10) {
                Player loser = player;
                Player winner = players.stream().filter(p -> !p.equals(loser)).findFirst().orElse(null);
                finishGame(game, winner, loser);
                return;
            }
        });

        // Condition 3: Card pile empty and all hands empty(adapting cardpilesize to not start with 30)
        if (game.getBoard().getCardPileSquare().getCards().isEmpty() && players.stream().allMatch(p -> p.getHand().isEmpty())) {
            Player winner = findTopPlayerByGameId(game.getGameId());  // Ensure this method handles the need correctly.
            if (winner != null) {
                Player loser = players.stream().filter(p -> !p.equals(winner)).findFirst().orElse(null);
                finishGame(game, winner, loser);
            }
        }
    }
    //refactor later, combine this with broadcastgamestate
    private void notifyGameEnd(GameStateDTO finalGameState, Long playerId) {
        messagingTemplate.convertAndSend("/topic/game/" + finalGameState.getGameId() + "/" + playerId, finalGameState);
    }

    /*public void setGameStatus(Game game, GameStatus newStatus) {
        if (game.getGameStatus() != newStatus) {
            game.setGameStatus(newStatus);
            gameRepository.save(game);
            if (newStatus == GameStatus.FINISHED) {
                eventPublisher.publishEvent(new GameFinishedEvent(this, game));
            }
        }
    }*/


    public Player findTopPlayerByGameId(Long gameId) {
        List<Player> players = playerRepository.findPlayersByGameIdOrderedByScore(gameId);
        return players.isEmpty() ? null : players.get(0);
    }

    public Player getWinner(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (game.getGameStatus() != GameStatus.FINISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Game isn't finished yet");
        }

        Player winner = game.getWinner();
        if (winner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No winner recorded for this game.");
        }

        return winner;
    }
    public long getWinCountByPlayer(Long playerId) {
        return gameRepository.countByWinnerId(playerId);
    }
}
