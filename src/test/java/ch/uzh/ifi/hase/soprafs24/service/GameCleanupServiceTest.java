package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.ArrayList;

public class GameCleanupServiceTest {

    @Mock private BoardService boardService;
    @Mock private UserRepository userRepository;
    @Mock private ChatService chatService;
    @Mock private PlayerRepository playerRepository;
    @Mock private GameRepository gameRepository;

    @InjectMocks private GameCleanupService gameCleanupService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCleanupGameDataSuccessfully() {
        // Prepare the game and its components
        Game game = new Game();
        ChatRoom chatRoom = new ChatRoom();
        Board board = new Board();
        Player winner = new Player();
        Player loser = new Player();
        winner.setUser(new User());
        loser.setUser(new User());
        game.setChatRoom(chatRoom);
        game.setBoard(board);
        game.setPlayers(new ArrayList<>(Arrays.asList(winner, loser)));
        game.setWinner(winner);
        game.setLoser(loser);

        // Mocking behaviors
        doNothing().when(chatService).cleanupChatRoom(chatRoom);
        doNothing().when(boardService).cleanup(board);
        doNothing().when(playerRepository).delete(any(Player.class));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Execute the cleanup method
        gameCleanupService.cleanupGameData(game);

        // Verifications
        verify(chatService).cleanupChatRoom(chatRoom);
        verify(boardService).cleanup(board);
        verify(playerRepository, times(2)).delete(any(Player.class));
        verify(gameRepository).save(game);
    }

@Test
    public void testCleanupGameDataWithNullGame() {
        gameCleanupService.cleanupGameData(null);

        verifyNoInteractions(chatService, boardService, playerRepository, gameRepository);
        verifyNoMoreInteractions(userRepository);
    }

}
