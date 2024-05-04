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

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GameRepository gameRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatService(UserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, GameRepository gameRepository, SimpMessagingTemplate messagingTemplate) {
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


    private ChatRoom createChatRoom(Long gameId) {
        // Logic to create a new chat room
        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setGame(gameRepository.findById(gameId).orElseThrow(
                () -> new RuntimeException("Game not found for gameId: " + gameId)
        ));
        return chatRoomRepository.save(newChatRoom);
    }
}
