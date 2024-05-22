package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
public class FriendRequestRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @AfterEach
    public void teardown() {
        friendRequestRepository.deleteAll();
    }

    @Test
    public void findBySenderIdAndReceiverId_success() {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestType(RequestType.FRIENDADDING);
        friendRequest.setCreationTime(LocalDateTime.now());
        friendRequest.setSenderId(1L);
        friendRequest.setReceiverId(2L);
        friendRequest.setStatus(RequestStatus.PENDING);
        entityManager.persist(friendRequest);
        entityManager.flush();

        FriendRequest found = friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L);

        assertNotNull(found);
        assertEquals(1L, found.getSenderId());
        assertEquals(2L, found.getReceiverId());
    }

    @Test
    public void findByRequestTypeAndSenderIdAndReceiverId_success() {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestType(RequestType.FRIENDADDING);
        friendRequest.setCreationTime(LocalDateTime.now());
        friendRequest.setSenderId(1L);
        friendRequest.setReceiverId(2L);
        friendRequest.setStatus(RequestStatus.PENDING);
        entityManager.persist(friendRequest);
        entityManager.flush();

        FriendRequest found = friendRequestRepository.findByRequestTypeAndSenderIdAndReceiverId(RequestType.FRIENDADDING, 1L, 2L);

        assertNotNull(found);
        assertEquals(RequestType.FRIENDADDING, found.getRequestType());
        assertEquals(1L, found.getSenderId());
        assertEquals(2L, found.getReceiverId());
    }

    @Test
    public void findByRequestTypeAndReceiverIdAndStatus_success() {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestType(RequestType.FRIENDADDING);
        friendRequest.setCreationTime(LocalDateTime.now());
        friendRequest.setReceiverId(2L);
        friendRequest.setStatus(RequestStatus.PENDING);
        friendRequest.setSenderId(1L);
        entityManager.persist(friendRequest);
        entityManager.flush();

        List<FriendRequest> foundRequests = friendRequestRepository.findByRequestTypeAndReceiverIdAndStatus(RequestType.FRIENDADDING, 2L, RequestStatus.PENDING);

        assertNotNull(foundRequests);
        assertEquals(1, foundRequests.size());
        assertEquals(RequestType.FRIENDADDING, foundRequests.get(0).getRequestType());
        assertEquals(2L, foundRequests.get(0).getReceiverId());
        assertEquals(RequestStatus.PENDING, foundRequests.get(0).getStatus());
    }

    @Test
    public void findBySenderIdAndStatus_success() {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestType(RequestType.FRIENDADDING);
        friendRequest.setCreationTime(LocalDateTime.now());
        friendRequest.setSenderId(1L);
        friendRequest.setStatus(RequestStatus.PENDING);
        friendRequest.setReceiverId(2L);
        entityManager.persist(friendRequest);
        entityManager.flush();

        List<FriendRequest> foundRequests = friendRequestRepository.findBySenderIdAndStatus(1L, RequestStatus.PENDING);

        assertNotNull(foundRequests);
        assertEquals(1, foundRequests.size());
        assertEquals(1L, foundRequests.get(0).getSenderId());
        assertEquals(RequestStatus.PENDING, foundRequests.get(0).getStatus());
    }

    @Test
    public void deleteById_success() {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestType(RequestType.FRIENDADDING);
        friendRequest.setCreationTime(LocalDateTime.now());
        friendRequest.setSenderId(1L);
        friendRequest.setReceiverId(2L);
        friendRequest.setStatus(RequestStatus.PENDING);
        entityManager.persist(friendRequest);
        entityManager.flush();

        Long id = friendRequest.getId();
        friendRequestRepository.deleteById(id);
        entityManager.flush();

        FriendRequest found = entityManager.find(FriendRequest.class, id);
        assertNull(found);
    }
}
