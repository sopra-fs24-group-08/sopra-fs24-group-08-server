package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRepositoryIntegrationTest {

    // @Autowired
    // private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsername_success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("TestPassword");
        user.setToken("UniqueToken123");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreation_date(LocalDate.now());

        // entityManager.persist(user);
        // entityManager.flush();
        userRepository.save(user);
        userRepository.flush();

        User found = userRepository.findByUsername(user.getUsername());

        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
        userRepository.delete(user);
    }

    @Test
    public void findByid_success() {
        User user = new User();
        user.setUsername("testUser2");
        user.setPassword("AnotherPassword");
        user.setToken("AnotherToken123");
        user.setStatus(UserStatus.ONLINE);
        user.setCreation_date(LocalDate.now());

        // entityManager.persist(user);
        // entityManager.flush();
        userRepository.save(user);
        userRepository.flush();

        User found = userRepository.findByid(user.getId());

        assertNotNull(found);
        assertEquals(user.getId(), found.getId());
        userRepository.delete(user);
    }

    @Test
    public void findFriendsByUserId_success() {
        User user = new User();
        user.setUsername("user1");
        user.setPassword("Password1");
        user.setToken("Token1");
        user.setStatus(UserStatus.ONLINE);
        user.setCreation_date(LocalDate.now());

        User friend = new User();
        friend.setUsername("friend1");
        friend.setPassword("Password2");
        friend.setToken("Token2");
        friend.setStatus(UserStatus.OFFLINE);
        friend.setCreation_date(LocalDate.now());

        user.getFriends().add(friend);

        // entityManager.persist(friend);
        // entityManager.persist(user);
        // entityManager.flush();
        userRepository.save(friend);
        userRepository.save(user);
        userRepository.flush();

        List<User> friends = userRepository.findFriendsByUserId(user.getId());

        assertFalse(friends.isEmpty());
        assertEquals(friend.getId(), friends.get(0).getId());
        userRepository.delete(user);
        userRepository.delete(friend);
    }

    @Test
    public void findByToken_success() {
        User user = new User();
        user.setUsername("userTokenTest");
        user.setPassword("Password3");
        user.setToken("SecureToken456");
        user.setStatus(UserStatus.ONLINE);
        user.setCreation_date(LocalDate.now());

        // entityManager.persist(user);
        // entityManager.flush();
        userRepository.save(user);
        userRepository.flush();

        User found = userRepository.findByToken(user.getToken());

        assertNotNull(found);
        assertEquals(user.getToken(), found.getToken());
        userRepository.delete(user);
    }

    @Test
    public void existsByUserIdAndToken_success() {
        User user = new User();
        user.setUsername("checkExistence");
        user.setPassword("Password4");
        user.setToken("ExistToken12345");
        user.setStatus(UserStatus.ONLINE);
        user.setCreation_date(LocalDate.now());

        // entityManager.persist(user);
        // entityManager.flush();
        userRepository.save(user);
        userRepository.flush();

        boolean exists = userRepository.existsByUserIdAndToken(user.getId(), user.getToken());

        assertTrue(exists);
        userRepository.delete(user);
    }
}

