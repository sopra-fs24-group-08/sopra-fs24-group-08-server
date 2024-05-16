package ch.uzh.ifi.hase.soprafs24.service.util;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import java.util.List;

@Service
public class BatchDataHandler {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Deletes messages in batches to avoid large transactions and manage performance.
     * @param chatRoomId The ID of the chat room whose messages are to be deleted.
     */
    @Transactional
    public void deleteChatMessagesInBatch(Long chatRoomId) {
        int pageSize = 100; // Number of records to process at a time
        boolean allDeleted = false;
        while (!allDeleted) {
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoomId, PageRequest.of(0, pageSize));
            if (messages.size() < pageSize) {
                allDeleted = true;
            }
            chatMessageRepository.deleteInBatch(messages);
        }
    }
}
