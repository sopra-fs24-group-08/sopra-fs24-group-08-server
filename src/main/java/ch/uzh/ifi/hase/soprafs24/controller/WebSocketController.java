package ch.uzh.ifi.hase.soprafs24.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {


    @MessageMapping("/announce")
    @SendTo("/topic/announcements")
    public String announce(String message) {
        return message;
    }
}