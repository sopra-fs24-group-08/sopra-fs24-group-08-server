package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Game")
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Board board;

    @Column(nullable = false)
    private GameStatus gameStatus = GameStatus.STARTING;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;
    //might need to add cascade if it starts causing trouble

    @ManyToOne
    @JoinColumn(name = "loser_id")
    private Player loser;
    //Not sure if there's a better option to handle game <-> chatRoom
    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoom chatRoom;

    @Column(name = "current_turn_player_id")
    private Long currentTurnPlayerId;
    // Track whose turn it is
    private int cardPileSize;

    public int getCardPileSize() {
        return cardPileSize;
    }

    public void setCardPileSize(int cardPileSize) {
        this.cardPileSize = cardPileSize;
    }

    public Long getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(Long currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Player getLoser() {
        return loser;
    }

    public void setLoser(Player loser) {
        this.loser = loser;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Game() {
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
        this.players = new ArrayList<>(players);
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}


