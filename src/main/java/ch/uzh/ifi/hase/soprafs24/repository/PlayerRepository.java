package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUser(User user);
    @Query("SELECT p.user.username FROM Player p WHERE p.id = :playerId")
    String findUsernameByPlayerId(@Param("playerId") Long playerId);
    @Query("SELECT p FROM Player p WHERE p.game.id = :gameId ORDER BY p.score DESC")
    List<Player> findPlayersByGameIdOrderedByScore(@Param("gameId") Long gameId);
}

