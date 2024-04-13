package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
      return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    LocalDate date = LocalDate.now();
    newUser.setCreation_date(date);
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */

  /*private void checkIfUserExists(User userToBeCreated) {
      // This is a helper method. It checks uniqueness of username.
      User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

      String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
      if (userByUsername != null) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
      }
  }*/
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    User userByName = userRepository.findByName(userToBeCreated.getName());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null && userByName != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(baseErrorMessage, "username and the name", "are"));
    } else if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    } else if (userByName != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "name", "is"));
    }
  }
  public User loginCredentials(User user) {
      // This method check if username and password provided by user is correct.
      // Throws exception in case of discrepancies.
      // If username, password correct, returns user information.
      String username = user.getUsername();
      String password = user.getPassword();
      User userByUsername = userRepository.findByUsername(username);

      String uniqueErrorMessage = "%s username not found. Please register!";
      if (userByUsername == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(uniqueErrorMessage, username));
      }

      String savedPassword = userByUsername.getPassword();

      String passwordErrorMessage = "Password incorrect! Try again!";
      if (!password.equals(savedPassword)) {
          throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(passwordErrorMessage));
      }
      userByUsername.setStatus(UserStatus.ONLINE);
      return userByUsername;
  }
  public User getUserbyUserID(Long id) {
      User userById = userRepository.findByid(id);

      String uniqueErrorMessage = "User with id %s not found!";
      if (userById == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(uniqueErrorMessage, id));
      }
      return userById;
    }

  public User editUserbyUserID(User user) {

      Long userid = user.getId();
      String username = user.getUsername();
      LocalDate birthday = user.getBirthday();
      String password = user.getPassword();
      User userbyID = userRepository.findByid(userid);

      String notFoundErrorMessage = "User with user id %s not found!";
      if (userbyID == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(notFoundErrorMessage, userid));
      }

      String uniqueErrorMessage = "Username already exist";
      User existingUser = userRepository.findByUsername(username);

      if (existingUser != null && !existingUser.getId().equals(userid)) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, uniqueErrorMessage);
      }
      if (username != null) {                 //如果没有被输入，那么返回的就是null，这里就不会进行编辑
            userbyID.setUsername(username);
      }
      if (birthday != null) {
            userbyID.setBirthday(birthday);
      }
      if (password != null) {
          userbyID.setPassword(password);
      }
      return userbyID;
    }
    public User logoutUserbyUserID(Long userid) {
        // Input: user id
        // Function: Change online status to offline
        // Return: Edited user information
        User userbyID = userRepository.findByid(userid);
        userbyID.setStatus(UserStatus.OFFLINE);
        return userbyID;
    }

    //above are all saved from M1

    public void authenticateUser(String token, Long userid){
        User userById = userRepository.findByid(userid);
        // handle token
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (!userById.getToken().equals(token)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to user data!");
        }
    }

    // For Authorization
    public void authorizeUser(String token){
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        User userByToken = userRepository.findByToken(token);
        if (userByToken == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user with an unauthorized token.");
        }
    }


}
