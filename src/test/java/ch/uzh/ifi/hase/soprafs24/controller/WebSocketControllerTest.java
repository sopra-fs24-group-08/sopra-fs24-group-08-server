/*
package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.config.WebSocketConfig;
import ch.uzh.ifi.hase.soprafs24.websocket.TestStompSessionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketControllerTest {
    @Autowired
    private WebSocketStompClient stompClient;

    @LocalServerPort
    private int port;

    private StompSession connectToWebSocket() throws Exception {
        String websocketUrl = "ws://localhost:" + port + "/ws";
        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        return stompClient.connect(websocketUrl, sessionHandler).get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testWebSocketConnection() throws Exception {
        String websocketUrl = "ws://localhost:" + port + "/ws";
        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        stompClient.connect(websocketUrl, sessionHandler).get(10, TimeUnit.SECONDS);
    }



    @Test
    public void testSubscribeAndReceive() throws Exception {
        StompSession session = connectToWebSocket();
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((String) payload);
            }
        });

        session.send("/app/chat", "Hello, WebSocket!");
        assertEquals("Hello, WebSocket!", completableFuture.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testSendMessage() throws Exception {
        StompSession session = connectToWebSocket();
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        session.subscribe("/queue/reply", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((String) payload);
            }
        });

        session.send("/app/message", "Test Message");
        assertEquals("Response from server", completableFuture.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testHandleDisconnect() throws Exception {
        StompSession session = connectToWebSocket();
        session.disconnect();
    }

    @Test
    public void testSessionAttributes() throws Exception {
        StompSession session = connectToWebSocket();

    }

    @Test
    public void testConnectionAndDisconnectionLogging() throws Exception {
        StompSession session = connectToWebSocket();
        Thread.sleep(1000); // wait for logs

        session.disconnect();
        Thread.sleep(1000); // wait for disconnection logs
    }


}*/
