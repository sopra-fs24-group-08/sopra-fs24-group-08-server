package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

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


