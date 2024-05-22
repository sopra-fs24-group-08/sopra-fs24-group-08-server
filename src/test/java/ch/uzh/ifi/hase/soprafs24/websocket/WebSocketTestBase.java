package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebSocketTestBase {

    @Autowired
    protected WebSocketStompClient stompClient;

    @LocalServerPort
    protected int port;

    @MockBean
    protected UserService userService;

    protected String websocketUrl;

    @BeforeEach
    public void setupWebSocket() throws Exception {
        websocketUrl = "ws://localhost:" + port + "/ws";
        // Simulate token validation
        when(userService.validateUserIdToken(anyLong(), anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(1);
            return "validToken".equals(token);
        });
    }

    protected StompSession connectToWebSocket(String token) throws Exception {
        TestStompSessionHandler sessionHandler = new TestStompSessionHandler();

        // Simulate error message for null or empty tokens
        if (token == null || token.isEmpty()) {
            System.out.println("Connection failed: Token is missing or empty");
            throw new IllegalArgumentException("Connection failed: Token is missing or empty");
        }

        String urlWithToken = String.format("%s?token=%s", websocketUrl, URLEncoder.encode(token, StandardCharsets.UTF_8));
        StompSession session = stompClient.connect(urlWithToken, sessionHandler).get(10, TimeUnit.SECONDS);
        sessionHandler.awaitConnection();  // Wait for the connection to be established
        return session;
    }

}
