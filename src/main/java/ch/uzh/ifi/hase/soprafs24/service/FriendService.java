package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendGetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final PlayerRepository playerRepository;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket messaging template


    @Autowired
    public FriendService(@Qualifier("userRepository") UserRepository userRepository,
                         @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository,
                         @Qualifier("playerRepository") PlayerRepository playerRepository,
                         GameService gameService,SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.playerRepository = playerRepository;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;


    }
    //get Friend list
    /*public List<User> getFriends(Long userId) {
        User currentUser = userRepository.findByid(userId);
        if (currentUser == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can find the current user.");
        }
        return currentUser.getFriends();
    }*/

    public List<User> getFriendsQuery(Long userId) {
        return userRepository.findFriendsByUserId(userId);
    }

    //Add friend request
    @Transactional
    public User addFriendRequest(Long userId, FriendRequest friendAdding){
        // make sure the type of request is friendadding
        if (friendAdding.getRequestType() != RequestType.FRIENDADDING){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The request type is not FRIENDADDING!");
        }
        // query if there exists such a user
        Long receiverId = friendAdding.getReceiverId();
        if (Objects.equals(receiverId, userId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can't add yourself as a friend!");
        }
        User receiver = userRepository.findByid(receiverId);
        if (receiver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can find the user you want to as as friend.");
        }
        // query if the friend is already in user's friendlist
        User user = userRepository.findByid(userId);
        if (user.getFriends().contains(receiver)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The user is already in your friend list.");
        }
        // query if exists the request from the same sender to the same receiver
        FriendRequest oldRequest = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, userId, receiverId);
        if (oldRequest != null && (oldRequest.getStatus() == RequestStatus.SENT || oldRequest.getStatus() == RequestStatus.PENDING)){
            // query the creation_time
            LocalDateTime previousTime = oldRequest.getCreationTime();
            LocalDateTime nowTime = LocalDateTime.now();
            long duration = Duration.between(nowTime, previousTime).getSeconds();
            if (duration < GlobalConstants.MAX_REQUEST_DURATION){
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You've already sent a friend request to this user recently.");
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
        return receiver;
    }
    public void acceptedGameInvitation(Long senderId, Long receiverId) {
        FriendRequest gameInvite = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        if (gameInvite == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found.");
        }
        messagingTemplate.convertAndSend("/user/"+senderId, "Game invitation accepted!");
    }   ///user/{userId}/queue/responses

    // /app/game/{gameId}/accept
    public void declinedGameInvitation(Long senderId, Long receiverId) {
        FriendRequest gameInvite = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        messagingTemplate.convertAndSend("/user/"+senderId, "Game invitation accepted!");
    }   ///user/{userId}/queue/responses*/


    //WS
    public FriendRequest acceptFriendRequest(Long senderId, Long receiverId) {
        // Fetch the existing friend request
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);

        if (friendRequest == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found.");
        }

        // Update the status of the friend request
        friendRequest.setStatus(RequestStatus.ACCEPTED);
        friendRequestRepository.save(friendRequest);

        return friendRequest;
    }
    //WS
    public void declineFriendRequest(Long senderId, Long receiverId) {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        if (friendRequest.getReceiverId().equals(receiverId) && friendRequest.getStatus() == RequestStatus.PENDING) {
            friendRequest.setStatus(RequestStatus.DECLINED);
            friendRequestRepository.save(friendRequest);

            User sender = userRepository.findById(friendRequest.getSenderId()).orElseThrow(() -> new IllegalStateException("Sender not found"));
            // Notify the sender that their friend request has been declined
            //FIx later
            messagingTemplate.convertAndSendToUser(sender.getId().toString(), "/queue/friend-requests", "Your friend request has been declined by " );
        }
    }

    private void forwardToWebSocket(String message) {
        // Forward to WebSocket using a specific topic or user queue
        messagingTemplate.convertAndSend("/topic/messageRoute", message);
    }


    @Transactional
    public User addFriendAutomatically(Long userId, FriendGetDTO fakefriendRequest){
        User user = userRepository.findByid(userId);
        long friendId = fakefriendRequest.getId();
        User friend = userRepository.findByid(friendId);
        user.addFriend(friend);
        friend.addFriend(user);
        userRepository.save(user);
        userRepository.save(friend);
        userRepository.flush();
        return user;
    }

    //Invitation to game request
    @Transactional
    public FriendRequest inviteFriendToGame(Long userId, FriendRequest gameInvitation){
        // make sure the type of request is game invitation
        if (gameInvitation.getRequestType() != RequestType.GAMEINVITATION){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The request type is not GAMEINVITATION!");
        }
        // query if there exists such a user
        Long receiverId = gameInvitation.getReceiverId();
        if (Objects.equals(receiverId, userId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can't invite yourself into a game!");
        }
        User receiver = userRepository.findByid(receiverId);
        if (receiver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The user doesn't exist.");
        }
        // query if the invitation is sent to the friend
        User user = userRepository.findByid(userId);
        if (!user.getFriends().contains(receiver)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The user is not your friend.");
        }
        // query if sender or receiver is already in a game
        Player player1 = playerRepository.findByUser(user);
        Player player2 = playerRepository.findByUser(receiver);
        if (player1 != null || player2 != null){
          throw new ResponseStatusException(HttpStatus.CONFLICT, "At least 1 of the users is already in game!");
        }

        // query if exists the request from the same sender to the same receiver
        FriendRequest oldGameInvitation = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, userId, receiverId);
        if (oldGameInvitation != null){
            // query the creation_time
            LocalDateTime previousTime = oldGameInvitation.getCreationTime();
            if (Duration.between(LocalDateTime.now(), previousTime).getSeconds() < GlobalConstants.MAX_REQUEST_DURATION){
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "U are sending too many invitation requests!");
            }else{
                // delete the old request if it's beyond 60 sec
                friendRequestRepository.delete(oldGameInvitation);
            }
        }
        // create new request and preserve into repository
        gameInvitation.setSenderId(userId);
        gameInvitation.setStatus(RequestStatus.PENDING);
        friendRequestRepository.save(gameInvitation);
        friendRequestRepository.flush();
        return gameInvitation;
    }

    //handle friend request
    @Transactional
    public FriendRequest handleFriendRequest(Long userId, FriendRequest receivedFriendAdding){
        // check if there does exist such a request
        Long friendId = receivedFriendAdding.getSenderId();
        FriendRequest friendAdding = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING,friendId, userId);
        if (friendAdding == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This person never sent you a friend request.");
        }else if (friendAdding.getStatus() != RequestStatus.SENT){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This friend request has already been processed.");
        }
        // change request status
        if (receivedFriendAdding.getStatus() == RequestStatus.ACCEPTED){
            friendAdding.setStatus(RequestStatus.ACCEPTED);
            // add friend into both user's friend list
            User user = userRepository.findByid(userId);
            User friend = userRepository.findByid(friendId);
            user.addFriend(friend);
            friend.addFriend(user);
        } else if (receivedFriendAdding.getStatus() == RequestStatus.DECLINED){
            friendAdding.setStatus(RequestStatus.DECLINED);
        }
        return friendAdding;
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