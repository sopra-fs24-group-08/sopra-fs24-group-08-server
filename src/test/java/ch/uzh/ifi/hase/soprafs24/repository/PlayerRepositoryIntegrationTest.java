package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs24.constant.GameStatus.STARTING;
import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class PlayerRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @AfterEach
    public void teardown() {
        playerRepository.deleteAll();
    }

    @Test
    public void findByUser_success() {
        // Create and persist a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setCreation_date(LocalDate.now());
        user.setToken("testtoken");
        user.setStatus(ONLINE);
        entityManager.persist(user);
        entityManager.flush();

        // Create and persist a player
        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        // Create and persist a player
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        entityManager.persist(player);
        entityManager.flush();

        // Find the player by user
        Player foundPlayer = playerRepository.findByUser(user);
        assertNotNull(foundPlayer);
        assertEquals("testuser", foundPlayer.getUser().getUsername());
    }

    @Test
    public void findUsernameByPlayerId_success() {
        // Create and persist a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setCreation_date(LocalDate.now());
        user.setToken("testtoken");
        user.setStatus(ONLINE);
        entityManager.persist(user);
        entityManager.flush();

        // Create and persist a player
        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        // Create and persist a player
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        entityManager.persist(player);
        entityManager.flush();

        // Find the username by player ID
        String username = playerRepository.findUsernameByPlayerId(player.getId());
        assertNotNull(username);
        assertEquals("testuser", username);
    }

    @Test
    public void findPlayersByGameIdOrderedByScore_success() {
        // Create and persist users
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setPassword("testpassword1");
        user1.setCreation_date(LocalDate.now());
        user1.setToken("testtoken1");
        user1.setStatus(ONLINE);
        entityManager.persist(user1);
        entityManager.flush();

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setPassword("testpassword2");
        user2.setCreation_date(LocalDate.now());
        user2.setToken("testtoken2");
        user2.setStatus(ONLINE);
        entityManager.persist(user2);
        entityManager.flush();

        // Create and persist players
        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        // Create and persist a player
        Player player1 = new Player();
        player1.setUser(user1);
        player1.setGame(game);
        player1.setScore(10);
        entityManager.persist(player1);
        entityManager.flush();

        Player player2 = new Player();
        player2.setUser(user2);
        player2.setGame(game);
        player2.setScore(20);
        entityManager.persist(player2);
        entityManager.flush();

        List<Player> players = playerRepository.findPlayersByGameIdOrderedByScore(player1.getGame().getGameId());

        assertEquals("testuser2", players.get(0).getUser().getUsername());
        assertEquals("testuser1", players.get(1).getUser().getUsername());
    }

    /*@Test
    public void findByUser_notFound() {

        User existingUser = new User();
        existingUser.setUsername("existent");
        existingUser.setPassword("testpassword");
        existingUser.setCreation_date(LocalDate.now());
        existingUser.setToken("testtoken");
        existingUser.setStatus(ONLINE);

        entityManager.persist(existingUser);
        entityManager.flush();

        Game game = new Game();
        game.setGameStatus(STARTING);
        game.setCardPileSize(0);

        entityManager.persist(game);
        entityManager.flush();

        Player player = new Player();
        player.setScore(100);
        player.setUser(existingUser);
        player.setGame(game);

        entityManager.persist(player);
        entityManager.flush();

        // Create a new User object with a different username (ensuring it’s not in the database)
        User nonExistentUser = new User();
        nonExistentUser.setUsername("nonexistent");
        nonExistentUser.setPassword("testpassword");
        nonExistentUser.setCreation_date(LocalDate.now());
        nonExistentUser.setToken("testtoken");
        nonExistentUser.setStatus(ONLINE);

        // Perform the search using the nonExistentUser
        Player foundPlayer = playerRepository.findByUser(nonExistentUser);

        // Assert that no Player is found
        assertNull(foundPlayer);
    } */

    @Test
    public void findUsernameByPlayerId_notFound() {
        // Attempt to find a username by a player ID that does not exist
        String username = playerRepository.findUsernameByPlayerId(999L); // Assuming 999 is not a valid ID
        assertNull(username);
    }

    @Test
    public void findPlayersByGameIdOrderedByScore_notFound() {
        // Attempt to find players by a game ID that does not exist
        List<Player> players = playerRepository.findPlayersByGameIdOrderedByScore(999L); // Assuming 999 is not a valid game ID
        assertTrue(players.isEmpty());
    }
}
