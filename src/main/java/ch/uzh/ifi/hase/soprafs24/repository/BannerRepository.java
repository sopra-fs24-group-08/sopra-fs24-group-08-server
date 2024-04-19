package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("bannerRepository")
public interface BannerRepository extends JpaRepository<Banner, Long> {
    Banner findByName(String name);
}
