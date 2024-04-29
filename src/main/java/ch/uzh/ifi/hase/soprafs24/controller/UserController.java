package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
        User userData = userService.loginCredentials(userCredentials);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData);
    }
    /*
      Get by id
     */
    @GetMapping(value = "/users/self/{userId}")
    @ResponseBody
    public UserGetDTO getUserSelfbyID(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        userService.authenticateUser(authorization, userId);
        User userData = userService.getUserbyUserID(userId);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData);
    }

    @GetMapping(value = "/users/other/{userId}")
    @ResponseBody
    public OtherUserGetDTO getOtherUserbyID(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        userService.authorizeUser(authorization);
        User userData = userService.getUserbyUserID(userId);
        return DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(userData);
    }

    /*
      Edit: put
       */
    @PutMapping(value = "/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //请求成功后不返回信息 只有204代码
    @ResponseBody
    public void editUser(@RequestBody EditUserPutDTO editUserPutDTO, @PathVariable("id") Long id) {
        User editUser = DTOMapper.INSTANCE.convertEditUserPutDTOtoEntity(editUserPutDTO);
        editUser.setId(id);
        User edited_user = userService.editUserbyUserID(editUser);
    }

    /*
    Logout: Change status of profile //Why are we returning user information back after client logs out?
     */
    @PutMapping(value = "/users/{userId}/logout")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logoutUser(@PathVariable("userId") Long userId ,@RequestHeader("Authorization") String authorization) {
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
