package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserEditDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

  //isn't even protected never was, leave it like this?
  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }

    return userGetDTOs;
  }


  @GetMapping(value = "/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public  UserGetDTO getUserById(@RequestHeader("Authorization") String token,@PathVariable long userId) {


      User user = userService.getUserById(userId);

      // fetch all users in the internal representation
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }


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


  @PutMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUser(@RequestHeader("Authorization") String token,@RequestBody UserEditDTO userEditDTO, @PathVariable Long userId){
      userService.authToken(token);
      userService.editUser(userId, userEditDTO,token);
      //really not return anything?
      
  }
  @PutMapping("/users/{userId}/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logout(@RequestHeader("Authorization") String token,@PathVariable Long userId){
      userService.authToken(token);
      userService.logout(userId);
    }

}
