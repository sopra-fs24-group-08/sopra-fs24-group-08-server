package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.service.ChatService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FriendService friendService;

    //private final Logger log = LoggerFactory.getLogger(GameService.class);
    @Autowired
    private UserService userService;


    @MessageMapping("/chat")
    @SendTo("/chat/chatbox")
    public String chatbox(String message){
        return message; }

    @MessageMapping("/announce")
    @SendTo("/topic/announcements")
    public String announce(String message) {
        return message;
    }

    /*@MessageMapping("/friend-requests/respond")
    public void respondToFriendRequest(Long userId, FriendRequestDTO response) {
        if (response.getStatus().equals("ACCEPTED")) {
            FriendService.acceptFriendRequest(userId, response.getReques());
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/friend-requests", "Friend request accepted!");
        } else {
            UserService.declineFriendRequest(userId, response.getRequestId());
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/friend-requests", "Friend request declined!");
        }
    }*/



    // Endpoint for handling in-game chat, demonstrating how to send messages to a specific game's chatbox
    /*@MessageMapping("/game/{gameId}/chat")
    public void gameChat(@DestinationVariable Long gameId, String message) {
        // Assuming a method exists to send a chat message to all subscribers of a game's chat
        gameService.broadcastChatMessage(gameId, message);
    }*/


}

