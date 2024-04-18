package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.entity.Card;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GridSquare> squares;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public List<GridSquare> getSquares() {
        return squares;
    }

    public void setSquares(List<GridSquare> squares) {
        this.squares = squares;
    }

    public GridSquare getSquareAt(int index) {
        if (index >= 0 && index < 9) {
            return squares.get(index);
        } else {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + squares.size());
        }
    }


}