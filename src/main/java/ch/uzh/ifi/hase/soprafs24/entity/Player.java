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
public class Player implements Serializable {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // Use the same ID as the associated User
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
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

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public void addCardToHand(Card card) {
        if (card != null) {
            this.hand.add(card);
        }
    }

    public void removeCardFromHand(Card card) {
        this.hand.remove(card);
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

    public void subScore(int subtractPoints) {
        this.score -= subtractPoints;
    }

    public void placeCard(GridSquare square, Card card) {
        if (square != null && card != null && this.hand.contains(card)) {
            if (!square.isOccupied()) {
                square.getCards().add(card); // Adds card to the square, checking again after service
                this.hand.remove(card);      // Removes card from player's hand
                if (card.getColor().equals(square.getColor())) {
                    addScore(card.getPoints() * 2); // Double points for matching color
                } else {
                    addScore(card.getPoints());
                }
            } else {
                throw new IllegalStateException("Square is already occupied.");
            }
        } else {
            throw new IllegalArgumentException("Card not found in player's hand or invalid square/card.");
        }
    }
}