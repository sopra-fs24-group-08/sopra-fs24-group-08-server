package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameCleanupEvent;
import ch.uzh.ifi.hase.soprafs24.EventListener.GameEndEvent;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class GameService {

    private final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final Object gameLock = new Object();  // A lock for game finishing operations





    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
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
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalStateException("Game not found with ID: " + gameId));

        synchronized (gameLock) {
            if (game.getGameStatus() != GameStatus.ONGOING) {
                throw new IllegalStateException("Game is not in an ongoing state.");
            }

            Player surrenderingPlayer = game.getPlayers().stream()
                    .filter(p -> p.getId().equals(surrenderingPlayerId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Surrendering Player not found or not part of the game"));

            Player winningPlayer = game.getPlayers().stream()
                    .filter(p -> !p.getId().equals(surrenderingPlayerId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No other player found; automatic win not possible"));

            finishGame(game, winningPlayer, surrenderingPlayer);
        }
    }



    public void finishGame(Game game, Player winner, Player loser) {
        synchronized (gameLock) {  // Synchronize on game object for more specific lock control
            if (game.getGameStatus() == GameStatus.FINISHED) {
                logger.info("Attempt to finish an already finished game (Game ID: {}).", game.getGameId());
                return;  // Prevent multiple endings if the game is already marked as finished.
            }

            // Set winner, loser, and game status
            game.setWinner(winner);
            game.setLoser(loser);
            game.setCurrentTurnPlayerId(null);
            game.setGameStatus(GameStatus.FINISHED);
            gameRepository.save(game);

            logger.info("Game (ID: {}) finished. Winner: {}, Loser: {}", game.getGameId(), winner.getId(), loser.getId());

            // Asynchronously publish game cleanup event first to handle all cleanup tasks
            eventPublisher.publishEvent(new GameCleanupEvent(this, game));
            // The game end event will now be published after cleanup in the GameCleanupListener
        }
    }

    private void notifyGameEnd(GameStateDTO finalGameState, Long playerId) {
        messagingTemplate.convertAndSend("/topic/game/" + finalGameState.getGameId() + "/" + playerId, finalGameState);
    }



    private void revertPlayerToUser(Player player) {
        player.getUser().setInGame(false);
        userRepository.save(player.getUser());
    }


    public GameResultRequest getGameMatchResult(Long gameId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Request was done at this moment " + timestamp);

        // Fetch the finished game based on gameId
        Game finishedGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found with ID: " + gameId));

        // Ensure the game has winners and losers set
        if (finishedGame.getWinner() == null || finishedGame.getLoser() == null) {
            throw new IllegalStateException("Game data is incomplete. Winner or loser is not set.");
        }

        // Fetch the winner details
        Player winner = finishedGame.getWinner();
        User user1 = userRepository.findById(winner.getId())
                .orElseThrow(() -> new IllegalStateException("User not found for winner with ID: " + winner.getId()));
        String winnerUsername = user1.getUsername();

        // Fetch the loser details
        Player loser = finishedGame.getLoser();
        User user2 = userRepository.findById(loser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found for loser with ID: " + loser.getId()));
        String loserUsername = user2.getUsername();

        // Logging for debugging
        System.out.println("Winner: " + winnerUsername + "getWinner");
        System.out.println("Loser: " + loserUsername + "getLoser");
        System.out.println("//////");

        // Create and fill the GameResultRequest object
        GameResultRequest gameResultRequest = new GameResultRequest();
        gameResultRequest.setGameId(finishedGame.getGameId());
        gameResultRequest.setWinnerId(winner.getId());
        gameResultRequest.setWinnerUsername(winnerUsername);
        gameResultRequest.setLoserId(loser.getId());
        gameResultRequest.setLoserUsername(loserUsername);

        // Log the desired output format
        System.out.println(gameResultRequest.getGameId() + "||" + gameResultRequest.getWinnerId() + "||" + gameResultRequest.getLoserId());
        System.out.println("Game result has been returned1: " + gameResultRequest);

        // Return the filled gameResultRequest
        return gameResultRequest;
    }

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
        updateGameState(game);


    }
    //Give it the id of the currentPlayer(do validation before)
    private void switchTurns(Game game, Long currentPlayerId) {
        game.getPlayers().forEach(p -> {
            if (!p.getId().equals(currentPlayerId)){
                game.setCurrentTurnPlayerId(p.getId());
            }
        });

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

        // Update the turn and save the state
        switchTurns(game, game.getCurrentTurnPlayerId());
        gameRepository.save(game);
        boardRepository.save(game.getBoard());

        // Check if the game has ended and handle accordingly
        if (!checkGameOverConditions(game)) {
            // If the game is not over, broadcast the updated game state
            broadcastGameState(game);
        }
        // If condition is true then broadcast would be managed over GameEventService.handleGameEnd but only after cleanup has been done.
    }



    private void broadcastGameState(Game game) {
        game.getPlayers().forEach(player -> {
            GameStateDTO gameState = DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game,player.getId());
            messagingTemplate.convertAndSend("/topic/game/"+game.getGameId()+"/"+player.getUser().getId().toString(),gameState);

        }); //`/topic/game/${gameId}/${currUser.id}`
    }

    public boolean checkGameOverConditions(Game game) {
        synchronized (gameLock) {
            if (game.getGameStatus() == GameStatus.FINISHED) {
                logger.info("Game ID: {} is already finished.", game.getGameId());
                return false;
            }

            if (areGameOverConditionsMet(game)) {
                Player winner = determineWinner(game);
                Player loser = determineLoser(game, winner);

                if (winner != null && loser != null) {
                    finishGame(game, winner, loser);
                    return true;
                } else {
                    logger.error("Could not determine winner and loser for game ID: {}", game.getGameId());
                    return false;
                }
            }
            return false;
        }
    }

    private boolean areGameOverConditionsMet(Game game) {
        List<Player> players = game.getPlayers();

        // Condition 1: All squares occupied
        if (boardService.isAllSquaresOccupied(game.getBoard())) {
            return true;
        }

        // Condition 2: Any player has more than 10 cards
        if (players.stream().anyMatch(p -> p.getHand().size() >= 10)) {
            return true;
        }

        // Condition 3: Card pile and all hands are empty
        if (game.getBoard().getCardPileSquare().getCards().isEmpty() && players.stream().allMatch(p -> p.getHand().isEmpty())) {
            return true;
        }

        return false;
    }

    private Player determineWinner(Game game) {
        List<Player> players = game.getPlayers();

        // Determine winner based on all squares occupied
        if (boardService.isAllSquaresOccupied(game.getBoard())) {
            return players.stream().max(Comparator.comparingInt(Player::getScore)).orElse(null);
        }

        // Determine winner based on least cards if any player has more than 10 cards
        if (players.stream().anyMatch(p -> p.getHand().size() >= 10)) {
            return players.stream().filter(p -> p.getHand().size() < 10).findFirst().orElse(null);
        }

        // Determine winner when the card pile is empty and all hands are empty
        if (game.getBoard().getCardPileSquare().getCards().isEmpty() && players.stream().allMatch(p -> p.getHand().isEmpty())) {
            return findTopPlayerByGameId(game.getGameId());
        }

        return null;
    }

    private Player determineLoser(Game game, Player winner) {
        List<Player> players = game.getPlayers();

        // Exclude the winner and find the first other player in the game
        return players.stream()
                .filter(p -> !p.equals(winner))
                .findFirst()
                .orElse(null);
    }


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
