package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs24.service.ChatService;
import ch.uzh.ifi.hase.soprafs24.websocket.WebSocketTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.stomp.StompSession;

import static org.mockito.Mockito.*;

public class ChatControllerTest extends WebSocketTestBase {

    @MockBean
    private ChatService chatService;

    @Test
    public void testHandleChatMessage() throws Exception {
        Long gameId = 1L;
        String validToken = "validToken";
        MessagePostDTO messagePostDTO = new MessagePostDTO();
        messagePostDTO.setMessage("Hello, World!");
        messagePostDTO.setSenderId(2L);

        StompSession stompSession = connectToWebSocket(validToken);
        stompSession.send(String.format("/app/chat/%d", gameId), messagePostDTO);

        // Wait for the message to be processed (using Thread.sleep or preferably we do CountDownLatch next week)
        Thread.sleep(100);

        verify(chatService).sendChatMessage(eq(gameId), argThat(msg ->
                msg.getMessage().equals("Hello, World!") &&
                        msg.getSenderId().equals(2L)
        ));
    }
}

