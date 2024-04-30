package ch.uzh.ifi.hase.soprafs24.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketChatBoxControllerTests {

    @LocalServerPort
    private int port;

    @Test
    public void testBroadcast() throws Exception {
        CountDownLatch latch = new CountDownLatch(2); // Expect two messages, one for each session
        TextMessage[] receivedMessage1 = {null};
        TextMessage[] receivedMessage2 = {null};

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        // Create two separate WebSocket sessions
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketSession session1 = webSocketClient.doHandshake(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                receivedMessage1[0] = message;
                latch.countDown(); // Decrease latch count when message is received
            }
        }, headers, URI.create("ws://localhost:" + port + "/chat")).get();

        WebSocketSession session2 = webSocketClient.doHandshake(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                receivedMessage2[0] = message;
                latch.countDown(); // Decrease latch count when message is received
            }
        }, headers, URI.create("ws://localhost:" + port + "/chat")).get();

        // Send a message from one session
        String testMessage = "Hello, WebSocket!";
        session1.sendMessage(new TextMessage(testMessage));

        // Wait for the message to be received by both sessions
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Check if the message was received by both sessions
        assertTrue(receivedMessage1[0].getPayload().equals(testMessage));
        assertTrue(receivedMessage2[0].getPayload().equals(testMessage));

        // Close the sessions
        session1.close();
        session2.close();
    }
}
