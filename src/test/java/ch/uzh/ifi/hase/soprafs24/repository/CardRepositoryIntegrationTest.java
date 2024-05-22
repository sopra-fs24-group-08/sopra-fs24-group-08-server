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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class CardRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    @Test
    public void findByPlayerId_success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setCreation_date(LocalDate.now());
        user.setToken("testToken");
        user.setStatus(ONLINE);

        Game game = new Game();
        game.setGameStatus(GameStatus.STARTING);

        Player player = new Player();
        player.setUser(user);
        player.setGame(game);

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(game);
        entityManager.persistAndFlush(player);

        Card card1 = new Card("red", 5);
        card1.setPlayer(player);

        Card card2 = new Card("blue", 3);
        card2.setPlayer(player);

        entityManager.persistAndFlush(card1);
        entityManager.persistAndFlush(card2);

        List<Card> foundCards = cardRepository.findByPlayerId(player.getId());

        assertNotNull(foundCards);
        assertEquals(2, foundCards.size());
        assertTrue(foundCards.stream().anyMatch(card -> card.getColor().equals("red")));
        assertTrue(foundCards.stream().anyMatch(card -> card.getColor().equals("blue")));
    }

    @AfterEach
    public void teardown() {
        cardRepository.deleteAll();
    }
}
