package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import java.util.List;
import java.util.Optional;

@Repository("friendRequestRepository")
// <FriendRequest, Long> FriendRequest：这表明RequestRepository将操作FriendRequest实体类的实例。
// 也就是说，这个仓库将用来保存、查询、删除等操作FriendRequest对象。
// Long：这指定了FriendRequest实体的ID属性的类型是Long。ID是实体的唯一标识符，用于在数据库中唯一定位记录。
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    FriendRequest findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    FriendRequest findByRequestTypeAndSenderIdAndReceiverId(RequestType requestType, Long senderId, Long receiverId);
    List <FriendRequest> findByRequestTypeAndReceiverIdAndStatus(RequestType requestType, Long receiverId, RequestStatus status);
    List<FriendRequest> findBySenderIdAndStatus(@Param("senderId") Long senderId, RequestStatus status);
    void deleteById(Long id);
}
