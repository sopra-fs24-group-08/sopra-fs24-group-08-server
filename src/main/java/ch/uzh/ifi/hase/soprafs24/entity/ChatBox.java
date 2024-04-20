package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ChatBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Definiere eine Many-to-One-Beziehung zwischen ChatBox und Game,
    // aber die Beziehung ist optional, um auch ChatBoxen ohne Spiel zuzuordnen
    // @ManyToOne(optional = true)
    // @JoinColumn(name = "game_id")
    private Game game;
    @ManyToMany
    private Set<User> participants = new HashSet<>();
    public ChatBox(){
    }
    public ChatBox(Game game) {
        this.game= game;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }
}

