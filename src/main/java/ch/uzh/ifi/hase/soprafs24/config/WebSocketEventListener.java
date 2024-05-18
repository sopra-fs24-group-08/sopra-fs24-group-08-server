package ch.uzh.ifi.hase.soprafs24.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    private final Logger logger;

    // Default constructor for normal use
    public WebSocketEventListener() {
        this.logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    }

    // Constructor for testing
    public WebSocketEventListener(Logger logger) {
        this.logger = logger;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("New WebSocket connection established for userId {}", event);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        logger.info("WebSocket session disconnected: {}", sessionId);
    }
}