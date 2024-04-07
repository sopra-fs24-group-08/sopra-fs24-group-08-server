package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DBService {

    private final AchievementRepository achievementRepository;

    @Autowired
    public DBService(@Qualifier("achievementRepository") AchievementRepository achievementRepository) {
        this.achievementRepository= achievementRepository;
    }

    @Transactional
    public void initializeAchievements() {
        List<Achievement> predefinedAchievements = List.of(
                new Achievement("Newbie","Account has just been created"),
                new Achievement("Tutorial finished","Finish the Tutorial"),
                new Achievement("First Win", "Win your first game."),
                new Achievement("Intermediate", "Play 5 games."),
                new Achievement("Popular Player", "Have more than 5 friends."),
                new Achievement("Best Player in town", "Have the most wins.")

        );

        predefinedAchievements.forEach(achievement -> {
            if (achievementRepository.findByTitle(achievement.getTitle()) == null) {
                achievementRepository.save(achievement);
            }
        });
    }
}