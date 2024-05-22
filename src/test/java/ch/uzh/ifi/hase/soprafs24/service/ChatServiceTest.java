package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ChatRoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessagePostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.Optional;

public class ChatServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private ChatService chatService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testCleanupChatRoom() {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        chatService.cleanupChatRoom(chatRoom);

        verify(chatMessageRepository, times(1)).deleteAll(chatRoom.getMessages());
        verify(chatRoomRepository, times(1)).delete(chatRoom);
    }
    @Test
    public void testSendChatMessageSuccessfully() {
        Long gameId = 1L;
        Long senderId = 1L;
        MessagePostDTO messagePostDTO = new MessagePostDTO();
        messagePostDTO.setSenderId(senderId);
        messagePostDTO.setMessage("Hello, World!");

        User user = new User();
        user.setId(senderId);
        user.setUsername("testUser");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findByGameId(gameId)).thenReturn(Optional.of(chatRoom));

        chatService.sendChatMessage(gameId, messagePostDTO);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        assertEquals("/topic/chat/1", destinationCaptor.getValue());
        Map<String, Object> sentMessage = messageCaptor.getValue();
        assertEquals("Hello, World!", sentMessage.get("messageContent"));
        assertEquals(senderId, sentMessage.get("senderId"));
        assertEquals("testUser", sentMessage.get("senderUsername"));
        assertNotNull(sentMessage.get("timestamp"));  // Ensure timestamp is recorded
    }
}
