package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import ch.uzh.ifi.hase.soprafs24.service.MatchmakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;


@Controller
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final UserService userService;

    @Autowired
    public MatchmakingController(MatchmakingService matchmakingService,UserService userService) {
        this.matchmakingService = matchmakingService;
        this.userService= userService;
    }

    @MessageMapping("/matchmaking/join/{userId}")
    public void joinMatchmaking(@DestinationVariable Long userId, SimpMessageHeaderAccessor headerAccessor) {
        // Retrieve session attributes to verify user session integrity
        String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");
        System.out.println("Session: " + sessionToken);
        if (sessionToken != null && userService.validateUserIdToken(userId, sessionToken)) {


            System.out.println("User " + userId + " has joined the matchmaking queue.");
            matchmakingService.addToQueue(userId);
        } else {
            System.out.println("Invalid session token for user: " + userId);
            // Optionally, handle the error, like informing the client
        }
    }


    @MessageMapping("/matchmaking/leave/{userId}")
    public void leaveMatchmaking(@DestinationVariable Long userId, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("Attempting to leave matchmaking for user: " + userId);
        String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");

        if (sessionToken != null && userService.validateUserIdToken(userId, sessionToken)) {
            matchmakingService.removeFromQueue(userId);
            System.out.println("User " + userId + " has left the matchmaking queue.");
        } else {
            System.out.println("Invalid session token or session mismatch for user: " + userId);
        }
    }

}
