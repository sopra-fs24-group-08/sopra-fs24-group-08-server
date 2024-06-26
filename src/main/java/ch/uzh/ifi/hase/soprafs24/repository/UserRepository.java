package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT f FROM User u JOIN u.friends f WHERE u.id = :userId")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
    User findByUsername(String username);
    User findByid(Long id);
    User findByPassword(String password);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :userId AND u.token = :token")
    boolean existsByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    @Query("SELECT u.avatarUrl FROM User u WHERE u.id = :userId")
    Optional<String> findAvatarUrlByUserId(@Param("userId") Long userId);


    // Need this for authorization, to verify if the token is really a valid one
    User findByToken(String token);
}


