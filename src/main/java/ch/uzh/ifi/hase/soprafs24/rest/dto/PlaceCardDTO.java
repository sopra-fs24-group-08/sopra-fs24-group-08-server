package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class PlaceCardDTO {

    private Long playerId;
    private int cardId;
    private int row;
    private int column;

    // 默认构造函数用于JSON反序列化
    public PlaceCardDTO() {
    }

    // getter和setter方法
    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
