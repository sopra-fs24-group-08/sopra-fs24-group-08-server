package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IconRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.AchievementRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
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
                       AchievementRepository achievementRepository, IconRepository iconRepository) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.iconRepository = iconRepository;

    }

    public void updateUserAvatar(Long userId, String avatarUrl) {
        System.out.println("Attempting to update avatar for user ID: " + userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Current avatar URL: " + user.getAvatarUrl());

        user.setAvatarUrl(avatarUrl); // 设置新的头像 URL
        System.out.println("New avatar URL set to: " + avatarUrl);

        userRepository.save(user); // 保存更新
        System.out.println("Avatar URL updated successfully in the database for user ID: " + userId);
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
        User userByPassword = userRepository.findByPassword(userToBeCreated.getPassword());
        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null && userByPassword != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format(baseErrorMessage, "username and the name", "are"));
        }
        else if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
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


    public User editUserbyUser(User editedUser) {
        Long id = editedUser.getId();
        User newUser = userRepository.findByid(id); //Check Logic still applies despite DTOs
        if(userRepository.findByUsername(editedUser.getUsername())!=null && (Objects.equals(newUser.getUsername(), editedUser.getUsername()))){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The username you desired is already in use, try a different one.");
        }
        if(!Objects.equals(editedUser.getUsername(), "") & newUser.getUsername() != null) {
            newUser.setUsername(editedUser.getUsername());
        }

        if(editedUser.getBirthday() != null) {
            newUser.setBirthday(editedUser.getBirthday());
        }

        if(newUser.getBirthday()!= null && editedUser.getBirthday()==null){
            newUser.setBirthday(editedUser.getBirthday());
        }

        if(!Objects.equals(editedUser.getPassword(), "") & newUser.getPassword() != null) {
            newUser.setPassword(editedUser.getPassword());
        }

        userRepository.save(newUser);
        userRepository.flush();
        return newUser;
        //We shouldn't be returning s
    }
    public void logoutUserbyUserID(Long userid) {
        // Input: user id
        // Function: Change online status to offline
        // Return: Edited user information
        User userbyID = userRepository.findByid(userid);
        userbyID.setStatus(UserStatus.OFFLINE);
    }

    public void unlockIconUser(Long userId, Long iconId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Icon icon = iconRepository.findById(iconId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Icon not found"));

        user.addIcon(icon);
        userRepository.save(user);
    }

    //will either scrap the editUser yet again or add extra rest controller so I don't have to waste time refactoring tests of others.
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

    public void authorizeUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User userByToken = userRepository.findByToken(token);
        if (userByToken == null) {
            System.out.println("problematic"+ token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user with an unauthorized token.");
        }
    }
    //only for ws
    public boolean validateUserIdToken(Long userId, String token) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getToken().equals(token);
    }

    // For Authentication, was for handshake, now using for edit, feel free to refactor later.
    public void authenticateUser(String token, Long userid) {
        User userById = userRepository.findByid(userid);
        // handle token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (userById == null || !userById.getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No access to user data!");
        }
    }
    /**
     * Retrieves a specific friend's User object for a given user.
     *
     * @param userId The ID of the user whose friend list is to be checked.
     * @param friendId The ID of the friend to be retrieved.
     * @return The friend User object if found, otherwise null.
     */
    public User getSpecificFriend(Long userId, Long friendId) {
        List<User> friends = userRepository.findFriendsByUserId(userId);
        return friends.stream()
                .filter(friend -> friend.getId().equals(friendId))
                .findFirst()
                .orElse(null);
    }

    public UserGetDTO getSpecificFriendDTO(Long userId, Long friendId) {
        User friend = getSpecificFriend(userId, friendId); // Your existing method
        return mapToDTO(friend);
    }
    private UserGetDTO mapToDTO(User user) {
        UserGetDTO dto = new UserGetDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setStatus(user.getStatus());
        dto.setCreation_date(user.getCreation_date());
        dto.setBirthday(user.getBirthday());
        dto.setCurrIcon(user.getCurrIcon());
        return dto;
    }

    // For WS Handshake , would prefer boolean return
    public boolean checkTokenValidity(String token) {
        authorizeUser(token);

        return true;
    }

}
