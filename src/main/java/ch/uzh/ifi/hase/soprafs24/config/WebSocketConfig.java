package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    String Local = "http://localhost:3000/";
    String Prod = "https://sopra-fs24-group-23-client.oa.r.appspot.com/";

    private TaskScheduler messageBrokerTaskScheduler;


    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue","/game","/chat").setTaskScheduler(HBScheduler()).setTaskScheduler(this.messageBrokerTaskScheduler);
        registry.setApplicationDestinationPrefixes("/app");


    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(Local, Prod)
                .setAllowedOriginPatterns("*");    }

    @Bean
    public TaskScheduler HBScheduler() {
        // Verifies that connections is still working
        return new ThreadPoolTaskScheduler();
    }
}
