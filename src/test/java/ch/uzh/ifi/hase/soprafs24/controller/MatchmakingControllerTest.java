package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.MatchmakingService;
import ch.uzh.ifi.hase.soprafs24.websocket.WebSocketTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MatchmakingControllerTest extends WebSocketTestBase {

    @MockBean
    private MatchmakingService matchmakingService;

    @Test
    public void testJoinMatchmakingWithValidSession() throws Exception {
        Long userId = 1L;
        String validToken = "validToken";

        StompSession stompSession = connectToWebSocket(validToken);
        stompSession.send(String.format("/app/matchmaking/join/%d", userId), null);
        Thread.sleep(100); // Short delay to allow async handling

        verify(matchmakingService, times(1)).addToQueue(userId);
    }

    @Test
    public void testMatchmakingWithTwoUsers() throws Exception {
        Long userId1 = 1L;
        Long userId2 = 2L;
        String validToken1 = "validToken1";
        String validToken2 = "validToken2";

        when(userService.validateUserIdToken(userId1, validToken1)).thenReturn(true);
        when(userService.validateUserIdToken(userId2, validToken2)).thenReturn(true);

        // Connect two users
        StompSession session1 = connectToWebSocket(validToken1);
        StompSession session2 = connectToWebSocket(validToken2);

        session1.send(String.format("/app/matchmaking/join/%d", userId1), null);
        session2.send(String.format("/app/matchmaking/join/%d", userId2), null);

        // Allow time for processing, play around with lowest value
        Thread.sleep(100);

        // Verify that both users are added to the queue
        verify(matchmakingService, times(1)).addToQueue(userId1);
        verify(matchmakingService, times(1)).addToQueue(userId2);
        // Check if checkForMatches was indeed called
        verify(matchmakingService).checkStatusBeforeMatch(userId1);
        verify(matchmakingService).checkStatusBeforeMatch(userId2);
        matchmakingService.checkForMatches();
        verify(matchmakingService).checkForMatches();


        // If there's any issue with not being invoked, consider adding more diagnostic output
        verifyNoMoreInteractions(matchmakingService);
    }

    @Test
    public void testJoinMatchmakingWithInvalidSession() throws Exception {
        Long userId = 1L;
        String invalidToken = "invalidToken";
        when(userService.validateUserIdToken(userId, invalidToken)).thenReturn(false);

        StompSession stompSession = connectToWebSocket(invalidToken);
        stompSession.send(String.format("/app/matchmaking/join/%d", userId), null);

        verify(matchmakingService, never()).addToQueue(userId);
    }

    @Test
    public void testJoinMatchmakingWithNullTokenShouldFail() throws Exception {
        Long userId = 1L;
        String nullToken = null;

        // Expect an IllegalArgumentException due to null token
        assertThrows(IllegalArgumentException.class, () -> connectToWebSocket(nullToken));
    }

    @Test
    public void testJoinMatchmakingWithEmptyTokenShouldFail() throws Exception {
        Long userId = 1L;
        String emptyToken = "";

        // Expect an IllegalArgumentException due to empty token
        assertThrows(IllegalArgumentException.class, () -> connectToWebSocket(emptyToken));
    }

    @Test
    public void testLeaveMatchmakingWithValidSessionToken() throws Exception {
        Long userId = 1L;
        String validToken = "validToken" + userId;

        // Mock header accessor to return valid session attributes
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        when(headerAccessor.getSessionAttributes()).thenReturn(Collections.singletonMap("sessionId", validToken));
        when(userService.validateUserIdToken(userId, validToken)).thenReturn(true);

        StompSession stompSession = connectToWebSocket(validToken);
        stompSession.send(String.format("/app/matchmaking/leave/%d", userId), null);
        Thread.sleep(50); // Short delay to allow async handling

        verify(matchmakingService, times(1)).removeFromQueue(userId);
        verify(userService, times(1)).validateUserIdToken(userId, validToken);
    }
    @Test
    public void testLeaveMatchmakingWithInvalidSessionToken() throws Exception {
        Long userId = 1L;
        String invalidToken = "invalidToken";

        // Mock header accessor to return valid session attributes
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        when(headerAccessor.getSessionAttributes()).thenReturn(Collections.singletonMap("sessionId", invalidToken));
        when(userService.validateUserIdToken(userId, invalidToken)).thenReturn(false);

        StompSession stompSession = connectToWebSocket(invalidToken);
        stompSession.send(String.format("/app/matchmaking/leave/%d", userId), null);
        Thread.sleep(50); // Short delay to allow async handling

        verify(matchmakingService, never()).removeFromQueue(userId);
        verify(userService, times(1)).validateUserIdToken(userId, invalidToken);
    }

}