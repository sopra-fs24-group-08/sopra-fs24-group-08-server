package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ChatRoomRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    public ChatRoom createOrGetChatRoom(Long participantOneId, Long participantTwoId) {
        return chatRoomRepository.findChatRoomByParticipants(participantOneId, participantTwoId)
                .orElseGet(() -> {
                    User participantOne = userRepository.findById(participantOneId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + participantOneId));
                    User participantTwo = userRepository.findById(participantTwoId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + participantTwoId));
                    ChatRoom newRoom = new ChatRoom(participantOne, participantTwo);
                    return chatRoomRepository.save(newRoom);
                });
    }

    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void deleteChatMessagesByRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with ID: " + roomId));
        chatMessageRepository.deleteByChatRoom(room);
        chatRoomRepository.delete(room);  // Optional: if you also want to delete the room itself
    }
}
