package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import org.springframework.http.HttpStatus;
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
    public FriendGetDTO addFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO, @RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        userService.authenticateUser(authorization, userId);
        FriendRequest friendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        User friend = friendService.addFriendRequest(userId, friendRequest);
        return DTOMapper.INSTANCE.convertEntityToFriendGetDTO(friend);
    }

    // Long-polling to check for adding friend requests/response and game invitation request/response
    @GetMapping("/users/{userId}/polling")
    @ResponseStatus(HttpStatus.OK)
    public DeferredResult<List<FriendRequestDTO>> pollFriendRequest(@PathVariable Long userId, @RequestHeader("Authorization") String authorization) {
        userService.authenticateUser(authorization, userId);
        DeferredResult<List<FriendRequestDTO>> deferredResult = new DeferredResult<>(GlobalConstants.POLL_TIMEOUT); // 5 seconds timeout
        friendService.pollUpdates(deferredResult, userId);
        return deferredResult;
    }

    // Handle friend request
    @PostMapping("/users/{userId}/friendresponse")
    @ResponseStatus(HttpStatus.OK)
    public FriendRequestDTO handleFriendRequest(@PathVariable Long userId, @RequestBody FriendRequestDTO friendRequestDTO, @RequestHeader("Authorization") String authorization){
        userService.authenticateUser(authorization, userId);
        // turn DTO to entity
        FriendRequest receivedFriendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        //
        FriendRequest friendRequest = friendService.handleFriendRequest(userId, receivedFriendRequest);
        return friendService.convertEntityToFriendRequestDTO(friendRequest);
    }

    //Invite friend into game
    @PostMapping("/game/invite/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestDTO gameInvitation(@RequestBody FriendRequestDTO gameInvitationDTO, @RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        userService.authenticateUser(authorization, userId);
        System.out.println("FriendInvSend");
        FriendRequest gameInvitation = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(gameInvitationDTO);
        FriendRequest updateGameInvitation = friendService.inviteFriendToGame(userId, gameInvitation);
        return friendService.convertEntityToFriendRequestDTO(updateGameInvitation);
    }

    // Handle game invitation
    @PostMapping("/game/{userId}/invitationresponse")
    @ResponseStatus(HttpStatus.OK)
    public GameMatchResultDTO handleGameInvitation(@PathVariable Long userId, @RequestBody FriendRequestDTO friendRequestDTO, @RequestHeader("Authorization") String authorization){
        userService.authenticateUser(authorization, userId);
        System.out.println("FriendInvResponse");
        // turn DTO to entity
        FriendRequest receivedFriendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        //
        GameMatchResultDTO gameMatchResultDTO = friendService.handleGameInvitation(userId, receivedFriendRequest);
        return gameMatchResultDTO;
    }

    // Delete friend
    @PutMapping("/users/{userId}/friends/delete")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendGetDTO> deleteFriends(@PathVariable Long userId, @RequestParam("FriendId") Long friendId, @RequestHeader("Authorization") String authorization) {

        // authenticate user
        userService.authenticateUser(authorization, userId);

        // delete friend by userId
        friendService.deleteFriend(userId, friendId);

        // get friendlist

        return getAllUsers(userId, authorization);
    }

    /*
    get all friends
     */
    @GetMapping("/users/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<FriendGetDTO> getAllUsers(@PathVariable Long userId, @RequestHeader("Authorization") String authorization) {
        // authorization
        System.out.println(authorization);
        userService.authorizeUser(authorization);

        // fetch all users in the internal representation
        //List<User> friends = friendService.getFriends(userId);
        List<User> friends = friendService.getFriendsQuery(userId);
        List<FriendGetDTO> friendGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User friend : friends) {
            friendGetDTOs.add(DTOMapper.INSTANCE.convertEntityToFriendGetDTO(friend));
        }
        return friendGetDTOs;
    }


}