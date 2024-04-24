package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.IconRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.AchievementRepository;
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
  private final IconRepository iconRepository;


    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository,
                       AchievementRepository achievementRepository,IconRepository iconRepository) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.iconRepository = iconRepository;

    }

    public Icon getDefaultIcon(String defaultIconName) {
        return iconRepository.findByName(defaultIconName);
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
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

    public void authorizeUser(String token){
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        User userByToken = userRepository.findByToken(token);
        if (userByToken == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user with an unauthorized token.");
        }
    }

    // For Authentication
    public void authenticateUser(String token, Long userid){
        User userById = userRepository.findByid(userid);
        // handle token
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (userById == null || !userById.getToken().equals(token)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to user data!");
        }
    }
}
