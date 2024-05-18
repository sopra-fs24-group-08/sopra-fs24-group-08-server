package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendGetDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameInvitationDTO;
import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.service.MatchService;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameMatchResultDTO;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final PlayerRepository playerRepository;
    private final GameService gameService;
    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket messaging template


    @Autowired
    public FriendService(@Qualifier("userRepository") UserRepository userRepository,
                         @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository,
                         @Qualifier("playerRepository") PlayerRepository playerRepository,
                         GameService gameService,MatchmakingService matchmakingService, SimpMessagingTemplate messagingTemplate) {
      this.userRepository = userRepository;
      this.friendRequestRepository = friendRequestRepository;
      this.playerRepository = playerRepository;
      this.gameService = gameService;
      this.matchmakingService = matchmakingService;
      this.messagingTemplate = messagingTemplate;


    }

    private void forwardToWebSocket(FriendRequest request, Long receiverId) {
      // Forward to WebSocket using a specific topic or user queue
      FriendRequestDTO requestDTO = convertEntityToFriendRequestDTO(request);
      messagingTemplate.convertAndSend("/topic/queue/"+receiverId + "/notifications", requestDTO);
      System.out.println("/topic/queue/"+receiverId + "/notifications");
    }

    private void forwardErrorMessage(Long userId, String message) {
      messagingTemplate.convertAndSend("/topic/queue/"+userId + "/notifications", Collections.singletonMap("error", message));
      throw new IllegalArgumentException(message);
    }

    public List<User> getFriendsQuery(Long userId) {
        return userRepository.findFriendsByUserId(userId);
    }

    //Add friend request
    @Transactional
    public void addFriendRequest(Long userId, FriendRequest friendAdding){
      // make sure the type of request is friendadding
      System.out.println("user is trying to add a friend!");
      if (friendAdding.getRequestType() != RequestType.FRIENDADDING){
        forwardErrorMessage(userId, "The request type is not FRIENDADDING!");
      }
      // query if there exists such a user
      Long receiverId = friendAdding.getReceiverId();
      if (Objects.equals(receiverId, userId)){
        forwardErrorMessage(userId, "You can't add yourself as a friend!");
      }
      User receiver = userRepository.findByid(receiverId);
      if (receiver == null) {
        forwardErrorMessage(userId, "Can find the user you want to as as friend.");
      }
      // query if the friend is already in user's friendlist
      User user = userRepository.findByid(userId);
      if (user.getFriends().contains(receiver)){
        forwardErrorMessage(userId, "The user is already in your friend list.");
      }
      // query if exists the request from the same sender to the same receiver
      FriendRequest oldRequest = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, userId, receiverId);
      if (oldRequest != null && oldRequest.getStatus() == RequestStatus.PENDING){
          // query the creation_time
          LocalDateTime previousTime = oldRequest.getCreationTime();
          LocalDateTime nowTime = LocalDateTime.now();
          long duration = Duration.between(nowTime, previousTime).getSeconds();
          if (duration < GlobalConstants.MAX_REQUEST_DURATION){
            forwardErrorMessage(userId, "You've already sent a friend request to this user recently. Please try it later!");
          }else{
              // delete the old request if it's beyond 60 sec
              friendRequestRepository.delete(oldRequest);
          }
      }
      // create new request and preserve into repository
      friendAdding.setSenderId(userId);
      friendAdding.setStatus(RequestStatus.PENDING);
      friendAdding.setCreationTime(LocalDateTime.now());
      friendRequestRepository.save(friendAdding);
      friendRequestRepository.flush();
      forwardToWebSocket(friendAdding, receiverId);
    }

    //Invitation to game request
    @Transactional
    public void inviteFriendToGame(Long userId, FriendRequest gameInvitation){
      // make sure the type of request is game invitation
      if (gameInvitation.getRequestType() != RequestType.GAMEINVITATION){
        forwardErrorMessage(userId, "The request type is not GAMEINVITATION!");
      }
      // query if there exists such a user
      Long receiverId = gameInvitation.getReceiverId();
      if (Objects.equals(receiverId, userId)){
        forwardErrorMessage(userId, "You can't invite yourself into a game!");
      }
      User receiver = userRepository.findByid(receiverId);
      if (receiver == null) {
        forwardErrorMessage(userId, "The user doesn't exist.");
      }
      // query if the invitation is sent to the friend
      User user = userRepository.findByid(userId);
      if (!user.getFriends().contains(receiver)){
        forwardErrorMessage(userId, "The user is not your friend.");
      }
      // query if sender or receiver is already in a game
      Player player1 = playerRepository.findByUser(user);
      Player player2 = playerRepository.findByUser(receiver);
      if (player1 != null || player2 != null){
        forwardErrorMessage(userId, "At least 1 of the users is already in game!");
      }
      // query if exists the request from the same sender to the same receiver
      FriendRequest oldGameInvitation = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, userId, receiverId);
      if (oldGameInvitation != null && oldGameInvitation.getStatus() == RequestStatus.PENDING){
        // query the creation_time
        LocalDateTime previousTime = oldGameInvitation.getCreationTime();
        LocalDateTime nowTime = LocalDateTime.now();
        Long duration = Duration.between(nowTime, previousTime).getSeconds();
        if (duration < GlobalConstants.MAX_REQUEST_DURATION){
          forwardErrorMessage(userId, "U are sending too many invitation requests! Please try later!");
        }else{
            // delete the old request if it's beyond 60 sec
            friendRequestRepository.delete(oldGameInvitation);
        }
      }
      // create new request and preserve into repository
      gameInvitation.setSenderId(userId);
      gameInvitation.setStatus(RequestStatus.PENDING);
      gameInvitation.setCreationTime(LocalDateTime.now());
      friendRequestRepository.save(gameInvitation);
      friendRequestRepository.flush();
      forwardToWebSocket(gameInvitation, receiverId);
    }

    public void handleRequest(Long userId, FriendRequest receivedRequest){
      RequestType type = receivedRequest.getRequestType();
      Long senderId = receivedRequest.getSenderId();
      Long receiverId = receivedRequest.getReceiverId();
      // check if the receiver is the user
      if (!receiverId.equals(userId)){
        forwardErrorMessage(userId, "You can't reply other's request!");
      }
      // check if the request exists
      FriendRequest friendRequest = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(type, senderId, receiverId);
      if (friendRequest == null || friendRequest.getStatus() != RequestStatus.PENDING){
        forwardErrorMessage(userId, "The request doesn't exist!");
      }
      // handle requests
      dealRequest(friendRequest, receivedRequest.getStatus());
    }

    public void addtoFriendList(Long userId1, Long userId2){
      User user1 = userRepository.findByid(userId1);
      User user2 = userRepository.findByid(userId2);
      user1.addFriend(user2);
      user2.addFriend(user1);
    }

    //WS
    public synchronized void dealRequest(FriendRequest request, RequestStatus result) {
        request.setStatus(result);
        friendRequestRepository.save(request);
        if (!(request.getStatus() == RequestStatus.ACCEPTED && request.getRequestType() == RequestType.GAMEINVITATION)){
          forwardToWebSocket(request, request.getReceiverId());
          forwardToWebSocket(request, request.getSenderId());
        }
        // Handling accepted requests
        if (request.getStatus() == RequestStatus.ACCEPTED) {
            if (request.getRequestType() == RequestType.FRIENDADDING) {
                // Add to friend list if the request is for adding a friend
                addtoFriendList(request.getSenderId(), request.getReceiverId());
            } else if (request.getRequestType() == RequestType.GAMEINVITATION) {
                // Start game with friend if the request is for a game invitation
                matchmakingService.startGameWithFriend(request.getSenderId(), request.getReceiverId());
            }
        }

        // Deleting the request from the repository after handling
        friendRequestRepository.deleteById(request.getId());
    }

    // provide all pending request
    public List<FriendRequestDTO> provideAllPendingRequest(Long userId){
      List<FriendRequest> requests = friendRequestRepository
                        .findByRequestTypeAndReceiverIdAndStatus(RequestType.FRIENDADDING, userId, RequestStatus.PENDING);
      
      return requests.stream().map(this::convertEntityToFriendRequestDTO).collect(Collectors.toList());
    }

    // public void acceptedGameInvitation(Long senderId, Long receiverId) {
    //     FriendRequest gameInvite = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    //     if (gameInvite == null) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found.");
    //     }
    //     messagingTemplate.convertAndSend("/user/"+senderId, "Game invitation accepted!");

    // }   ///user/{userId}/queue/responses

    // // /app/game/{gameId}/accept
    // public void declinedGameInvitation(Long senderId, Long receiverId) {
    //     FriendRequest gameInvite = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    //     messagingTemplate.convertAndSend("/user/"+senderId, "Game invitation accepted!");
    // }   ///user/{userId}/queue/responses*/

    // @Transactional
    // public User addFriendAutomatically(Long userId, FriendGetDTO fakefriendRequest){
    //     User user = userRepository.findByid(userId);
    //     long friendId = fakefriendRequest.getId();
    //     User friend = userRepository.findByid(friendId);
    //     user.addFriend(friend);
    //     friend.addFriend(user);
    //     userRepository.save(user);
    //     userRepository.save(friend);
    //     userRepository.flush();
    //     return user;
    // }

    //handle game invitation
    //adjusted Method, trying to fix existing problems.
    // @Transactional
    // public FriendRequest handleGameInvitation(Long userId, FriendRequest receivedGameInvitation) {
    //     System.out.println("HandleGameInv is there");
    //     FriendRequest gameInvitation = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, receivedGameInvitation.getSenderId(), userId);
    //     if (gameInvitation == null) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This person never invited you.");
    //     } else if (gameInvitation.getStatus() != RequestStatus.PENDING) {
    //         throw new ResponseStatusException(HttpStatus.CONFLICT, "This game invitation has already been processed.");
    //     }

    //     if (receivedGameInvitation.getStatus() == RequestStatus.ACCEPTED) {
    //         gameInvitation.setStatus(RequestStatus.ACCEPTED);
    //         Game friendlyGame = gameService.createGame();
    //        // gameService.startFriendsGame(friendlyGame.getGameId(), userId, receivedGameInvitation.getSenderId());
    //     } else if (receivedGameInvitation.getStatus() == RequestStatus.DECLINED) {
    //         gameInvitation.setStatus(RequestStatus.DECLINED);
    //     }
    //     return gameInvitation;
    // }

    //Delete friend
    public void deleteFriend(Long userId, Long friendId){
        User currentUser = userRepository.findByid(userId);
        User oldFriend = userRepository.findByid(friendId);
        if (oldFriend == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend Username doesn't exist.");
        }else if (!currentUser.getFriends().contains(oldFriend)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The user is not in your friend list.");
        }
        currentUser.deleteFriend(oldFriend);
        oldFriend.deleteFriend(currentUser);
    }

    //convert request to FriendRequestDTO
    public FriendRequestDTO convertEntityToFriendRequestDTO(FriendRequest friendRequest) {
        FriendRequestDTO dto = new FriendRequestDTO();
        Long senderId = friendRequest.getSenderId();
        Long receiverId = friendRequest.getReceiverId();
        String senderName = userRepository.findByid(senderId).getUsername();
        String receiverName = userRepository.findByid(receiverId).getUsername();
        dto.setStatus(friendRequest.getStatus());
        dto.setRequestType(friendRequest.getRequestType());
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setSenderName(senderName);
        dto.setReceiverName(receiverName);
        return dto;
    }
}