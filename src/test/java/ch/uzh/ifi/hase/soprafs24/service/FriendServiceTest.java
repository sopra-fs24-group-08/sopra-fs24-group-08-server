package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FriendRequestRepository friendRequestRepository;

    @MockBean
    private PlayerRepository playerRepository;

    @MockBean
    private MatchmakingService matchmakingService;

    @Captor
    private ArgumentCaptor<Map> mapCaptor;

    private User user, receiver;
    private FriendRequest friendRequest, oldRequest, gameInvitation, oldInvitation, receivedRequest;
    private Player player;

    @BeforeEach
    void setUp() {
      user = new User();
      user.setId(1L);
      user.setUsername("user1");

      receiver = new User();
      receiver.setId(2L);
      receiver.setUsername("user2");

      friendRequest = new FriendRequest();
      friendRequest.setReceiverId(receiver.getId());
      friendRequest.setRequestType(RequestType.FRIENDADDING);

      receivedRequest = new FriendRequest();
      receivedRequest.setSenderId(user.getId());
      receivedRequest.setReceiverId(receiver.getId());
      receivedRequest.setRequestType(RequestType.FRIENDADDING);
      receivedRequest.setStatus(RequestStatus.ACCEPTED);

      gameInvitation = new FriendRequest();
      gameInvitation.setReceiverId(receiver.getId());
      gameInvitation.setRequestType(RequestType.GAMEINVITATION);
      

      oldRequest = new FriendRequest();
      oldRequest.setStatus(RequestStatus.PENDING);

      oldInvitation = new FriendRequest();
      oldInvitation.setStatus(RequestStatus.PENDING);

      player = new Player();
      player.setUser(receiver);

      when(userRepository.findByid(1L)).thenReturn(user);
      when(userRepository.findByid(2L)).thenReturn(receiver);
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, 1L, 2L))
        .thenReturn(null);
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, 1L, 2L))
        .thenReturn(null);
      when(playerRepository.findByUser(user)).thenReturn(null);
    }

    @Test
    void addFriendRequest_ExistingFriend_ThrowsException() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.addFriendRequest(1L, friendRequest)
      );
      assertTrue(exception.getMessage().contains("The user is already in your friend list."));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The user is already in your friend list.", mapCaptor.getValue().get("error"));
    }

    @Test
    void addFriendRequest_invalidType_ThrowsException() {
      // Setup user to already have receiver as a friend
      friendRequest.setRequestType(RequestType.GAMEINVITATION);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.addFriendRequest(1L, friendRequest)
      );
      assertTrue(exception.getMessage().contains("The request type is not FRIENDADDING!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The request type is not FRIENDADDING!", mapCaptor.getValue().get("error"));
    }

    @Test
    void addFriendRequest_addSelf_ThrowsException() {
      // Setup user to already have receiver as a friend
      friendRequest.setReceiverId(1L);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.addFriendRequest(1L, friendRequest)
      );
      assertTrue(exception.getMessage().contains("You can't add yourself as a friend!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("You can't add yourself as a friend!", mapCaptor.getValue().get("error"));
    }

    @Test
    void addFriendRequest_notFound_ThrowsException() {
      // Setup user to already have receiver as a friend
      when(userRepository.findByid(2L)).thenReturn(null);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.addFriendRequest(1L, friendRequest)
      );
      assertTrue(exception.getMessage().contains("Can find the user you want to as as friend."));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("Can find the user you want to as as friend.", mapCaptor.getValue().get("error"));
    }

    @Test
    void addFriendRequest_FrequentRequest_ThrowsException() {
      // Setup user to already have receiver as a friend
      oldRequest.setCreationTime(LocalDateTime.now().minusSeconds(1));
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId()))
        .thenReturn(oldRequest);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.addFriendRequest(1L, friendRequest)
      );
      assertTrue(exception.getMessage().contains("You've already sent a friend request to this user recently. Please try it later!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("You've already sent a friend request to this user recently. Please try it later!", mapCaptor.getValue().get("error"));
    }

    @Test
    void addFriendRequest_NoOldRequest_success() {
      // Setup user to already have receiver as a friend
      LocalDateTime beforeCalling = LocalDateTime.now();
      doNothing().when(messagingTemplate).convertAndSend(anyString(), any(FriendRequestDTO.class));
      friendService.addFriendRequest(user.getId(), friendRequest);
      assertEquals(user.getId(), friendRequest.getSenderId());
      assertEquals(RequestStatus.PENDING, friendRequest.getStatus());
      assertTrue(friendRequest.getCreationTime().isAfter(beforeCalling),
                   "Creation time should be within five minutes before and after now");
      verify(friendRequestRepository).save(friendRequest);
      verify(friendRequestRepository).flush();
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(FriendRequestDTO.class));
    } 

    @Test
    void addFriendRequest_TimeoutOldRequest_success() {
      // Setup user to already have receiver as a friend
      LocalDateTime beforeCalling = LocalDateTime.now();
      oldRequest.setCreationTime(LocalDateTime.now().minusSeconds(61));
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId()))
        .thenReturn(oldRequest);
      friendService.addFriendRequest(user.getId(), friendRequest);
      assertEquals(user.getId(), friendRequest.getSenderId());
      assertEquals(RequestStatus.PENDING, friendRequest.getStatus());
      assertTrue(friendRequest.getCreationTime().isAfter(beforeCalling),
                   "Creation time should be later than previous calling.");
      verify(friendRequestRepository).delete(oldRequest);
      verify(friendRequestRepository).save(friendRequest);
      verify(friendRequestRepository).flush();
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(FriendRequestDTO.class));
    } 

    @Test
    void inviteFriendToGame_NotFriend_ThrowsException() {
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(user.getId(), gameInvitation)
      );
      assertTrue(exception.getMessage().contains("The user is not your friend."));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The user is not your friend.", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_invalidType_ThrowsException() {
      user.getFriends().add(receiver);
      gameInvitation.setRequestType(RequestType.FRIENDADDING);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(user.getId(), gameInvitation)
      );
      assertTrue(exception.getMessage().contains("The request type is not GAMEINVITATION!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The request type is not GAMEINVITATION!", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_inviteSelf_ThrowsException() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      gameInvitation.setReceiverId(user.getId( ));
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(user.getId(), gameInvitation)
      );
      assertTrue(exception.getMessage().contains("You can't invite yourself into a game!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("You can't invite yourself into a game!", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_notFound_ThrowsException() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      when(userRepository.findByid(receiver.getId())).thenReturn(null);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(user.getId(), gameInvitation)
      );
      assertTrue(exception.getMessage().contains("The user doesn't exist."));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The user doesn't exist.", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_inGame_ThrowsException() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      when(playerRepository.findByUser(receiver)).thenReturn(player);
      when(playerRepository.findByUser(user)).thenReturn(null);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(user.getId(), gameInvitation)
      );
      assertTrue(exception.getMessage().contains("Your friend is in a game now!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("Your friend is in a game now!", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_FrequentRequest_ThrowsException() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      oldInvitation.setCreationTime(LocalDateTime.now().minusSeconds(1));
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, user.getId(), receiver.getId()))
        .thenReturn(oldInvitation);
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
          friendService.inviteFriendToGame(1L, gameInvitation)
      );
      assertTrue(exception.getMessage().contains("U are sending too many invitation requests! Please try later!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("U are sending too many invitation requests! Please try later!", mapCaptor.getValue().get("error"));
    }

    @Test
    void inviteFriendToGame_NoOldRequest_success() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      LocalDateTime beforeCalling = LocalDateTime.now();
      doNothing().when(messagingTemplate).convertAndSend(anyString(), any(FriendRequestDTO.class));
      friendService.inviteFriendToGame(user.getId(), gameInvitation);
      assertEquals(user.getId(), gameInvitation.getSenderId());
      assertEquals(RequestStatus.PENDING, gameInvitation.getStatus());
      assertTrue(gameInvitation.getCreationTime().isAfter(beforeCalling),
                   "Creation time should be later than previous calling.");
      verify(friendRequestRepository).save(gameInvitation);
      verify(friendRequestRepository).flush();
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(FriendRequestDTO.class));
    } 

    @Test
    void inviteFriendToGame_TimeoutOldInvitation_success() {
      // Setup user to already have receiver as a friend
      user.getFriends().add(receiver);
      LocalDateTime beforeCalling = LocalDateTime.now();
      oldInvitation.setCreationTime(LocalDateTime.now().minusSeconds(61));
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, user.getId(), receiver.getId()))
        .thenReturn(oldInvitation);
      friendService.inviteFriendToGame(user.getId(), gameInvitation);
      assertEquals(user.getId(), gameInvitation.getSenderId());
      assertEquals(RequestStatus.PENDING, gameInvitation.getStatus());
      assertTrue(gameInvitation.getCreationTime().isAfter(beforeCalling),
                   "Creation time should be within five minutes before and after now");
      verify(friendRequestRepository).delete(oldInvitation);
      verify(friendRequestRepository).save(gameInvitation);
      verify(friendRequestRepository).flush();
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(FriendRequestDTO.class));
    } 

    @Test
    void handleRequest_ReceiverIsNotUser_ThrowException() {
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
      friendService.handleRequest(user.getId(), friendRequest)
      );
      assertTrue(exception.getMessage().contains("You can't reply other's request!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("You can't reply other's request!", mapCaptor.getValue().get("error"));
    }   

    @Test
    void handleRequest_RequestNotFound_ThrowException() {
      Exception exception = assertThrows(IllegalArgumentException.class, () ->
      friendService.handleRequest(receiver.getId(), friendRequest)
      );
      assertTrue(exception.getMessage().contains("The request doesn't exist!"));
      verify(messagingTemplate, times(1)).convertAndSend(anyString(), mapCaptor.capture());
      assertEquals("The request doesn't exist!", mapCaptor.getValue().get("error"));
    }   

    @Test
    void handleRequest_friendAdding_Accepted() {
      friendRequest.setStatus(RequestStatus.PENDING);
      friendRequest.setSenderId(user.getId());
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId()))
      .thenReturn(friendRequest);
      friendService.handleRequest(receiver.getId(), receivedRequest);
      verify(friendRequestRepository).findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId());
      verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(FriendRequestDTO.class));
      assertTrue(user.getFriends().contains(receiver), "Receiver should be in user's friendlist now.");
      assertTrue(receiver.getFriends().contains(user), "user should be in receiver's friendlist now.");
    }

    @Test
    void handleRequest_friendAdding_Declined() {
      friendRequest.setStatus(RequestStatus.PENDING);
      friendRequest.setSenderId(user.getId());
      receivedRequest.setStatus(RequestStatus.DECLINED);
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId()))
      .thenReturn(friendRequest);
      friendService.handleRequest(receiver.getId(), receivedRequest);
      verify(friendRequestRepository).findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, user.getId(), receiver.getId());
      verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(FriendRequestDTO.class));
      assertFalse(user.getFriends().contains(receiver), "Receiver should not be in user's friendlist now.");
      assertFalse(receiver.getFriends().contains(user), "user should not be in receiver's friendlist now.");
    }

    @Test
    void handleRequest_GameInvitation_Accepted() {
      gameInvitation.setStatus(RequestStatus.PENDING);
      gameInvitation.setSenderId(user.getId());
      receivedRequest.setRequestType(RequestType.GAMEINVITATION);
      when(friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, user.getId(), receiver.getId()))
      .thenReturn(gameInvitation);
      friendService.handleRequest(receiver.getId(), receivedRequest);
      verify(friendRequestRepository).findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, user.getId(), receiver.getId());
      verify(matchmakingService, times(1)).startGameWithFriend(gameInvitation.getSenderId(), gameInvitation.getReceiverId());
    }

    @Test
    void testProvideAllPendingRequest(){
      friendRequest.setStatus(RequestStatus.PENDING);
      friendRequest.setSenderId(user.getId());
      List<FriendRequest> requests = new ArrayList<>();
      requests.add(friendRequest);
      when(friendRequestRepository.findByRequestTypeAndReceiverIdAndStatus(RequestType.FRIENDADDING, receiver.getId(), RequestStatus.PENDING))
      .thenReturn(requests);
      List<FriendRequestDTO> expectedRequestDTOs = requests.stream().map(friendService::convertEntityToFriendRequestDTO).collect(Collectors.toList());
      List<FriendRequestDTO> realRequestDTOs = friendService.provideAllPendingRequest(receiver.getId());
      assertEquals(expectedRequestDTOs.size(), realRequestDTOs.size());
    }

    @Test
    void deleteFriend_notFound_throwException(){
      when(userRepository.findByid(2L)).thenReturn(null);
      Exception exception = assertThrows(ResponseStatusException.class, () -> {
        friendService.deleteFriend(user.getId(), receiver.getId());
      });
      assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatus());
      assertEquals("Friend doesn't exist.", ((ResponseStatusException) exception).getReason());
    }

    @Test
    void deleteFriend_notFriend_throwException(){
      Exception exception = assertThrows(ResponseStatusException.class, () -> {
        friendService.deleteFriend(user.getId(), receiver.getId());
      });
      assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatus());
      assertEquals("The user is not in your friend list.", ((ResponseStatusException) exception).getReason());
    }

    @Test
    void deleteFriend_inList_success(){
      user.addFriend(receiver);
      receiver.addFriend(user);
      assertTrue(user.getFriends().contains(receiver), "Receiver should be in user's friendlist now.");
      friendService.deleteFriend(user.getId(), receiver.getId());
      assertFalse(user.getFriends().contains(receiver), "Receiver should not be in user's friendlist now.");
    }

    @Test
    void testConvertEntityToFriendRequestDTO_success(){
      friendRequest.setSenderId(user.getId());
      friendRequest.setStatus(RequestStatus.PENDING);
      FriendRequestDTO realDTO = friendService.convertEntityToFriendRequestDTO(friendRequest);
      assertEquals(realDTO.getReceiverId(), friendRequest.getReceiverId());
      assertEquals(realDTO.getSenderId(), friendRequest.getSenderId());
      assertEquals(realDTO.getRequestType(), friendRequest.getRequestType());
      assertEquals(realDTO.getStatus(), friendRequest.getStatus());
      assertEquals(realDTO.getReceiverName(), receiver.getUsername());
      assertEquals(realDTO.getSenderName(), user.getUsername());
    }
}

