package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

}
