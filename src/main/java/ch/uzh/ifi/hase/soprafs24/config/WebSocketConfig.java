package ch.uzh.ifi.hase.soprafs24.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private static final String WS_LOCALHOST = "http://localhost:3000";
    private static final String WS_PROD = "https://sopra-fs24-group-08-client.oa.r.appspot.com";



    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        logger.info("Configuring message broker");
        registry.enableSimpleBroker("/topic", "/queue", "/game", "/chat")
                .setTaskScheduler(heartbeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("Registering STOMP endpoints");
        registry.addEndpoint("/ws")
                .addInterceptors(httpSessionHandshakeInterceptor())
                .setAllowedOrigins(WS_LOCALHOST, WS_PROD);
    }

    @Bean
    public ThreadPoolTaskScheduler heartbeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 1 MB adjust if we ever need more.
    }

    //For Testing
    @Bean
    public WebSocketStompClient webSocketStompClient() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setInboundMessageSizeLimit(10240); // Set message size limit (in bytes)

        return stompClient;
    }


    @Bean
    public HttpSessionHandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                try {
                    logger.debug("Starting handshake");
                    if (request instanceof ServletServerHttpRequest) {
                        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                        String token = servletRequest.getServletRequest().getParameter("token");
                        if (token != null && !token.isEmpty()) {
                            attributes.put("sessionId", token);
                            logger.debug("Handshake successful with token: {}", token);
                            return true;
                        }
                    }
                    logger.warn("Handshake failed: Token is missing or empty");
                    return false;
                } catch (Exception e) {
                    logger.error("Error during handshake", e);
                    throw e;
                }
            }
        };
    }
}