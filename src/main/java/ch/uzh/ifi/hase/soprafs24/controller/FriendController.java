package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Objects;


@Controller
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    @Autowired
    public FriendController(FriendService friendService, UserService userService) {
        this.friendService = friendService;
        this.userService= userService;
    }

    @MessageMapping("/friend/adding/{userId}")
    public void addFriendRequest(@DestinationVariable Long userId, @Payload FriendRequestDTO requestDTO, SimpMessageHeaderAccessor headerAccessor) {
      String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");
      if (sessionToken != null && userService.validateUserIdToken(userId, sessionToken)){
        FriendRequest friendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(requestDTO);
        friendService.addFriendRequest(userId, friendRequest);
      }
    }

    @MessageMapping("/friend/invitation/{userId}")
    public void inviteFriendRequest(@DestinationVariable Long userId, @Payload FriendRequestDTO requestDTO, SimpMessageHeaderAccessor headerAccessor) {
      String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");
      if (sessionToken != null && userService.validateUserIdToken(userId, sessionToken)){
        FriendRequest friendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(requestDTO);
        friendService.inviteFriendToGame(userId, friendRequest);
      }
    }

    @MessageMapping("/friend/result/{userId}")
    public void handleFriendRequest(@DestinationVariable Long userId, @Payload FriendRequestDTO requestDTO, SimpMessageHeaderAccessor headerAccessor) {
      String sessionToken = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("sessionId");
      if (sessionToken != null && userService.validateUserIdToken(userId, sessionToken)){
        FriendRequest friendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(requestDTO);
        friendService.handleRequest(userId, friendRequest);
      }
    }

}
