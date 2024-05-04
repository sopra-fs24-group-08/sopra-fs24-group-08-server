package ch.uzh.ifi.hase.soprafs24.entity;
import javax.persistence.*;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(nullable = false)
    private boolean isCardPile = false;

    public GridSquare() {
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

    public boolean isOccupied() {
        return this.card != null;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean isCardPile() {
        return isCardPile;
    }

    public void setCardPile(boolean cardPile) {
        isCardPile = cardPile;
    }
}
