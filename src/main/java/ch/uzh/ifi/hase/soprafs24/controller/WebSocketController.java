/*
package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    //private final Logger log = LoggerFactory.getLogger(GameService.class);


    @MessageMapping("/user")
    @SendTo("/topic/greetings")
    public String greeting() {
        return "Greetings User!";
    }

    @MessageMapping("/chat")
    @SendTo("/chat/chatbox")
    public String chatbox(String message){
        return message; }

    @MessageMapping("/announce")
    @SendTo("/topic/announcements")
    public String announce(String message) {
        return message;
    }


    // Endpoint for handling in-game chat, demonstrating how to send messages to a specific game's chatbox
    */
/*@MessageMapping("/game/{gameId}/chat")
    public void gameChat(@DestinationVariable Long gameId, String message) {
        // Assuming a method exists to send a chat message to all subscribers of a game's chat
        gameService.broadcastChatMessage(gameId, message);
    }*//*



}

*/
