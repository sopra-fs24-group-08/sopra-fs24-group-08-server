package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByRecipientAndAcceptedIsNull(User recipient);
    List<FriendRequest> findBySenderAndAcceptedIsNull(User sender);
    List<FriendRequest> findByRecipientAndAccepted(User recipient, Boolean accepted);
}
