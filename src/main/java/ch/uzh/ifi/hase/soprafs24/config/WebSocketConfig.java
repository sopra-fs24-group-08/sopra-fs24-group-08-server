package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private UserRepository userRepository;

    private static final String WS_LOCALHOST = "http://localhost:3000";
    private static final String WS_PROD = "https://sopra-fs24-group-08-client.oa.r.appspot.com";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setInterceptors(httpSessionHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/chat","/user").setTaskScheduler(heartBeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    @Primary
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("WebSocketHeartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public HttpSessionHandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUri(request.getURI());
                Map<String, String> queryParams = uriComponents.build().getQueryParams().toSingleValueMap();

                String userIdStr = queryParams.get("userId");
                String token = queryParams.get("token");

                try {
                    Long userId = Long.parseLong(userIdStr);
                    if (userRepository.existsByUserIdAndToken(userId, token)) {
                        attributes.put("userId", userId);
                        System.out.println("Handshake successful for userId: " + userId);
                        return true; // Valid userId and token
                    } else {
                        System.err.println("Invalid token or userId: " + userIdStr + ", token: " + token);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid userId format: " + userIdStr);
                }
                return false;
            }
        };
    }

}


