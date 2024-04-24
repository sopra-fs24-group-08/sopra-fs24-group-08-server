package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import ch.uzh.ifi.hase.soprafs24.entity.MatchmakingResult;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MatchmakingResponse;
import ch.uzh.ifi.hase.soprafs24.service.MatchmakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.*;

import java.util.Random;

@Controller
public class MatchmakingController {
    //Handle with Polling if possible other extend on this
    @Autowired
    private MatchmakingService matchmakingService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/matchmaking/join")
    @SendToUser("/queue/matchmaking")
    public void joinMatchmaking(SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("WE is trying to join jup");
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        MatchmakingResult result = matchmakingService.joinPlayer(userId);
        if (result.isMatchFound()) {
            // Notify both users that they are matched
            messagingTemplate.convertAndSendToUser(result.getFirstPlayerId().toString(), "/queue/matchmaking", new MatchmakingResponse("matched", result.getGameId()));
            messagingTemplate.convertAndSendToUser(result.getSecondPlayerId().toString(), "/queue/matchmaking", new MatchmakingResponse("matched", result.getGameId()));

            // Send a decision request to one of the players

            decideTurns(result.getGameId(), result.getFirstPlayerId(),result.getSecondPlayerId())   ;
        }
    }

    private void sendDecisionRequest(Long gameId, Long playerId) {
        // Send a message to the chosen player to decide who starts
     //   messagingTemplate.convertAndSendToUser(playerId.toString(), "/queue/turn-decision", new TurnDecisionRequest("Do you want to go first?", gameId));
    }

    private void decideTurns(Long gameId, Long firstPlayerId, Long secondPlayerId) {
        boolean firstPlayerStarts = new Random().nextBoolean();
        Long chooserPlayerId = firstPlayerStarts ? firstPlayerId : secondPlayerId;
        String congrats = "U have won the coin flip!,would you like to go first?";;
        messagingTemplate.convertAndSendToUser(chooserPlayerId.toString(), "/queue/turn-decision", new TurnDecisionRequestDTO(gameId,congrats));
    }
}
