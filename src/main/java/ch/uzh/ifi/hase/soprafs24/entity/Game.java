package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
//Add the variables that are necessary, we should stick to the diagrams
@Entity
@Table(name = "Game")
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;

    // Hier wird eine One-to-One-Beziehung zwischen Game und ChatBox definiert
    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL)
    private ChatBox chatBox;


//Decide how exactly implement User/Player, M2 Report doesn't show enough
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    public Long getGameId() {
        return gameId;
    }

    public Game() {
        // Beim Erstellen eines neuen Spielobjekts wird auch eine neue Chatbox erstellt und zugeordnet
        this.chatBox = new ChatBox(this);
    }
    public ChatBox getChatBox() {
        return chatBox;
    }

    public void setChatBox(ChatBox chatBox) {
        this.chatBox = chatBox;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setGame(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setGame(null);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

}
