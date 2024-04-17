package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IconRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.AchievementRepository;
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
import java.util.*;

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
  private final AchievementRepository achievementRepository;
  private final FriendRequestRepository friendRequestRepository;
  private final IconRepository iconRepository;


    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository,
                       AchievementRepository achievementRepository, FriendRequestRepository friendRequestRepository, IconRepository iconRepository) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.iconRepository = iconRepository;

    }
    public Icon getDefaultIcon(String defaultIconName) {
        return iconRepository.findByName(defaultIconName);
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
    newUser.setStatus(UserStatus.ONLINE);
    checkIfUserExists(newUser);
    newUser.setCreation_date(LocalDate.now());
    newUser.setBirthday(null);
    Icon defaultIcon = getDefaultIcon("Default Icon");
    if (defaultIcon != null) {
        newUser.setCurrIcon(defaultIcon); // Set the current icon to the default
        newUser.addIcon(defaultIcon); // Add the default icon to the user's collection
    } else {
        log.warn("Default icon not found.");
    }
    //No clue about Optional.ofNullable, IDE recommend and it works
    Optional<Achievement> achievementOptional = Optional.ofNullable(achievementRepository.findById(1L));
    Optional<Achievement> achievementOptional2 = Optional.ofNullable(achievementRepository.findById(7L));



      if (achievementOptional.isPresent() && achievementOptional2.isPresent()) {
          // If the achievement is found, add it to the new user
          Achievement predefinedAchievement = achievementOptional.get();

          newUser.addAchievement(predefinedAchievement);
          Achievement predefinedAchievementTest = achievementOptional2.get();
          newUser.addAchievement(predefinedAchievementTest);

      } else {
          // Handle the case where the achievement is not found
          log.error("Predefined achievement not found. User created without this achievement.");
          // Optionally, throw an exception or take other actions as needed
      }

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
  private void checkUserCredentials(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
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
        checkUserCredentials(userCredentials);
        User userByUsername = userRepository.findByUsername(userCredentials.getUsername());
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

    public void unlockIconUser(Long userId, Long iconId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Icon icon = iconRepository.findById(iconId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Icon not found"));

        user.addIcon(icon);
        userRepository.save(user);
    }

    public Icon chooseIconUser(Long userId, Long iconId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Icon iconToSelect = iconRepository.findById(iconId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Icon not found"));

        if (!user.getIcons().contains(iconToSelect)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This icon is not available for the user.");
        }

        user.setCurrIcon(iconToSelect);
        userRepository.save(user);

        return iconToSelect;
    }

    public void addFriend(Long userId, String friendUsername) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User friend = userRepository.findByUsername(friendUsername);
        if (friend == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found");
        }
        user.addFriend(friend);
        userRepository.save(user);
    }

    public Set<User> getFriends(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getFriends();
    }

    /*public void sendFriendRequest(Long senderId, Long recipientId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> recipientOpt = userRepository.findById(recipientId);

        if (!senderOpt.isPresent() || !recipientOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender or recipient not found");
        }

        User sender = senderOpt.get();
        User recipient = recipientOpt.get();

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(sender);
        friendRequest.setRecipient(recipient);
        friendRequestRepository.save(friendRequest);
    }

    public void acceptFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(requestId);

        if (!friendRequestOpt.isPresent()) {
            // Handle "not found" scenario
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found");
        }

        FriendRequest friendRequest = friendRequestOpt.get();
        friendRequest.setAccepted(true);

        User sender = friendRequest.getSender();
        User recipient = friendRequest.getRecipient();
        sender.addFriend(recipient); // Assuming addFriend() method is defined in User
        userRepository.save(sender);
    }

    public void declineFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findById(requestId);

        if (!friendRequestOpt.isPresent()) {
            // Handle "not found" scenario
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found");
        }

        FriendRequest friendRequest = friendRequestOpt.get();
        friendRequest.setAccepted(false);
        friendRequestRepository.save(friendRequest);
    }
    public List<FriendRequest> getPendingFriendRequestsForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found.");
            // Handle this error appropriately
            return new ArrayList<>();
        }
        return friendRequestRepository.findByRecipientAndAcceptedIsNull(user);
    }
    // Might come in handy, for when a user sends frequest and immediately refreshes friendlist.
    public List<FriendRequest> getResolvedFriendRequestsForUser(Long userId, Boolean accepted) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found.");
            return new ArrayList<>();
        }
        return friendRequestRepository.findByRecipientAndAccepted(user, accepted);
    }*/

}


