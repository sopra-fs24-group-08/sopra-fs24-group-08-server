package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class GameGetDTO {

    private Long id;
    private List<GridRowDTO> grid; // 保留这个属性用于映射

    // 构造函数、getter 和 setter
    public GameGetDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 修正为正确的 GridRowDTO 类型
    public List<GridRowDTO> getGrid() {
        return grid;
    }

    public void setGrid(List<GridRowDTO> grid) {
        this.grid = grid;
    }
}
