package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Transactional
    void deleteByChatRoom(ChatRoom chatRoom);
}

