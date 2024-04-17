package ch.uzh.ifi.hase.soprafs24.entity.GameElements;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public final List<GridSquare> grid;
    public int occupiedGridSquare;

    public Board() {
        this.grid = new ArrayList<>();
        this.occupiedGridSquare = 0;
        for (int i = 0; i < 9; i++) {
            if (i==4){
                this.grid.add(null);
            }else {
                this.grid.add(new GridSquare());
            }
        }
    }
    public String getGridColor(Integer position) {
        GridSquare square = grid.get(position);
        if (square == null) {
            return null;
        }
        return square.getColor();
    }
    public boolean placeCard(Card card, Integer position) {
        if (position < 0 || position >= grid.size() || grid.get(position) == null) {
            return false;
        }
        GridSquare square = grid.get(position);
        if (square.getOccupied()) {
            return false;
        } else {
            square.setOccupied();
            occupiedGridSquare +=1;
            return true;
        }
    }
    public Boolean isFull(){
        return occupiedGridSquare == (grid.size() - 1);
    }

    public GridSquare getGrid(Integer position) {
        return grid.get(position);
    }
}
