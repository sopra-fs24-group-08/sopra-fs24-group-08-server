package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.GameInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import java.util.List;


@Repository("invitationRepository")
public interface GameInvitationRepository extends JpaRepository<GameInvitation, Long> {
    GameInvitation findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List <GameInvitation> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);
    List<GameInvitation> findBySenderIdAndStatus(Long senderId, RequestStatus status);
    void deleteBySenderIdAndStatus(Long userId, RequestStatus status);
}