package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest
public class ChatMessageRepositoryIntegrationTest {

    //@Autowired
    //private TestEntityManager entityManager;

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByChatRoomId_success() {
        ChatRoom chatRoom = new ChatRoom();
        chatRoomRepository.saveAndFlush(chatRoom);

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setCreation_date(LocalDate.now());
        user.setToken("testToken");
        user.setStatus(ONLINE);
        userRepository.saveAndFlush(user);
        //entityManager.persistAndFlush(user);

        ChatMessage message1 = new ChatMessage();
        message1.setMessageContent("Hello");
        message1.setChatRoom(chatRoom);
        message1.setTimestamp(LocalDateTime.now());
        message1.setSender(user);
        chatMessageRepository.saveAndFlush(message1);

        ChatMessage message2 = new ChatMessage();
        message2.setMessageContent("Hi");
        message2.setChatRoom(chatRoom);
        message2.setTimestamp(LocalDateTime.now().plusMinutes(1));
        message2.setSender(user);

        chatMessageRepository.saveAndFlush(message2);

        Pageable pageable = PageRequest.of(0, 1); // Get the first page, one message per page
        List<ChatMessage> foundMessages = chatMessageRepository.findByChatRoomId(chatRoom.getId(), pageable);

        assertNotNull(foundMessages);
        assertEquals(1, foundMessages.size());
        assertEquals("Hello", foundMessages.get(0).getMessageContent());

        pageable = PageRequest.of(1, 1); // Get the second page, one message per page
        foundMessages = chatMessageRepository.findByChatRoomId(chatRoom.getId(), pageable);

        assertNotNull(foundMessages);
        assertEquals(1, foundMessages.size());
        assertEquals("Hi", foundMessages.get(0).getMessageContent());
    }

    @AfterEach
    public void teardown() {
        chatMessageRepository.deleteAll();
    }
}
