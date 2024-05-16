package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.Mockito.*;

public class WebSocketEventListenerTest {
    private WebSocketEventListener eventListener;
    private Logger loggerMock;

    @BeforeEach
    public void setUp() {
        loggerMock = mock(Logger.class);
        eventListener = new WebSocketEventListener(loggerMock);
    }

    @Test
    public void whenConnected_thenLogConnection() {
        SessionConnectEvent event = mock(SessionConnectEvent.class);
        eventListener.handleWebSocketConnectListener(event);

        verify(loggerMock).info(contains("New WebSocket connection established"), eq(event));
    }

    @Test
    public void whenDisconnected_thenLogDisconnection() {
        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getSessionId()).thenReturn("12345");
        eventListener.handleWebSocketDisconnectListener(event);

        verify(loggerMock).info(contains("WebSocket session disconnected"), eq("12345"));
    }
}
