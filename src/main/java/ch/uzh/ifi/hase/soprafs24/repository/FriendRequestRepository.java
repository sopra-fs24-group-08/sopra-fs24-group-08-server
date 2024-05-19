package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import java.util.List;

@Repository("friendRequestRepository")
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    FriendRequest findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    FriendRequest findByRequestTypeAndSenderIdAndReceiverId(RequestType requestType, Long senderId, Long receiverId);
    List <FriendRequest> findByRequestTypeAndReceiverIdAndStatus(RequestType requestType, Long receiverId, RequestStatus status);
    List<FriendRequest> findBySenderIdAndStatus(@Param("senderId") Long senderId, RequestStatus status);
    void deleteById(Long id);
}
