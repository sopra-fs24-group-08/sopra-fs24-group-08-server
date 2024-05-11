package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByWinnerId(Long winnerId);

    // Find games by loser
    List<Game> findByLoserId(Long loserId);

    // Find games where a specific player either won or lost
    @Query("SELECT g FROM Game g WHERE g.winner.id = :playerId OR g.loser.id = :playerId")
    List<Game> findByPlayerAsWinnerOrLoser(@Param("playerId") Long playerId);

    @Query("SELECT COUNT(g) FROM Game g WHERE g.winnerUser.id = :userId")
    Long countByWinnerUserId(Long userId);


    Game findByGameId(Long gameId);
}