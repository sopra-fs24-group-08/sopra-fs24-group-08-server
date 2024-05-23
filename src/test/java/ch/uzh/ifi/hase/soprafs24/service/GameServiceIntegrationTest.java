package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.CardNotFoundException;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import ch.uzh.ifi.hase.soprafs24.service.GameService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

import static ch.uzh.ifi.hase.soprafs24.constant.GameStatus.FINISHED;
import static ch.uzh.ifi.hase.soprafs24.constant.GameStatus.ONGOING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private GridSquareRepository gridSquareRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
        cardRepository.deleteAll();
        chatRoomRepository.deleteAll();
        chatMessageRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        //GameService related methods already do teardown.
    }

    @Test
    @Transactional
    void CheckGameOverConditionsGameAlreadyFinished() {
        User user1 = new User();
        user1.setUsername("TestPlayer10");
        user1.setPassword("password1");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("TestPlayer20");
        user2.setPassword("password2");
        userService.createUser(user2);

        Game game = gameService.startGame(user1.getId(), user2.getId());
        game.setGameStatus(GameStatus.FINISHED);
        gameRepository.save(game);
        assertFalse(gameService.checkGameOverConditions(game));
        assertEquals(FINISHED, game.getGameStatus());
    }

    private Game setupGameWithTwoPlayers() {
        User user1 = new User();
        user1.setUsername("TestPlayer1");
        user1.setPassword("password1");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("TestPlayer2");
        user2.setPassword("password2");
        userService.createUser(user2);

        return gameService.startGame(user1.getId(), user2.getId());
    }

    private Game setupFullGame() {
        return setupGameWithTwoPlayers();
    }


    @Test
    public void testCreateAndStartGame_withValidUsers() {
        // Create users using UserService
        User user1 = new User();
        user1.setUsername("JessiJessi");
        user1.setPassword("password1");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("JonJonJonJonJonJonJonJon");
        user2.setPassword("password2");
        userService.createUser(user2);

        // Start game using GameService
        Game game = gameService.startGame(user1.getId(), user2.getId());

        assertNotNull(game);
        assertTrue(game.getPlayers().size() >= 2);
        assertEquals(ONGOING, game.getGameStatus());
    }
    @Test
    public void testGameEndsAfterSurrender() {
        // Create and save users
        User user1 = new User();
        user1.setUsername("Charlie");
        user1.setPassword("password3");
        User savedUser1 = userService.createUser(user1);  // Make sure users are persisted

        User user2 = new User();
        user2.setUsername("Peter");
        user2.setPassword("password4");
        User savedUser2 = userService.createUser(user2);  // Make sure users are persisted

        // Start the game
        Game game = gameService.startGame(savedUser1.getId(), savedUser2.getId());
        assertNotNull(game, "Game should be successfully started");

        // Perform surrender
        gameService.handlePlayerSurrender(game.getGameId(), savedUser1.getId());

        // Fetch the updated game to verify changes
        Game updatedGame = gameRepository.findById(game.getGameId()).orElseThrow(() -> new AssertionError("Game should exist"));

        // Assertions to check if the game has ended correctly
        assertEquals(FINISHED, updatedGame.getGameStatus(), "Game should be marked as finished");
        assertNotNull(updatedGame.getWinnerUser(), "There should be a winner set(User Obj)");
        assertEquals(savedUser2.getId(), updatedGame.getWinnerUser().getId(), "The winner should be user2");

    }


    @Test
    @Transactional
    void ProcessMoveDrawCard() {
        User user1 = new User();
        user1.setUsername("JessiJessi");
        user1.setPassword("password1");
        userService.createUser(user1);
        User user2 = new User();
        user2.setUsername("JonJonJonJonJonJonJonJon");
        user2.setPassword("password2");
        userService.createUser(user2);


        // Start game using GameService

        Game game = gameService.startGame(user1.getId(), user2.getId());

        User chosenUser = userRepository.findByid(game.getCurrentTurnPlayerId());


        MoveDTO moveDTO = new MoveDTO();
        moveDTO.setPlayerId(chosenUser.getId());
        moveDTO.setMoveType(MoveType.DRAW);

        Player ingamePlayer = playerRepository.findByUser(chosenUser);
        int beforeValidDraw = DTOSocketMapper.INSTANCE.getCardPileSize(game);
        Card expecteddrawnCard = game.getBoard().getCardPileSquare().getCards().get(0);

        // Execute the test case
        System.out.println(expecteddrawnCard);
        System.out.println(ingamePlayer.getHand());
        gameService.processMove(game.getGameId(), moveDTO);
        // Validate the results
        System.out.println(ingamePlayer.getHand());

        assertTrue(ingamePlayer.getHand().contains(expecteddrawnCard));
        assertEquals(expecteddrawnCard.getPlayer(), ingamePlayer);
        assertEquals((beforeValidDraw-1), DTOSocketMapper.INSTANCE.getCardPileSize(game));
    }

    @Test
    public void testProcessMovePlaceCard_InvalidCardId() {
        // Setup users and start a game
        User user1 = new User();
        user1.setUsername("TestPlayer1");
        user1.setPassword("password1");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("TestPlayer2");
        user2.setPassword("password2");
        userService.createUser(user2);

        Game game = gameService.startGame(user1.getId(), user2.getId());
        User chosenUser = userRepository.findByid(game.getCurrentTurnPlayerId());
        Player ingamePlayer = playerRepository.findByUser(chosenUser);

        // Prepare a move with an invalid card ID
        MoveDTO moveDTO = new MoveDTO();
        moveDTO.setPlayerId(ingamePlayer.getId());
        moveDTO.setMoveType(MoveType.PLACE);
        moveDTO.setCardId(99999L);
        moveDTO.setPosition(0);

        Exception exception = assertThrows(CardNotFoundException.class, () -> {
            gameService.processMove(game.getGameId(), moveDTO);
        });

        // Verify that the exception message is as expected
        assertTrue(exception.getMessage().contains("Card not found in player's hand: 99999"));
    }



    @Test
    @Transactional
    void testProcessMovePlaceCard() {
        // Setup the users, game, player, and game board
        User user1 = new User();
        user1.setUsername("TestPlayer1");
        user1.setPassword("password1");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("TestPlayer2");
        user2.setPassword("password2");
        userService.createUser(user2);
        Game game = gameService.startGame(user1.getId(), user2.getId());
        User chosenUser = userRepository.findByid(game.getCurrentTurnPlayerId());
        Player ingamePlayer = playerRepository.findByUser(chosenUser);

        Card card = ingamePlayer.getHand().get(0); // Get the first card for simplicity
        card = cardRepository.findById(card.getId())
                .orElseThrow(() -> new RuntimeException("Card not found")); // Re-fetch to ensure it's managed

        int clientSquareIndex = 3; // Example client input, representing the fourth grid square
        GridSquare targetSquare = game.getBoard().getGridSquares().get(clientSquareIndex);
        if (targetSquare.isCardPile() || !targetSquare.getCards().isEmpty()) {
            throw new RuntimeException("Invalid square selected");
        }

        MoveDTO moveDTO = new MoveDTO();
        moveDTO.setPlayerId(ingamePlayer.getId());
        moveDTO.setMoveType(MoveType.PLACE);
        moveDTO.setCardId(card.getId());
        moveDTO.setPosition(clientSquareIndex);


        gameService.processMove(game.getGameId(), moveDTO);

        // Assert that the card is now on the target square
        assertTrue(targetSquare.getCards().contains(card), "Card should be placed on the target square");
        assertNull(card.getPlayer(), "Player should be unlinked from the card if placed on grid");
    }


    @Test
    public void testGameOverConditions_AllSquaresOccupied() {
        Game game = setupFullGame();
        simulateAllSquaresOccupied(game);
        assertTrue(gameService.checkGameOverConditions(game));
    }

    @Test
    public void testGameOverConditions_PlayerHasMoreThan10Cards() {
        Game game = setupFullGame();
        simulatePlayerWithMoreThan10Cards(game);
        assertTrue(gameService.checkGameOverConditions(game));
    }

    @Test
    public void testGameOverConditions_AllCardsDrawnAndAllHandsEmpty() {
        Game game = setupFullGame();
        simulateAllCardsDrawnAndAllHandsEmpty(game);
        assertTrue(gameService.checkGameOverConditions(game));
    }

    private void simulateAllSquaresOccupied(Game game) {
        game.getBoard().getGridSquares().forEach(square -> {
            if (!square.isCardPile()) {
                square.addCard(new Card("red", 1));
            }
        });
    }

    private void simulatePlayerWithMoreThan10Cards(Game game) {
        Player player = game.getPlayers().get(0);
        for (int i = 0; i < 11; i++) {
            player.addCardToHand(new Card("blue", 2));
        }
        playerRepository.save(player);
    }

    private void simulateAllCardsDrawnAndAllHandsEmpty(Game game) {
        game.getBoard().getCardPileSquare().getCards().clear();
        game.getPlayers().forEach(player -> {
            player.getHand().clear();
            playerRepository.save(player);
        });
    }
    @Test
    public void testGetWinCountForUser() {
        User user1 = new User();
        user1.setUsername("NewPlayer");
        user1.setPassword("password1");
        userService.createUser(user1);

        long count = gameService.getWinCountForUser(user1.getId());
        assertEquals(0, count);
    }

    @Test
    public void GetWinningPlayerByOnlyAddingPoints() {
        Game game = setupFullGame();
        Player testplayer = game.getPlayers().get(0);
        testplayer.addScore(10);
        gameRepository.save(game);
        playerRepository.save(testplayer);

        Player player = gameService.findTopPlayerByGameId(game);
        assertEquals(testplayer.getId(), player.getId());
    }

    @Test
    public void GetWinningPlayerByAddingAndRemovingPoints() {
        Game game = setupFullGame();
        Player testplayer = game.getPlayers().get(0);
        testplayer.subScore(10);
        Player cheatingPlayer = game.getPlayers().get(1);
        cheatingPlayer.addScore(1000);
        gameRepository.save(game);
        playerRepository.save(testplayer);
        playerRepository.save(cheatingPlayer);


        Player player = gameService.findTopPlayerByGameId(game);
        assertNotEquals(testplayer.getId(), player.getId());
        assertTrue(player.getScore() > testplayer.getScore());
        assertEquals(cheatingPlayer, player);
    }



}