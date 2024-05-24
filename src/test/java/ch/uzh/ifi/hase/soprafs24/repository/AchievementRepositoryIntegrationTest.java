package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AchievementRepositoryIntegrationTest {

    //@Autowired
    //private TestEntityManager entityManager;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    public void findByTitle_success() {
        Achievement achievement = new Achievement();
        achievement.setTitle("testAchievement");

        //entityManager.persistAndFlush(achievement);
        achievementRepository.save(achievement);
        achievementRepository.flush();

        Achievement found = achievementRepository.findByTitle("testAchievement");

        assertNotNull(found);
        assertEquals("testAchievement", found.getTitle());
        achievementRepository.delete(achievement);
    }

    @Test
    public void findById_success() {
        Achievement achievement = new Achievement();
        achievement.setTitle("testAchievement");

        achievementRepository.save(achievement);
        achievementRepository.flush();

        //entityManager.persistAndFlush(achievement);

        Achievement found = achievementRepository.findById(achievement.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(achievement.getId(), found.getId());
        achievementRepository.delete(achievement);
    }
}
