package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Embeddable;

@Embeddable
public class GridRow {
    private Integer cell1;
    private Integer cell2;
    private Integer cell3;

    // Constructors, getters, and setters
    public GridRow() {
        // Default constructor needed by JPA
    }

    public GridRow(Integer cell1, Integer cell2, Integer cell3) {
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.cell3 = cell3;
    }

    // Getters and setters for each cell
    public Integer getCell1() { return cell1; }
    public void setCell1(Integer cell1) { this.cell1 = cell1; }

    public Integer getCell2() { return cell2; }
    public void setCell2(Integer cell2) { this.cell2 = cell2; }

    public Integer getCell3() { return cell3; }
    public void setCell3(Integer cell3) { this.cell3 = cell3; }
}
