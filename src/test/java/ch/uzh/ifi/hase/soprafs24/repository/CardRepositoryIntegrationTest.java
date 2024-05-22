package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class CardRepositoryIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void findByPlayerId_success() {
        // Create and save user
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setCreation_date(LocalDate.now());
        user.setToken("testToken");
        user.setStatus(ONLINE);
        user = userRepository.saveAndFlush(user);

        // Create and save game
        Game game = new Game();
        game.setGameStatus(GameStatus.STARTING);
        game = gameRepository.saveAndFlush(game);

        // Create and save player
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        player = playerRepository.saveAndFlush(player);

        // Create and save cards
        Card card1 = new Card("red", 5);
        card1.setPlayer(player);
        card1 = cardRepository.saveAndFlush(card1);

        Card card2 = new Card("blue", 3);
        card2.setPlayer(player);
        card2 = cardRepository.saveAndFlush(card2);

        // Find cards by player ID
        List<Card> foundCards = cardRepository.findByPlayerId(player.getId());

        // Assertions
        assertNotNull(foundCards);
        assertEquals(2, foundCards.size());
        assertTrue(foundCards.stream().anyMatch(card -> card.getColor().equals("red")));
        assertTrue(foundCards.stream().anyMatch(card -> card.getColor().equals("blue")));
    }

    @AfterEach
    public void teardown() {
        cardRepository.deleteAll();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }
}
