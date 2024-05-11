package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    /*
    get all users
     */
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<OtherUserGetDTO> getAllUsers(@RequestHeader("Authorization") String authorization) {
        //authorization
        userService.authorizeUser(authorization);
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<OtherUserGetDTO> userGetDTOs = new ArrayList<>();
        System.out.println(users);

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(user));
        }
        return userGetDTOs;
    }

    /*
    Register
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        User createdUser = userService.createUser(userInput);
        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }
    /*
      Login: Post API to login user
     */
    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody LoginUserPostDTO loginUserPostDTO) {
        User userCredentials = DTOMapper.INSTANCE.convertLoginUserPostDTOtoEntity(loginUserPostDTO);
        System.out.println("credentials"+userCredentials);
        User userData = userService.loginCredentials(userCredentials);
        System.out.println("credentials"+userData);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData);
    }
    /*
      Get by id
     */
    //Properly fix all the HTTP codes so they match up
    @GetMapping(value = "/users/{userId}/{profileId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public ResponseEntity<?> getUserById(@RequestHeader("Authorization") String authorization, @PathVariable Long userId, @PathVariable Long profileId) {
        userService.authenticateUser(authorization, userId);

        User userData = userService.getUserbyUserID(Objects.equals(userId, profileId) ? userId : profileId);
        System.out.println(userData + " User Data");

        return ResponseEntity.accepted().body(Objects.equals(userId, profileId) ?
                DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData) :
                DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(userData));
    }


    @GetMapping(value = "/users/{userId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public UserGetDTO getMyProfile(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        userService.authenticateUser(authorization, userId);
        User userData = userService.getUserbyUserID(userId);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData);
    }


    @GetMapping(value = "/users/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public ResponseEntity<UserGetDTO> getFriendDTO(@RequestHeader("Authorization") String authorization, @PathVariable Long userId, @PathVariable Long friendId) {
        userService.authenticateUser(authorization, userId);
        UserGetDTO friendDTO = userService.getSpecificFriendDTO(userId, friendId);
        if (friendDTO != null) {
            return ResponseEntity.ok(friendDTO);
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping(value = "/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //请求成功后不返回信息 只有204代码
    @ResponseBody
    public void editUser(@RequestBody EditUserPutDTO editUserPutDTO, @PathVariable("userId") Long userId,@RequestHeader("Authorization") String authorization) {
        userService.authenticateUser(authorization,userId);
        User editUser = DTOMapper.INSTANCE.convertEditUserPutDTOtoEntity(editUserPutDTO);
        editUser.setId(userId);
        userService.editUserbyUser(editUser);
    }

    /*
    Logout: Change status of profile //Why are we returning user information back after client logs out?
     */
    @PutMapping(value = "/users/{userId}/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void logoutUser(@PathVariable("userId") Long userId ,@RequestHeader("Authorization") String authorization) {
        System.out.println("User with the following userId and token is trying t log out:"+userId+authorization);
        userService.authenticateUser(authorization, userId);
        userService.logoutUserbyUserID(userId);
    }

    // authenticate user
    @GetMapping(value = "/users/authenticate/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean authenticateUser(@RequestHeader("Authorization") String authorization, @PathVariable Long userId){
        userService.authenticateUser(authorization, userId);
        return true;
    }


    @GetMapping("/users/{userId}/achievements")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Set<Achievement> getUserAchievements(@PathVariable Long userId) {
        // Fetch the user by ID
        User user = userService.getUserbyUserID(userId);
        return user.getAchievements();
  }





}
