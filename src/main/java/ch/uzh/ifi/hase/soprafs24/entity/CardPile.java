/*
package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class CardPile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cardPile", cascade = CascadeType.ALL)
    private List<Card> cards = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "game_id")
    private Game game;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }


    public void addCard(Card card) {
        cards.add(card);
        card.setCardPile(this);
    }

    public Card drawCard() {
        if (!cards.isEmpty()) {
            return cards.remove(0);
        }
        return null;
    }
}
*/
