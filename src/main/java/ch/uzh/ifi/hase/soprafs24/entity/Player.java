package ch.uzh.ifi.hase.soprafs24.entity;



import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
//Add all the other variables mentioned on the diagrams.
@Entity
public class Player implements  Serializable{

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  //same id as user
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "player_id")
    private List<Card> hand = new ArrayList<>();

    @Column(nullable = false)
    private int score = 0;

    public Player() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int additionalPoints) {
        this.score += additionalPoints;
    }
    //Special events might make use of this.
    public void subScore(int substractPoints) {
        this.score -= substractPoints;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public void addCardToHand(Card card) {
        hand.add(card);
    }

    public void placeCard(GridSquare square, Card card) {
        if (this.hand.contains(card)) {     // Check if the card is in the player's hand
            this.hand.remove(card);
            square.setCard(card);
            this.score += card.getPoints();
        }
    }

}
