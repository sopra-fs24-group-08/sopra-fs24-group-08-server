package ch.uzh.ifi.hase.soprafs24.service;

import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GameInvitation;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameInvitationRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameInvitationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CombinedUpdateDTO;
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
    private final GameInvitationRepository gameInvitationRepository;
    private GameService gameService = new GameService();

    public FriendService(@Qualifier("userRepository") UserRepository userRepository,
                         @Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository,
                         @Qualifier("invitationRepository") GameInvitationRepository gameInvitationRepository) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.gameInvitationRepository = gameInvitationRepository;
    }

    //get Friend list
    public List<User> getFriends(Long userId) {
        User currentUser = userRepository.findByid(userId);
        if (currentUser == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can find the current user.");
        }
        return currentUser.getFriends();
    }

    //Add friend request
    @Transactional
    public User addFriendRequest(Long userId, FriendRequest friendRequest){
        // query if there exists such a user
        Long receiverId = friendRequest.getReceiverId();
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
        FriendRequest oldRequest = friendRequestRepository.findBySenderIdAndReceiverId(userId, receiverId);
        if (oldRequest != null){
            // query the creation_time
            LocalDateTime previousTime = oldRequest.getCreationTime();
            if (Duration.between(LocalDateTime.now(), previousTime).getSeconds() < GlobalConstants.MAX_REQUEST_DURATION){
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You've already sent a friend request to this user recently.");
            }else{
                // delete the old request if it's beyond 60 sec
                friendRequestRepository.delete(oldRequest);
            }
        }
        // create new request and preserve into repository
        friendRequest.setSenderId(userId);
        friendRequest.setStatus(RequestStatus.PENDING);
        friendRequestRepository.save(friendRequest);
        friendRequestRepository.flush();
        return receiver;
    }

    //Invitation to game request
    @Transactional
    public GameInvitation inviteFriendToGame(Long userId, GameInvitation gameInvitation){
        // query if there exists such a user
        Long receiverId = gameInvitation.getReceiverId();
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
        GameInvitation oldGameInvitation = gameInvitationRepository.findBySenderIdAndReceiverId(userId, receiverId);
        if (oldGameInvitation != null){
            // query the creation_time
            LocalDateTime previousTime = oldGameInvitation.getCreationTime();
            if (Duration.between(LocalDateTime.now(), previousTime).getSeconds() < GlobalConstants.MAX_REQUEST_DURATION){
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "U are sending too many invitation requests!");
            }else{
                // delete the old request if it's beyond 60 sec
                gameInvitationRepository.delete(oldGameInvitation);
            }
        }
        // create new request and preserve into repository
        gameInvitation.setSenderId(userId);
        gameInvitation.setStatus(RequestStatus.PENDING);
        gameInvitationRepository.save(gameInvitation);
        gameInvitationRepository.flush();
        return gameInvitation;
    }

    //handle friend request
    @Transactional
    public FriendRequest handleFriendRequest(Long userId, FriendRequest receivedFriendRequest){
        // check if there does exist such a request
        Long friendId = receivedFriendRequest.getSenderId();
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(friendId, userId);
        if (friendRequest == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This person never sent you a friend request.");
        }else if (friendRequest.getStatus() != RequestStatus.PENDING){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This friend request has already been processed.");
        }
        // change request status
        if (receivedFriendRequest.getStatus() == RequestStatus.ACCEPTED){
            friendRequest.setStatus(RequestStatus.ACCEPTED);
            // add friend into both user's friend list
            User user = userRepository.findByid(userId);
            User friend = userRepository.findByid(friendId);
            user.addFriend(friend);
            friend.addFriend(user);
        } else if (receivedFriendRequest.getStatus() == RequestStatus.DECLINED){
            friendRequest.setStatus(RequestStatus.DECLINED);
        }
        return friendRequest;
    }

    //handle game invitation
    @Transactional
    public GameInvitation handleGameInvitation(Long userId, GameInvitation receivedGameInvitation){
        // check if there does exist such a request
        Long friendId = receivedGameInvitation.getSenderId();
        GameInvitation gameInvitation = gameInvitationRepository.findBySenderIdAndReceiverId(friendId, userId);
        if (gameInvitation == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This person never invited you.");
        }else if (gameInvitation.getStatus() != RequestStatus.PENDING){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This game invitation has already been processed.");
        }
        // change request status
        if (receivedGameInvitation.getStatus() == RequestStatus.ACCEPTED){
            gameInvitation.setStatus(RequestStatus.ACCEPTED);
            // start a game session with 2 User
            gameService.startGameSession(userId, friendId);
        } else if (receivedGameInvitation.getStatus() == RequestStatus.DECLINED){
            gameInvitation.setStatus(RequestStatus.DECLINED);
        }
        return gameInvitation;
    }


    // check for polling updates
    @Transactional
    public void pollUpdates(DeferredResult<CombinedUpdateDTO> deferredResult, Long userId) {
        // Friend request: including userId as senderId and as receiverId
        List<FriendRequest> pendingRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
        List <FriendRequest> acceptedOrDeclinedRequests = friendRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.ACCEPTED);
        acceptedOrDeclinedRequests.addAll(friendRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.DECLINED));
        List<FriendRequest> friendRequests = new ArrayList<>(pendingRequests);
        friendRequests.addAll(acceptedOrDeclinedRequests);

        // Game Invitation: including userId as senderId and as receiverId
        List<GameInvitation> pendingInvitations = gameInvitationRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
        List<GameInvitation> acceptedOrDeclinedInvitations = gameInvitationRepository.findBySenderIdAndStatus(userId, RequestStatus.ACCEPTED);
        acceptedOrDeclinedInvitations.addAll(gameInvitationRepository.findBySenderIdAndStatus(userId, RequestStatus.DECLINED));
        List<GameInvitation> gameInvitations = new ArrayList<>(pendingInvitations);
        gameInvitations.addAll(acceptedOrDeclinedInvitations);

        List<FriendRequestDTO> friendRequestDTOs = friendRequests.stream()
                .map(DTOMapper.INSTANCE::convertEntityToFriendRequestDTO)
                .collect(Collectors.toList());
        List<GameInvitationDTO> gameInviteDTOs = gameInvitations.stream()
                .map(DTOMapper.INSTANCE::convertEntityToGameInvitationDTO)
                .collect(Collectors.toList());

        if (!friendRequestDTOs.isEmpty() || !gameInvitations.isEmpty()){
            deferredResult.setResult(new CombinedUpdateDTO(friendRequestDTOs, gameInviteDTOs));
            // Delete finished friend reqeust and game invitations. Do we need to presetve them?
            friendRequestRepository.deleteInBatch(acceptedOrDeclinedRequests);
            gameInvitationRepository.deleteInBatch(acceptedOrDeclinedInvitations);
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
}