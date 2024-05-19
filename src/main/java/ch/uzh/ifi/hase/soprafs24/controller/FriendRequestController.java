package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    // Delete friend
    @PutMapping("/users/{userId}/friends/delete")
    @ResponseStatus(HttpStatus.OK)
    public List<OtherUserGetDTO> deleteFriends(@PathVariable Long userId, @RequestParam("FriendId") Long friendId, @RequestHeader("Authorization") String authorization) {
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
    public List<OtherUserGetDTO> getAllUsers(@PathVariable Long userId, @RequestHeader("Authorization") String authorization) {
        // authorization
        System.out.println(authorization);
        userService.authorizeUser(authorization);

        // fetch all users in the internal representation
        //List<User> friends = friendService.getFriends(userId);
        List<User> friends = friendService.getFriendsQuery(userId);
        List<OtherUserGetDTO> friendGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User friend : friends) {
            friendGetDTOs.add(DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(friend));
        }
        return friendGetDTOs;
    }

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List <FriendRequestDTO> getAllRequests(@PathVariable Long userId, @RequestHeader("Authorization") String authorization) {
        userService.authenticateUser(authorization, userId);
        return friendService.provideAllPendingRequest(userId);

    }


}