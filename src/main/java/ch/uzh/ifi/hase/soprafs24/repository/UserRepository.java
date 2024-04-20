package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository("userRepository")   //在JPA里都定义好了 不用自己写
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT f FROM User u JOIN u.friends f WHERE u.id = :userId")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
    User findByName(String name);
    User findByUsername(String username);
    User findByid(Long id);

    // Need this for authorization, to verify if the token is really a valid one
    User findByToken(String token);
}


