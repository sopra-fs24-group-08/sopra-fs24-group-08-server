package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(GameService.class);


    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final GridSquareRepository gridSquareRepository;
    private final BoardService boardService;

    @Autowired
    public GameService(GameRepository gameRepository, UserRepository userRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate,
                        ChatRoomRepository chatRoomRepository, GridSquareRepository gridSquareRepository,BoardService boardService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRepository = chatRoomRepository;
        this.gridSquareRepository = gridSquareRepository;
        this.boardService = boardService;
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
        Long winningPlayerId = winningPlayer.getId();
        // Update the game state
        endGame(game,winningPlayer,surrenderingPlayer);
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
        if (boardService.isAllSquaresOccupied(game.getBoard())) {
            checkGameOverConditions(game);
        } else {
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


    public void endGame(Game endingGame,Player winningPlayer,Player losingPlayer) {
        endingGame.setWinner(winningPlayer);
        endingGame.setLoser(losingPlayer); //surrendering -> auto Lose
        endingGame.setGameStatus(GameStatus.FINISHED);
        endingGame.setCurrentTurnPlayerId(null); // No current turn necessary
    }


    private void checkGameOverConditions(Game game) {
        List<Player> players = game.getPlayers();

        boolean allSquaresOccupied = game.getBoard().getGridSquares().stream().allMatch(GridSquare::isOccupied);
        if (allSquaresOccupied) {
            Player winner = players.stream().max(Comparator.comparingInt(Player::getScore)).orElse(null);
            if (winner != null) {
                game.setWinner(winner);
                notifyPlayers(game.getGameId(), "Game over: Player " + winner.getId() + " wins with the highest score.");
            }
            return;
        }
        // Check if one player has 10 cards in hand
        players.forEach(player -> {
            if (player.getHand().size() >= 10) {
                game.setLoser(player);
                notifyPlayers(game.getGameId(), "Game over: Player " + player.getId() + " loses with 10 or more cards.");
            }
        });

        // Check if the card pile and players' hands are empty
        if (game.getBoard().getCardPileSquare().getCards().isEmpty() &&
                players.stream().allMatch(p -> p.getHand().isEmpty())) {
            Player winner = players.stream().max(Comparator.comparingInt(p -> p.getHand().size())).orElse(null);
            if (winner != null) {
                game.setWinner(winner);
                notifyPlayers(game.getGameId(), "Game over: Player " + winner.getId() + " wins with the most cards.");
            }
        }
    }

    private void notifyPlayers(Long gameId, String message) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game was not found"));
        game.getPlayers().forEach(player ->
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/" + player.getId(), message));
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
