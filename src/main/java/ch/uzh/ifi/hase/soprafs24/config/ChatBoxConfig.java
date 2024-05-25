package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ch.uzh.ifi.hase.soprafs24.controller.ChatBoxController;

@Configuration
@EnableWebSocket
public class ChatBoxConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/chat"是WebSocket端点，客户端将通过它来建立连接
        // 允许所有的域进行连接，也可以根据需要限制域
        registry.addHandler(new ChatBoxController(), "/chat").setAllowedOrigins("*");
    }
}