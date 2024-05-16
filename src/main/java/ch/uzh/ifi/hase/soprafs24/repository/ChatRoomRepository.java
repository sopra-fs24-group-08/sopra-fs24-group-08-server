package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    //@Query("SELECT cr FROM ChatRoom cr WHERE (cr.participantOne.id = ?1 AND cr.participantTwo.id = ?2) OR (cr.participantOne.id = ?2 AND cr.participantTwo.id = ?1)")
    //Optional<ChatRoom> findChatRoomByParticipants(Long participantOneId, Long participantTwoId); //Double check before prod please.
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.game.gameId = :gameId")
    Optional<ChatRoom> findByGameId(Long gameId);
}