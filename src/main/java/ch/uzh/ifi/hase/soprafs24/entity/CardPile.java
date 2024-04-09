package ch.uzh.ifi.hase.soprafs24.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardPile {
    private final List<Card> cards;
    public CardPile() {
        this.cards = new ArrayList<>();

    }
    public Card setRandom(){
        List<String> colors = List.of("red", "green", "blue", "white");
        List<Integer> points = List.of(1, 2, 3, 4);
        String color = colors.get(new Random().nextInt(colors.size()));
        int point = points.get(new Random().nextInt(points.size()));
        return new Card(color, point);
    }

    void initializeCardPile() {
        for (int i = 0; i < 30; i++) {
            Card card = setRandom();
            cards.add(card);
        }
    }
    public void shuffle() {
            Collections.shuffle(cards);
        }
    public Card drawCard() {
        if (!cards.isEmpty()) {
            return cards.remove(cards.size() - 1);
        } else {
            return null;
        }
    }
    }
