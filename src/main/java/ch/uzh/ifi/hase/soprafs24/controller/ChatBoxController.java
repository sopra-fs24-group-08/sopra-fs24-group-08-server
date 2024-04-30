package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/chat") // 指定WebSocket连接的端点
public class ChatBoxController extends TextWebSocketHandler {

    // Map以游戏ID为键，每个游戏的WebSocket会话列表为值
    private final Map<String, List<WebSocketSession>> gameChats = new ConcurrentHashMap<>();

    // 用来存储所有的WebSocket session，以便可以向所有用户广播消息
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 当新的WebSocket连接开启
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    // 当接收到客户端发来的消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 解析房间ID（游戏ID）和消息
        String gameId = (String) session.getAttributes().get("gameId");
        String actualMessage = message.getPayload();


        // 这里可以添加自定义的处理逻辑，比如解析消息内容
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

    // 这个方法可以用来主动发送消息给客户端
    public void sendMessageToClient(String clientId, String message) throws IOException {
        WebSocketSession session = sessions.get(clientId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}