package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> centralCardPile = new ArrayList<Card>();

    // @ElementCollection
    // private List<String> squareColors = new ArrayList<String>(GlobalConstants.TOTAL_CARD);

    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Card> placedCards = new ArrayList<Card>(GlobalConstants.TOTAL_CARD);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GridSquare> gridSquares = new ArrayList<GridSquare>(GlobalConstants.TOTAL_CARD);

    public Board() {
        initializeCentralCardPile();
        // initializeSquareColors();
        // initializePlacedCards();
        initializeGridSquares();
    }

    private void initializeCentralCardPile() {
        String[] colors = {"red", "green", "blue", "white"};
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            int points = 1 + random.nextInt(5);
            String color = colors[random.nextInt(colors.length)];
            Card card = new Card(color, points);
            centralCardPile.add(card);
        }
    }

    // private void initializeSquareColors() {
    //   Random random = new Random();
    //   String[] colors = {"red", "green", "blue", "white"};
    //   for (int i = 0; i < GlobalConstants.TOTAL_CARD; i++) {
    //       squareColors.add(colors[random.nextInt(colors.length)]); // Defaulting all to "Default Color" for simplicity
    //   }
    // }

    // private void initializePlacedCards() {
    //     for (int i = 0; i < GlobalConstants.TOTAL_CARD; i++) {
    //         placedCards.add(null); // Initialize all positions with null indicating no card is placed
    //     }
    // }

    private void initializeGridSquares(){
      Random random = new Random();
      String[] colors = {"red", "green", "blue","white"};
      for (int i = 0; i < GlobalConstants.TOTAL_CARD; i++){
        GridSquare gridsquare = new GridSquare();
        gridsquare.setColor(colors[random.nextInt(colors.length)]);
        gridsquare.setCard(null);
        this.gridSquares.add(gridsquare);
      }
    }

    public Long getId() {
        return id;
    }

    public List<Card> getCentralCardPile() {
        return centralCardPile;
    }

    public String getSquareColor(int index) {
        return gridSquares.get(index).getColor();
        // return squareColors.get(index);
    }

    // public List<String> getSquareColors(){
    //   return this.squareColors;
    // }

    public boolean isSquareOccupied(int position) {
        return gridSquares.get(position).getCard() != null;
        // return placedCards.get(position) != null;
    }

    public void setCardAtPosition(Card card, int position) {
      gridSquares.get(position).setCard(card);
      // placedCards.set(position, card);
    }

    public List<Card> getPlacedCards() {
      List<Card> placeCards = new ArrayList<>(GlobalConstants.TOTAL_CARD);
      for (int i = 0; i < gridSquares.size(); i++){
        placeCards.add(gridSquares.get(i).getCard());
      }
        return placeCards;
      // return this.placedCards;
    }

    public Card drawCardFromPile() {
        System.out.println(getCentralCardPile().size());
        if (!centralCardPile.isEmpty()) {
            return centralCardPile.remove(0);
        }
        return null; // or throw an exception if you prefer
    }

    public List<GridSquare> getGridSquares(){
      return this.gridSquares;
    }


}
