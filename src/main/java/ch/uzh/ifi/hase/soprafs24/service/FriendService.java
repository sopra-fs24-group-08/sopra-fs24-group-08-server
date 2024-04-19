package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import javassist.tools.framedump;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final GameService gameService;

    @Autowired
    public FriendService(@Qualifier("userRepository") UserRepository userRepository,
                         @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository,
                         GameService gameService) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.gameService = gameService;

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
        if (receiverId == userId){
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
            Long duration = Duration.between(nowTime, previousTime).getSeconds();
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

    //Invitation to game request
    @Transactional
    public FriendRequest inviteFriendToGame(Long userId, FriendRequest gameInvitation){
        // make sure the type of request is friendadding
        if (gameInvitation.getRequestType() != RequestType.GAMEINVITATION){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The request type is not GAMEINVITATION!");
        }
        // query if there exists such a user
        Long receiverId = gameInvitation.getReceiverId();
        if (receiverId == userId){
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

    //handle game invitation
    //adjusted Method, trying to fix existing problems.
    @Transactional
    public FriendRequest handleGameInvitation(Long userId, FriendRequest receivedGameInvitation) {
        System.out.println("HandleGameInv is there");
        FriendRequest gameInvitation = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.GAMEINVITATION, receivedGameInvitation.getSenderId(), userId);
        if (gameInvitation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This person never invited you.");
        } else if (gameInvitation.getStatus() != RequestStatus.SENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This game invitation has already been processed.");
        }

        if (receivedGameInvitation.getStatus() == RequestStatus.ACCEPTED) {
            gameInvitation.setStatus(RequestStatus.ACCEPTED);
            Game friendlyGame = gameService.createGame();
            gameService.startFriendsGame(friendlyGame.getGameId(), userId, receivedGameInvitation.getSenderId());
        } else if (receivedGameInvitation.getStatus() == RequestStatus.DECLINED) {
            gameInvitation.setStatus(RequestStatus.DECLINED);
        }
        return gameInvitation;
    }


    // check for polling updates
    @Transactional
    public void pollUpdates(DeferredResult<List<FriendRequestDTO>> deferredResult, Long userId) {
        // Friend request: including userId as senderId and as receiverId
        List<FriendRequest> pendingRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
        List <FriendRequest> acceptedOrDeclinedRequests = friendRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.ACCEPTED);
        acceptedOrDeclinedRequests.addAll(friendRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.DECLINED));
        List<FriendRequest> friendRequests = new ArrayList<>(pendingRequests);
        friendRequests.addAll(acceptedOrDeclinedRequests);

        List<FriendRequestDTO> friendRequestDTOs = friendRequests.stream()
                .map(this::convertEntityToFriendRequestDTO)
                .collect(Collectors.toList());

        if (!friendRequestDTOs.isEmpty()){
            deferredResult.setResult(friendRequestDTOs);
            // Delete finished friend reqeust and game invitations. Do we need to presetve them?
            friendRequestRepository.deleteInBatch(acceptedOrDeclinedRequests);
            // set request status as sent to avoid repeat send
            for (FriendRequest friendRequest: pendingRequests){
                friendRequest.setStatus(RequestStatus.SENT);
            }
        }else {
            deferredResult.onTimeout(() -> deferredResult.setErrorResult(
                    ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                            .body("No updates at the moment, please try again later.")));
        }
    }

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