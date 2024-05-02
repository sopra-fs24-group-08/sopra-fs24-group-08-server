package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        //Event raised when a new WebSocket client using a Simple Messaging Protocol (e. g. STOMP)
        // as the WebSocket sub-protocol issues a connect request.
        System.out.println("New WebSocket Connection has been formed");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("Session Disconnected: " + sessionId);

    }
}