/*
package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//Wait for Implementation of Jane, if hers work take hers.

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private WebSocketNotificationController webSocketNotificationController;

    public void sendFriendRequest(Long senderId, Long recipientId) {
        // Implementation
    }

    public void acceptFriendRequest(Long requestId) {

        webSocketNotificationController.notifyUserAboutFriendRequest(senderId, "Your friend request has been accepted.");
    }

    public void declineFriendRequest(Long requestId) {

        webSocketNotificationController.notifyUserAboutFriendRequest(senderId, "Your friend request has been declined.");
    }
}

*/
