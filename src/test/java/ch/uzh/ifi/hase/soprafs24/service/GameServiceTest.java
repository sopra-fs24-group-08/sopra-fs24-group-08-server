//package ch.uzh.ifi.hase.soprafs24.service;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
//import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
//import ch.uzh.ifi.hase.soprafs24.entity.*;
//import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
//import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
//import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
//import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.mockito.MockitoAnnotations;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.util.Optional;
//
//@SpringBootTest
//public class GameServiceTest {
//
//    @Mock
//    private GameRepository gameRepository;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private PlayerRepository playerRepository;
//    @Mock
//    private BoardService boardService;
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//    @InjectMocks
//    private GameService gameService;
//
//    private User user1, user2;
//    private Player player1, player2;
//    private Game game;
//    private Board board;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//        user1 = new User();
//        user1.setId(1L);
//        user2 = new User();
//        user2.setId(2L);
//
//        player1 = new Player();
//        player1.setUser(user1);
//        player1.setId(1L);
//
//        player2 = new Player();
//        player2.setUser(user2);
//        player2.setId(2L);
//
//        game = new Game();
//        game.setGameId(1L);
//        game.addPlayer(player1);
//        game.addPlayer(player2);
//
//        board = new Board();
//        game.setBoard(board);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
//        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
//        when(gameRepository.save(any(Game.class))).thenReturn(game);
//        when(boardService.initializeAndSaveBoard()).thenReturn(board);
//    }
//
//    @Test
//    public void testStartGame() {
//        when(gameRepository.save(any(Game.class))).thenReturn(game);
//
//        Game startedGame = gameService.startGame(1L, 2L);
//
//        assertNotNull(startedGame);
//        assertEquals(GameStatus.ONGOING, startedGame.getGameStatus());
//        verify(userRepository, times(2)).findById(anyLong());
//        verify(boardService, times(1)).initializeAndSaveBoard();
//    }
//
//    @Test
//    public void testHandleDrawMove() {
//        Card card = new Card("red", 5);
//        when(boardService.drawCardFromPile(board)).thenReturn(card);
//        MoveDTO expectedMove = new MoveDTO();
//        expectedMove.setPlayerId(1L);
//        expectedMove.setCardId(2L);
//        expectedMove.setPosition(5);
//        expectedMove.setMoveType(MoveType.DRAW);
//        gameService.processMove(game.getGameId(), expectedMove);
//
//        verify(playerRepository, times(1)).save(player1);
//        assertEquals(1, player1.getHand().size());
//    }
//
//    @Test
//    public void testCheckGameOverConditions_AllSquaresOccupied() {
//        when(boardService.isAllSquaresOccupied(board)).thenReturn(true);
//
//        assertTrue(gameService.checkGameOverConditions(game));
//        verify(gameRepository, times(1)).save(game);
//    }
//
//    @Test
//    public void testHandlePlayerSurrender() {
//        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
//        game.setGameStatus(GameStatus.ONGOING);
//
//        gameService.handlePlayerSurrender(game.getGameId(), player1.getId());
//
//        assertEquals(GameStatus.FINISHED, game.getGameStatus());
//        assertNotNull(game.getWinner());
//        assertEquals(player2.getId(), game.getWinner().getId());
//    }
//}
