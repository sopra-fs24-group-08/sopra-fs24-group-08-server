package ch.uzh.ifi.hase.soprafs24.websocket;

import org.springframework.messaging.simp.stomp.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestStompSessionHandler extends StompSessionHandlerAdapter {
    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private final CountDownLatch messageLatch = new CountDownLatch(1);
    private String lastReceivedMessage;

    public void awaitConnection() throws InterruptedException {
        if (!connectionLatch.await(1, TimeUnit.SECONDS)) {
            throw new RuntimeException("Connection not established");
        }
    }

    public boolean awaitMessage() throws InterruptedException {
        return messageLatch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Connected to WebSocket!");
        connectionLatch.countDown();
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("Received: " + new String((byte[]) payload));
        lastReceivedMessage = new String((byte[]) payload);
        messageLatch.countDown();
    }

    public String getLastReceivedMessage() {
        return lastReceivedMessage;
    }
}
