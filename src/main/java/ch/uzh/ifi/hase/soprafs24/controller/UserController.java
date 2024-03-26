package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
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

  /*
  get all users
   */
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
  @PostMapping("/login")
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
  @GetMapping(value = "/users/{id}")
  @ResponseBody
  public UserGetDTO getUserbyID(@PathVariable("id") Long id) {
      User userData = userService.getUserbyUserID(id);
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userData);
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
    Logout: Change status of profile
     */
  @PutMapping(value = "/logout/{id}")
  @ResponseBody
  public LogoutUserGetDTO logoutUser(@PathVariable("id") Long id) {
      User loggedUser = userService.logoutUserbyUserID(id);
      return DTOMapper.INSTANCE.convertEntityToLogoutUserGetDTO(loggedUser);
  }

}
