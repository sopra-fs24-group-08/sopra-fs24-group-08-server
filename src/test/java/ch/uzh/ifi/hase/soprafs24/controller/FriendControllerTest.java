package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.websocket.WebSocketTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

public class FriendControllerTest extends WebSocketTestBase {

    @MockBean
    private FriendService friendService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void testAddFriend() throws Exception {
      Long userId = 1L;
      FriendRequestDTO expectedRequest = new FriendRequestDTO();
      expectedRequest.setReceiverId(2L);
      expectedRequest.setRequestType(RequestType.FRIENDADDING);
      given(userService.validateUserIdToken(anyLong(), anyString())).willReturn(true);
      StompSession stompSession = connectToWebSocket("validToken");
      stompSession.send("/app/friend/adding/"+userId, expectedRequest);
      Thread.sleep(100);
      verify(friendService).addFriendRequest(eq(userId), Mockito.any(FriendRequest.class));
    }

    @Test
    public void testInviteFriendRequest() throws Exception {
      Long userId = 1L;
      FriendRequestDTO expectedRequest = new FriendRequestDTO();
      expectedRequest.setReceiverId(2L);
      expectedRequest.setRequestType(RequestType.GAMEINVITATION);
      given(userService.validateUserIdToken(anyLong(), anyString())).willReturn(true);
      StompSession stompSession = connectToWebSocket("validToken");
      stompSession.send("/app/friend/invitation/"+userId, expectedRequest);
      Thread.sleep(100);
      verify(friendService).inviteFriendToGame(eq(userId), Mockito.any(FriendRequest.class));
    }

    @Test
    public void testHandleFriendRequest() throws Exception {
      Long userId = 1L;
      FriendRequestDTO expectedRequest = new FriendRequestDTO();
      expectedRequest.setSenderId(userId);
      expectedRequest.setReceiverId(2L);
      expectedRequest.setRequestType(RequestType.GAMEINVITATION);
      given(userService.validateUserIdToken(anyLong(), anyString())).willReturn(true);
      StompSession stompSession = connectToWebSocket("validToken");
      stompSession.send("/app/friend/result/"+userId, expectedRequest);
      Thread.sleep(100);
      verify(friendService).handleRequest(eq(userId), Mockito.any(FriendRequest.class));
    }
}
