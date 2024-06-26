package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ChatRoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessagePostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);


    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GameRepository gameRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatService(UserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, GameRepository gameRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.gameRepository = gameRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendChatMessage(Long gameId, MessagePostDTO messagePostDTO) {
        User sender = userRepository.findById(messagePostDTO.getSenderId()).orElseThrow(() -> new RuntimeException("User not found"));
        ChatRoom chatRoom = chatRoomRepository.findByGameId(gameId).orElseThrow(() -> new RuntimeException("ChatRoom not found for gameId: " + gameId));

        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, messagePostDTO.getMessage());
        chatMessageRepository.save(chatMessage);

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("id", chatMessage.getId());
        messageDetails.put("messageContent", chatMessage.getMessageContent());
        messageDetails.put("timestamp", chatMessage.getTimestamp().toString());
        messageDetails.put("senderId", sender.getId());
        messageDetails.put("senderUsername", sender.getUsername());


        messagingTemplate.convertAndSend(String.format("/topic/chat/%s", gameId), messageDetails);
    }


    public void cleanupChatRoom(ChatRoom chatRoom) {
        if (chatRoom != null) {
            if (chatRoom.getMessages() != null) {
                chatMessageRepository.deleteAll(chatRoom.getMessages()); // Clear all messages explicitly
            } else {
                logger.warn("No messages to clear for chatRoom ID {}", chatRoom.getId());
            }
            chatRoomRepository.delete(chatRoom);  // Delete the chat room
            logger.info("Chat room and all messages cleared for chatRoom ID {}", chatRoom.getId());
        } else {
            logger.error("Attempted to clean up a null chat room.");
        }
    }
}
