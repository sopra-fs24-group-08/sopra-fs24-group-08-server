package ch.uzh.ifi.hase.soprafs24.entity;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
public class GridSquare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "square", cascade = CascadeType.ALL)
    private List<Card> cards;  // Cards mapping corrected

    @Column(nullable = false)
    private boolean isCardPile = false;

    public boolean isOccupied() {
        return !cards.isEmpty();  // A square is occupied if the cards list is not empty
        // (cards list can only be 0/1 on all squares except middle one
    }

    public void addCard(Card card) {
        if (!isCardPile && isOccupied()) {
            throw new IllegalStateException("This square is already occupied.");
        }
        cards.add(card);
        card.setSquare(this);
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public boolean isCardPile() {
        return isCardPile;
    }

    public void setCardPile(boolean cardPile) {
        isCardPile = cardPile;
    }


}
