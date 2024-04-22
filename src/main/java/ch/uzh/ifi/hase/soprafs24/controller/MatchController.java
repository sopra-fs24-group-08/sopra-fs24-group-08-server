package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import ch.uzh.ifi.hase.soprafs24.service.MatchService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
public class MatchController {

    private final UserService userService;
    private final MatchService matchService;

    MatchController(UserService userService, MatchService matchService) {
      this.userService = userService;
      this.matchService = matchService;
    }
    // request to start a game with random online users
    @PutMapping("/games/queue/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public DeferredResult<GameMatchResultDTO> MatchUser(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) { 
      userService.authenticateUser(authorization, userId);
      DeferredResult<GameMatchResultDTO> deferredResult = new DeferredResult<>(GlobalConstants.QUEUE_TIMEOUT); 
      deferredResult.onTimeout(() -> deferredResult.setErrorResult(
        ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body("No available Player right now, please try again!"))); 
      matchService.addUserToQueue(userId, deferredResult);
      return deferredResult;
    }
    
  
}