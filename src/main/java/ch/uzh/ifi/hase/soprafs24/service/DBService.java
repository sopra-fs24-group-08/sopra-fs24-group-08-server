package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.repository.AchievementRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IconRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class DBService implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final AchievementRepository achievementRepository;
    private final IconRepository iconRepository;

    @Autowired
    public DBService(AchievementRepository achievementRepository,IconRepository iconRepository) {
        this.achievementRepository= achievementRepository;
        this.iconRepository = iconRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeAchievements();
        initializeIcons();
    }


    public void initializeAchievements() {
        log.info("Initializing achievements...");
        List<Achievement> predefinedAchievements = List.of(
                new Achievement("Newbie","Account has just been created"),
                new Achievement("Tutorial finished","Finish the Tutorial"),
                new Achievement("First Win", "Win your first game."),
                new Achievement("Intermediate", "Play 5 games."),
                new Achievement("Popular Player", "Have more than 5 friends."),
                new Achievement("Best Player in town", "Have the most wins."),
                new Achievement("Test","Added for Testing")

        );

        predefinedAchievements.forEach(achievement -> {
            if (achievementRepository.findByTitle(achievement.getTitle()) == null) {
                achievementRepository.save(achievement);
            }
        });
    }
    public void initializeIcons() {
        log.info("Initializing icons...");
        List<Icon> predefinedIcons = List.of(
                new Icon("Default Icon","http://localhost:8080/images/OGIcon.jpg"),
                new Icon("Icon 2", "https://cdn2.thecatapi.com/images/77h.jpg"),
                new Icon("Icon 3", "https://cdn2.thecatapi.com/images/a3m.jpg"),
                new Icon("Icon 4", "https://cdn2.thecatapi.com/images/lJHXU7DlQ.jpg")
        );

        predefinedIcons.forEach(icon -> {
            if (iconRepository.findByName(icon.getName()) == null) {
                iconRepository.save(icon);
            }
        });
    }
}