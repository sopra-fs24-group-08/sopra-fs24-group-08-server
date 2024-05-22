package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ChatRoomRepositoryIntegrationTest {

    //@Autowired
    //private TestEntityManager entityManager;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private GameRepository gameRepository;


    @Test
    public void findByGameId_success() {
        Game game = new Game();
        //entityManager.persistAndFlush(game);
        gameRepository.saveAndFlush(game);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setGame(game);
        //entityManager.persistAndFlush(chatRoom);
        chatRoomRepository.saveAndFlush(chatRoom);

        Optional<ChatRoom> foundChatRoom = chatRoomRepository.findByGameId(game.getGameId());

        assertTrue(foundChatRoom.isPresent());
        assertEquals(chatRoom.getId(), foundChatRoom.get().getId());
        assertEquals(game.getGameId(), foundChatRoom.get().getGame().getGameId());
    }

    @AfterEach
    public void teardown() {
        chatRoomRepository.deleteAll();
    }
}
