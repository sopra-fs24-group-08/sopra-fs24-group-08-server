package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    //private final Logger log = LoggerFactory.getLogger(GameService.class);


    @MessageMapping("/user")
    @SendTo("/topic/greetings")
    public String greeting(String message) {
        return "Check out this message: " + message;
    }

    @MessageMapping("/chat")
    @SendTo("/chat/chatbox")
    public String chatbox(String message){
        return message; }
    }


