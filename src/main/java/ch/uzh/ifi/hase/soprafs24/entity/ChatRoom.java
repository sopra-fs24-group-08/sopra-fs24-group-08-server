package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class ChatRoom {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "participant_1_id", nullable = false)
    private User participantOne;

    @ManyToOne
    @JoinColumn(name = "participant_2_id", nullable = false)
    private User participantTwo;

    @OneToOne(optional = true) //if null its for private chat otherwise gamechat between 2 players.
    @JoinColumn(name = "game_id", unique = true) // Ensures that each game has at most one chat room
    private Game game;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    public ChatRoom() {
    }

    public ChatRoom(User participantOne, User participantTwo) {
        this.participantOne = participantOne;
        this.participantTwo = participantTwo;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public User getParticipantTwo() {
        return participantTwo;
    }

    public void setParticipantTwo(User participantTwo) {
        this.participantTwo = participantTwo;
    }

    public User getParticipantOne() {
        return participantOne;
    }

    public void setParticipantOne(User participantOne) {
        this.participantOne = participantOne;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
