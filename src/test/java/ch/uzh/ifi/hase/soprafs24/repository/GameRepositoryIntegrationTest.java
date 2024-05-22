package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
public class GameRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GameRepository gameRepository;

    @AfterEach
    public void teardown() {
        gameRepository.deleteAll();
    }

    @Test
    public void findByWinnerId_success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setToken("testToken");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreation_date(LocalDate.now());
        entityManager.persist(user);
        entityManager.flush();

        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        Player winner = new Player();
        winner.setUser(user);
        winner.setGame(game);
        entityManager.persist(winner);
        entityManager.flush();

        game.setWinner(winner);
        entityManager.persist(game);
        entityManager.flush();

        List<Game> foundGames = gameRepository.findByWinnerId(winner.getId());

        assertNotNull(foundGames);
        assertEquals(1, foundGames.size());
        assertEquals(winner.getId(), foundGames.get(0).getWinner().getId());
    }

    @Test
    public void findByLoserId_success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setToken("testToken");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreation_date(LocalDate.now());
        entityManager.persist(user);
        entityManager.flush();

        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        Player loser = new Player();
        loser.setUser(user);
        loser.setGame(game);
        entityManager.persist(loser);
        entityManager.flush();

        game.setLoser(loser);
        entityManager.persist(game);
        entityManager.flush();

        List<Game> foundGames = gameRepository.findByLoserId(loser.getId());

        assertNotNull(foundGames);
        assertEquals(1, foundGames.size());
        assertEquals(loser.getId(), foundGames.get(0).getLoser().getId());
    }

    @Test
    public void findByPlayerAsWinnerOrLoser_success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setToken("testToken");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreation_date(LocalDate.now());
        entityManager.persist(user);
        entityManager.flush();

        Game gameAsWinner = new Game();
        entityManager.persist(gameAsWinner);
        entityManager.flush();

        Player playerAsWinner = new Player();
        playerAsWinner.setUser(user);
        playerAsWinner.setGame(gameAsWinner);
        entityManager.persist(playerAsWinner);
        entityManager.flush();

        gameAsWinner.setWinner(playerAsWinner);
        entityManager.persist(gameAsWinner);
        entityManager.flush();

        Game gameAsLoser = new Game();
        entityManager.persist(gameAsLoser);
        entityManager.flush();

        Player playerAsLoser = new Player();
        playerAsLoser.setUser(user);
        playerAsLoser.setGame(gameAsLoser);
        entityManager.persist(playerAsLoser);
        entityManager.flush();

        gameAsLoser.setLoser(playerAsLoser);
        entityManager.persist(gameAsLoser);
        entityManager.flush();

        List<Game> foundGames = gameRepository.findByPlayerAsWinnerOrLoser(playerAsWinner.getId());

        assertNotNull(foundGames);
        assertEquals(2, foundGames.size());
    }

    @Test
    public void countByWinnerUserId_success() {
        User winnerUser = new User();
        winnerUser.setUsername("testUser");
        winnerUser.setPassword("testPassword");
        winnerUser.setToken("testToken");
        winnerUser.setStatus(UserStatus.OFFLINE);
        winnerUser.setCreation_date(LocalDate.now());
        entityManager.persist(winnerUser);
        entityManager.flush();

        Player player = new Player();
        player.setUser(winnerUser);
        entityManager.persist(player);
        entityManager.flush();

        Game game1 = new Game();
        game1.setWinnerUser(winnerUser);
        game1.setWinner(player);
        entityManager.persist(game1);

        Game game2 = new Game();
        game2.setWinnerUser(winnerUser);
        game2.setWinner(player);
        entityManager.persist(game2);

        entityManager.flush();

        Long count = gameRepository.countByWinnerUserId(winnerUser.getId());

        assertEquals(2, count);
    }

    @Test
    public void findByGameId_success() {
        Game game = new Game();
        entityManager.persist(game);
        entityManager.flush();

        Game foundGame = gameRepository.findByGameId(game.getGameId());

        assertNotNull(foundGame);
        assertEquals(game.getGameId(), foundGame.getGameId());
    }
}
