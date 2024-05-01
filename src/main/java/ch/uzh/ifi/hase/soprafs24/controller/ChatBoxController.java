package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/chat") // 指定WebSocket连接的端点
public class ChatBoxController extends TextWebSocketHandler {
    // 存储所有WebSocket session，以向所有用户广播消息
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 当新的WebSocket连接开启
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    // 当接收到客户端发来的消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();

        // 转发消息给所有连接的客户端，包括发送消息的客户端
        for (WebSocketSession webSocketSession : sessions.values()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(message);
            }
        }
    }


    // 当连接关闭
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }

    // 错误处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session.getId());
        session.close(CloseStatus.SERVER_ERROR);
    }

    // 主动发送消息给客户端
    public void sendMessageToClient(String clientId, String message) throws IOException {
        WebSocketSession session = sessions.get(clientId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}