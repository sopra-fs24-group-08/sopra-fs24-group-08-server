package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class IconRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private IconRepository iconRepository;

    @AfterEach
    public void teardown() {
        iconRepository.deleteAll();
    }

    @Test
    public void findByName_success() {
        // Create and persist an icon
        Icon icon = new Icon("Test Icon", "http://test.com/icon.png");
        entityManager.persist(icon);
        entityManager.flush();

        // Find the icon by name
        Icon foundIcon = iconRepository.findByName("Test Icon");
        assertNotNull(foundIcon);
        assertEquals("Test Icon", foundIcon.getName());
        assertEquals("http://test.com/icon.png", foundIcon.getImageUrl());
    }

    @Test
    public void findById_success() {
        // Create and persist an icon
        Icon icon = new Icon("Another Icon", "http://test.com/anothericon.png");
        entityManager.persist(icon);
        entityManager.flush();

        // Find the icon by ID
        Icon foundIcon = iconRepository.findById(icon.getId()).orElse(null);
        assertNotNull(foundIcon);
        assertEquals("Another Icon", foundIcon.getName());
        assertEquals("http://test.com/anothericon.png", foundIcon.getImageUrl());
    }

    @Test
    public void findByName_notFound() {
        // Attempt to find an icon by a name that does not exist
        Icon foundIcon = iconRepository.findByName("Nonexistent Icon");
        assertNull(foundIcon);
    }

    @Test
    public void findById_notFound() {
        // Attempt to find an icon by an ID that does not exist
        Icon foundIcon = iconRepository.findById(999L); // Assuming 999 is not a valid ID
        assertNull(foundIcon);
    }
}
