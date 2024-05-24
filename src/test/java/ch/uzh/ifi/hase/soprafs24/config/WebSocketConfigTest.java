package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.config.WebSocketConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WebSocketConfig.class)
public class WebSocketConfigTest {

    @Autowired
    private WebSocketStompClient webSocketStompClient;

    private WebSocketConfig webSocketConfig;
    private MessageBrokerRegistry brokerRegistry;
    private StompEndpointRegistry endpointRegistry;
    private WebSocketTransportRegistration transportRegistration;

    @BeforeEach
    public void setUp() {
        webSocketConfig = new WebSocketConfig();  // Direct instantiation of our configuration class
        brokerRegistry = mock(MessageBrokerRegistry.class);
        endpointRegistry = mock(StompEndpointRegistry.class);
        transportRegistration = mock(WebSocketTransportRegistration.class);

        // Mocking chained methods to return non-null or further mock objects
        SimpleBrokerRegistration simpleBrokerRegistration = mock(SimpleBrokerRegistration.class);
        when(brokerRegistry.enableSimpleBroker(any())).thenReturn(simpleBrokerRegistration);
        when(simpleBrokerRegistration.setTaskScheduler(any())).thenReturn(simpleBrokerRegistration);

        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);
        when(endpointRegistry.addEndpoint(anyString())).thenReturn(endpointRegistration);
        when(endpointRegistration.addInterceptors(any())).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOrigins(any())).thenReturn(endpointRegistration);
    }

    @Test
    public void testMessageBrokerConfig() {
        webSocketConfig.configureMessageBroker(brokerRegistry);
        verify(brokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(brokerRegistry).enableSimpleBroker("/topic", "/queue", "/game", "/chat");
    }

    @Test
    public void testRegisterEndpoints() {
        webSocketConfig.registerStompEndpoints(endpointRegistry);
        verify(endpointRegistry).addEndpoint("/ws");
    }

    @Test
    public void testWebSocketStompClientConfiguration() {
        assertNotNull(webSocketStompClient, "WebSocketStompClient should not be null");
        assertTrue(webSocketStompClient.getMessageConverter() instanceof MappingJackson2MessageConverter, "MessageConverter should be an instance of MappingJackson2MessageConverter");
        assertEquals(10240, webSocketStompClient.getInboundMessageSizeLimit(), "Inbound message size limit should be set to 10240 bytes");
    }

    @Test
    public void testConfigureWebSocketTransport() {
        webSocketConfig.configureWebSocketTransport(transportRegistration);
        verify(transportRegistration).setMessageSizeLimit(1024 * 1024);
    }
}
