/*
package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class MatchmakingController {

    @Autowired
    private MatchmakingService matchmakingService;

    @MessageMapping("/matchmaking/join")
    @SendToUser("/queue/matchmaking")
    public MatchmakingResponse joinMatchmaking(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        MatchmakingResult result = matchmakingService.joinPlayer(sessionId);
        if (result.isMatchFound()) {
            return new MatchmakingResponse("matched", result.getGameId());
        } else {
            return new MatchmakingResponse("waiting");
        }
    }
}
*/
