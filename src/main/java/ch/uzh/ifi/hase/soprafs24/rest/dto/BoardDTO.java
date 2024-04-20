package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class BoardDTO {
    public List<GridSquareDTO> getGridSquares() {
        return gridSquares;
    }

    public void setGridSquares(List<GridSquareDTO> gridSquares) {
        this.gridSquares = gridSquares;
    }

    private List<GridSquareDTO> gridSquares;


}
