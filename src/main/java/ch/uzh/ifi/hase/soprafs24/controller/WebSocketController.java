package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameInvitationDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.ChatService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Objects;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FriendService friendService;

    @Autowired
    private GameService gameService;

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

    @MessageMapping("/{userId}/friend-requests/respond")
    public void respondToFriendRequest(@DestinationVariable Long userId, @Payload FriendRequestDTO response) {
        // Assume userId matches the receiverId for validation
        if (!userId.equals(response.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }
        if (Objects.equals(response.getStatus().toString(), "ACCEPTED")) {
            friendService.acceptFriendRequest(response.getSenderId(), userId);

        }else{ friendService.declineFriendRequest(response.getSenderId(), userId);
        }
        messagingTemplate.convertAndSend("/user/" + userId + "/friend-requests/response", response);
    }

    @MessageMapping("/game/{userId}/accept")//duplicate userId sort it later
    public void acceptGameInvitation(@Payload GameInvitationDTO response, @DestinationVariable Long userId) {
        if (Objects.equals(response.getStatus().toString(), "ACCEPTED")) {
            friendService.acceptedGameInvitation(response.getSenderId(), userId);
            messagingTemplate.convertAndSend("/user/"+userId, "Game invitation accepted!");
        }
    }

    // Handle game invitation decline
    @MessageMapping("/game/{userId}/decline")
    public void declineGameInvitation(@Payload GameInvitationDTO response, @DestinationVariable Long userId) {
        if (Objects.equals(response.getStatus().toString(), "DECLINED")) {
            friendService.declinedGameInvitation(response.getSenderId(), userId);
            messagingTemplate.convertAndSend("/user/"+userId+"game-invitations", "Game invitation declined");
        }

    }
}



    // Endpoint for handling in-game chat, demonstrating how to send messages to a specific game's chatbox
    /*@MessageMapping("/game/{gameId}/chat")
    public void gameChat(@DestinationVariable Long gameId, String message) {
        // Assuming a method exists to send a chat message to all subscribers of a game's chat
        gameService.broadcastChatMessage(gameId, message);
    }*/

