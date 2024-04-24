package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
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

import java.util.List;
import java.util.Random;

@Controller
public class MatchmakingController {
    //Handle with Polling if possible other extend on this

    @Autowired
    private MatchmakingService matchmakingService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameService gameService;

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

            // Send a decision request to one of the players, other gets wait message
            SendCoinFlipDecision(result.getGameId(), result.getFirstPlayerId(),result.getSecondPlayerId())   ;
        }
    }


    private void SendCoinFlipDecision(Long gameId, Long firstPlayerId, Long secondPlayerId) {
        Long chosenPlayerId = gameService.coinFlip(firstPlayerId, secondPlayerId);
        String congrats = "U have won the coin flip!,would you like to go first?";;
        String waitMessage = "Wait for your opponent to choose the order.";

        messagingTemplate.convertAndSendToUser(chosenPlayerId.toString(), "/queue/match/turn-decision", new TurnDecisionRequestDTO(gameId,congrats));

        Long otherPlayerId = (chosenPlayerId.equals(firstPlayerId)) ? secondPlayerId : firstPlayerId;
        messagingTemplate.convertAndSendToUser(otherPlayerId.toString(), "/queue/match/turn-decision", waitMessage);
}

}
