package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Banner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class BannerRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BannerRepository bannerRepository;

    @Test
    public void findByName_success() {
        Banner banner = new Banner();
        banner.setName("testBanner");

        entityManager.persistAndFlush(banner);

        Banner found = bannerRepository.findByName("testBanner");

        assertNotNull(found);
        assertEquals("testBanner", found.getName());
    }

    @AfterEach
    public void teardown() {
        bannerRepository.deleteAll();
    }
}
