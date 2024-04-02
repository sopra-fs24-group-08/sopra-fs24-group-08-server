package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserEditDTO;
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
import java.util.Objects;
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

    public User getUserById(Long userId) {
      //special error bcs of how userid apparently has to be handled...?
        checkIDValidity(userId);
        return userRepository.findById(userId).orElse(null);
    }

    private User getUserByToken(String token){
      return userRepository.findByToken(token);
    }


  public User createUser(User newUser) {
    String Token = UUID.randomUUID().toString();
    newUser.setToken(Token);
    //Should the user also be logged in if I simply create the profile over postman e.g?
    newUser.setStatus(UserStatus.ONLINE);
    checkIfUserExists(newUser);
    newUser.setCreation_date(LocalDate.now());
    newUser.setBirthday(null);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  //Registration
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    User userByPassword = userRepository.findByPassword(userToBeCreated.getPassword());
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    //Changed after M1 interview, since they specifically asked for this method, makes sense why they were interested in it,
      //Had logical error in it because I never bothered with adapting the template stuff again after first week
      //if (userByUsername != null && userByPassword != null) {
      if ((userByPassword != null) && (userByPassword == userByUsername)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          String.format(baseErrorMessage, "username and the password", "are"));
    } else if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
    }
  }

  private void checkIDValidity(Long userId){
      String baseErrorMessage = "User with %s was not found";
      if (!userRepository.existsById(userId)){
          throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                  String.format(baseErrorMessage,userId));
      }
  }
  //Doing extra work, does it even matter security wise if I do private/public? If combine some of the methods later.
  private void checkUserCredentials(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        //User userByPassword = userRepository.findByPassword(userToBeCreated.getPassword());
        String baseErrorMessage = "Something went wrong, %s";
        if (userByUsername == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage,"username is not associated with any account"));
        }
    }

    public void authToken(String token){
        String baseErrorMessage = "Something went wrong, %s";
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage,"Authorization header is missing or empty"));
        }
        User authUser = userRepository.findByToken(token);
        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    String.format(baseErrorMessage,"authorization failed"));
        }
    }

    public String checkIfValid(User userCredentials){
        //Authenticate first and if its fine give back token
        checkUserCredentials(userCredentials);
        User userByUsername = userRepository.findByUsername(userCredentials.getUsername());
        // neeeed for token userByUsername.getToken()
        if (userByUsername.getPassword().equals(userCredentials.getPassword())){
            userByUsername.setStatus(UserStatus.ONLINE);
        }
        return userByUsername.getToken();
    }

    public User loginAuth(User userToBeAuthenticated){
    User userByUsername = userRepository.findByUsername(userToBeAuthenticated.getUsername());
    String baseErrorMessage = "Something went wrong, %s";
    if (userByUsername == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                String.format(baseErrorMessage,"username is not associated with any account"));
    }
    if (!userByUsername.getPassword().equals(userToBeAuthenticated.getPassword())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                String.format(baseErrorMessage,"invalid login credentials!"));}

        if (userByUsername.getStatus() == UserStatus.ONLINE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    String.format(baseErrorMessage,"account is already logged in on another device"));
        }
        userByUsername.setStatus(UserStatus.ONLINE);
        userRepository.save(userByUsername);
        userRepository.flush();
        return userByUsername; }

    public void logout(Long userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.OFFLINE);
        userRepository.save(user);
        userRepository.flush();
    }



    public void editUser(Long userId, UserEditDTO userEditDTO,String token) {
        User user = getUserById(userId);
        if(user != getUserByToken(token))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                    ,"Authorization for updating the profile failed");

        if(userRepository.findByUsername(userEditDTO.getUsername())!=null && !Objects.equals(user.getUsername(), userEditDTO.getUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The username you desired is already in use, try a different one.");
        }
        if(!Objects.equals(userEditDTO.getUsername(), "") & userEditDTO.getUsername() != null) {
            user.setUsername(userEditDTO.getUsername());
        }

        if(userEditDTO.getBirthday() != null) {
            user.setBirthday(userEditDTO.getBirthday());
        }

        if(user.getBirthday()!= null && userEditDTO.getBirthday()==null){
            user.setBirthday(userEditDTO.getBirthday());
        }

        userRepository.save(user);
        userRepository.flush();
        }

}


