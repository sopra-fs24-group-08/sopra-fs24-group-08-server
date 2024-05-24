package ch.uzh.ifi.hase.soprafs24.controller;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.exceptions.GameNotFoundException;
import ch.uzh.ifi.hase.soprafs24.exceptions.PlayerNotFoundException;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private GameStateDTO gameStateDTO;

    @Test
    public void startGame_Success() throws Exception {
        Long gameId = 1L;
        Long playerId = 1L;
        Game game = new Game();
        GameStateDTO gameStateDTO = new GameStateDTO();

        when(gameService.getGame(anyLong())).thenReturn(game);
        when(gameService.getGameStateForPlayer(any(Game.class), anyLong())).thenReturn(gameStateDTO);

        mockMvc.perform(post("/game/{gameId}/{playerId}/start", gameId, playerId))
                .andExpect(status().isOk());

        verify(messagingTemplate).convertAndSend("/topic/game/" + gameId + "/" + playerId, gameStateDTO);
    }
    @Test
    public void StartGameNotFound() throws Exception {
        Long gameId = 1L;
        Long playerId = 1L;
        Mockito.when(gameService.getGame(gameId)).thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(post("/game/{gameId}/{playerId}/start", gameId, playerId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GameNotFoundException))
                .andExpect(result -> assertEquals("Game not found", result.getResolvedException().getMessage()));
    }

    @Test
    public void StartPlayerNotFound() throws Exception {
        Long gameId = 1L;
        Long playerId = 1L;
        Game game = new Game();
        Mockito.when(gameService.getGame(gameId)).thenReturn(game);
        Mockito.when(gameService.getPlayerById(playerId)).thenThrow(new PlayerNotFoundException("Player not found"));

        mockMvc.perform(post("/game/{gameId}/{playerId}/start", gameId, playerId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PlayerNotFoundException))
                .andExpect(result -> assertEquals("Player not found", result.getResolvedException().getMessage()));
    }

    @Test
    void RequestGameResultFound() throws Exception {
        Long gameId = 1L;
        GameResultRequest gameResultRequest = new GameResultRequest();
        gameResultRequest.setWinnerId(1L);

        when(gameService.getGameMatchResult(gameId)).thenReturn(gameResultRequest);

        mockMvc.perform(get("/game/{gameId}/result", gameId)
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winnerId").value(1));
    }

    @Test
    void RequestGameResultNotFound() throws Exception {
        Long gameId = 2L;
        when(gameService.getGameMatchResult(gameId)).thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/game/{gameId}/result", gameId)
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void RequestGameResultIllegalState() throws Exception {
        Long gameId = 3L;
        when(gameService.getGameMatchResult(gameId)).thenThrow(new IllegalStateException("not in an ongoing state"));
        //Change later so it uses custom exception: GameNotFinished
        mockMvc.perform(get("/game/{gameId}/result", gameId)
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isConflict());
    }

    @Test
    void getWinCountSuccess() throws Exception {
        Long userId = 1L;
        Long winCount = 5L;

        when(gameService.getWinCountForUser(userId)).thenReturn(winCount);

        mockMvc.perform(get("/winCount/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getWinCountFailure() throws Exception {
        Long userId = 10L;

        // Make sure to throw the correct exception as per the service implementation
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(gameService).getWinCountForUser(userId);

        mockMvc.perform(get("/winCount/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                // Update the expected message to match the complete error response text
                .andExpect(result -> assertEquals("404 NOT_FOUND \"User not found\"", result.getResolvedException().getMessage()));
    }

    @Test
    void RequestGameResultGameIncomplete() throws Exception {
        Long gameId = 3L;
        when(gameService.getGameMatchResult(gameId)).thenThrow(new IllegalStateException("incomplete"));

        mockMvc.perform(get("/game/{gameId}/result", gameId)
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void RequestGameResultUnexpectedError() throws Exception {
        Long gameId = 3L;
        when(gameService.getGameMatchResult(gameId)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/game/{gameId}/result", gameId)
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isInternalServerError());
    }
}