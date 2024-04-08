package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.GameInvitation;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;


@RestController
public class FriendRequestController {

    private final UserService userService;
    private final FriendService friendService;

    FriendRequestController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    // add friend request
    @PostMapping("/users/{userId}/friends/add")
    @ResponseStatus(HttpStatus.CREATED)
    public String addFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO, @RequestHeader String token, @PathVariable Long userId) {
        userService.authenticateUser(token, userId);
        FriendRequest friendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        friendService.addFriendRequest(userId, friendRequest);
        return "Request sent";
    }

    // Long-polling to check for adding friend requests/response and game invitation request/response
    @GetMapping("/users/{userId}/polling")
    @ResponseStatus(HttpStatus.OK)
    public DeferredResult<CombinedUpdateDTO> pollFriendRequest(@PathVariable Long userId, @RequestHeader String token) {
        userService.authenticateUser(token, userId);
        DeferredResult<CombinedUpdateDTO> deferredResult = new DeferredResult<>(GlobalConstants.POLL_TIMEOUT); // 5 seconds timeout
        friendService.pollUpdates(deferredResult, userId);
        return deferredResult;
    }

    // Handle friend request
    @PutMapping("/users/{userId}/friendresponse")
    @ResponseStatus(HttpStatus.OK)
    public FriendRequestDTO handleFriendRequest(@PathVariable Long userId, @RequestBody FriendRequestDTO friendRequestDTO, @RequestHeader String token){
        userService.authenticateUser(token, userId);
        // turn DTO to entity
        FriendRequest receivedFriendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        //
        FriendRequest friendRequest = friendService.handleFriendRequest(userId, receivedFriendRequest);
        return DTOMapper.INSTANCE.convertEntityToFriendRequestDTO(friendRequest);
    }

    //Invite friend into game
    @PostMapping("/game/invite/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public String gameInvitation(@RequestBody GameInvitationDTO gameInvitationDTO, @RequestHeader String token, @PathVariable Long userId) {
        userService.authenticateUser(token, userId);
        GameInvitation gameInvitation = DTOMapper.INSTANCE.convertGameInvitationDTOtoEntity(gameInvitationDTO);
        friendService.inviteFriendToGame(userId, gameInvitation);
        return "Request sent";
    }

    // Handle game invitation

    // Delete friend
    @PutMapping("/users/{userId}/friends/delete")
    @ResponseStatus(HttpStatus.OK)
    public String deleteFriends(@PathVariable Long userId, @RequestParam(name = "friendId") Long friendId, @RequestHeader String token) {

        // authenticate user
        userService.authenticateUser(token, userId);

        // add friend by userId
        friendService.deleteFriend(userId, friendId);

        return "You removed a friend successfully.";
    }    

    /*
    get all friends
     */
    @GetMapping("/users/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<FriendGetDTO> getAllUsers(@PathVariable Long userId, @RequestHeader String token) {
        // authorization
        userService.authorizeUser(token, userId);

        // fetch all users in the internal representation
        List<User> friends = friendService.getFriends(userId);
        List<FriendGetDTO> friendGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User friend : friends) {
            friendGetDTOs.add(DTOMapper.INSTANCE.convertEntityToFriendGetDTO(friend));
        }
        return friendGetDTOs;
    }


}
