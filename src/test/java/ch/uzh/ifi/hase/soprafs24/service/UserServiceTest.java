package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.IconRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IconRepository iconRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");

        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        when(userRepository.save(Mockito.any())).thenReturn(testUser);
        when(iconRepository.findByName("Default Icon")).thenReturn(new Icon());
    }

    @Test
    public void createUser_validInputs_success() {
    when(userRepository.findByUsername(anyString())).thenReturn(null);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    User createdUser = userService.createUser(testUser);
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
    when(userRepository.findByUsername("testUser")).thenReturn(testUser);

    Exception exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    assertEquals(HttpStatus.CONFLICT, ((ResponseStatusException) exception).getStatus());
    }

    @Test
    public void loginCredentials_CorrectCredentials_ReturnUser() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User loggedInUser = userService.loginCredentials(testUser);

        assertNotNull(loggedInUser);
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
    }
    @Test
    public void loginCredentials_WrongUsername_ThrowsException() {
        when(userRepository.findByUsername("wrongUsername")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userService.loginCredentials(testUser);
        });

        assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatus());
    }
    @Test
    public void loginCredentials_WrongPassword_ThrowsException() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
        testUser.setPassword("wrongPassword");

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userService.loginCredentials(testUser);
        });

        assertEquals(HttpStatus.NOT_ACCEPTABLE, ((ResponseStatusException) exception).getStatus());
    }

    @Test
    public void getUserbyUserID_ValidId_ReturnUser() {
        when(userRepository.findByid(1L)).thenReturn(testUser);

        User foundUser = userService.getUserbyUserID(1L);

        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        verify(userRepository, times(1)).findByid(1L);
    }

    @Test
    public void getUserbyUserID_InvalidId_ThrowsException() {
        when(userRepository.findByid(1L)).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserbyUserID(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatus());
    }

    @Test
    public void editUserbyUser_ValidInfo_ReturnUpdatedUser() {
        when(userRepository.findByid(testUser.getId())).thenReturn(testUser);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.editUserbyUser(testUser);

        assertNotNull(updatedUser);
        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void editUserbyUser_UsernameExists_ThrowsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("existingUsername");

        when(userRepository.findByid(testUser.getId())).thenReturn(testUser);
        when(userRepository.findByUsername("existingUsername")).thenReturn(anotherUser);
        testUser.setUsername("existingUsername");

        assertThrows(ResponseStatusException.class, () -> {
            userService.editUserbyUser(testUser);
        });
    }

    @Test
    public void logoutUserbyUserID_success() {

        when(userRepository.findByid(testUser.getId())).thenReturn(testUser);

        userService.logoutUserbyUserID(testUser.getId());

        verify(userRepository).findByid(testUser.getId());
        assertEquals(UserStatus.OFFLINE, testUser.getStatus(), "User status should be set to OFFLINE");
    }
    @Test
    public void authorizeUser_ValidToken_Success() {
        String token = "Bearer validToken123";
        when(userRepository.findByToken("validToken123")).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.authorizeUser(token));
    }

    @Test
    public void authorizeUser_InvalidToken_ThrowsException() {
        String token = "Bearer invalidToken123";
        when(userRepository.findByToken("invalidToken123")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userService.authorizeUser(token);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ((ResponseStatusException) exception).getStatus());
    }

    @Test
    public void authenticateUser_ValidCredentials_Success() {
        String token = "Bearer validToken123";
        Long userId = 1L;
        when(userRepository.findByid(userId)).thenReturn(testUser);
        testUser.setToken("validToken123");

        assertDoesNotThrow(() -> userService.authenticateUser(token, userId));
    }

    @Test
    public void authenticateUser_InvalidToken_ThrowsException() {
        String token = "Bearer invalidToken123";
        Long userId = 1L;
        when(userRepository.findByid(userId)).thenReturn(testUser);
        testUser.setToken("validToken123");

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userService.authenticateUser(token, userId);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ((ResponseStatusException) exception).getStatus());
    }

    @Test
    public void getSpecificFriend_Found_Success() {
        Long userId = 1L, friendId = 2L;
        User friend = new User();
        friend.setId(friendId);
        List<User> friends = List.of(friend);

        when(userRepository.findFriendsByUserId(userId)).thenReturn(friends);

        User result = userService.getSpecificFriend(userId, friendId);

        assertNotNull(result);
        assertEquals(friendId, result.getId());
    }

    @Test
    public void getSpecificFriend_NotFound_ReturnsNull() {
        Long userId = 1L, friendId = 2L;
        when(userRepository.findFriendsByUserId(userId)).thenReturn(Collections.emptyList());

        User result = userService.getSpecificFriend(userId, friendId);

        assertNull(result);
    }

}
