package ch.uzh.ifi.hase.soprafs24.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.persistence.*;


@Entity
public class CardPile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "cardPile")
    private List<Card> cards = new ArrayList<>();

    public CardPile() {
        initializeCards();
    }

    //perhaps add ColorConstant?
    private void initializeCards() {
        String[] colors = {"red", "green", "blue", "yellow", "black", "white"};
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);
            String color = colors[random.nextInt(colors.length)];
            Card card = new Card(color, points);
            card.setCardPile(this);
            cards.add(card);
        }
    }

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
}
