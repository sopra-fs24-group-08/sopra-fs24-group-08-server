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

    @Autowired
    private MatchmakingService matchmakingService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/matchmaking/join")
    @SendToUser("/queue/matchmaking")
    public void joinMatchmaking(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        MatchmakingResult result = matchmakingService.joinPlayer(userId);
        if (result.isMatchFound()) {
            // Send game ID and waiting notification
            messagingTemplate.convertAndSendToUser(result.getFirstPlayerId().toString(), "/queue/matchmaking", new MatchmakingResponse("waiting_room", result.getGameId()));
            messagingTemplate.convertAndSendToUser(result.getSecondPlayerId().toString(), "/queue/matchmaking", new MatchmakingResponse("waiting_room", result.getGameId()));

            // Determine who decides the turn
            decideTurns(result.getGameId(), result.getFirstPlayerId(), result.getSecondPlayerId());
        }
    }

    private void decideTurns(Long gameId, Long firstPlayerId, Long secondPlayerId) {
        boolean firstPlayerStarts = new Random().nextBoolean();
        Long chooserPlayerId = firstPlayerStarts ? firstPlayerId : secondPlayerId;
        String congrats = "U have won the coin flip!,would you like to go first?";;
        messagingTemplate.convertAndSendToUser(chooserPlayerId.toString(), "/queue/game-start", new TurnDecisionRequestDTO(gameId,congrats));
    }
}
