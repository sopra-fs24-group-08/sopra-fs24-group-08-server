package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GridSquare> grid = new ArrayList<>();
    private int occupiedGridSquare;
    private Long player1Id;
    private Long player2Id;
    private Boolean coinTossResult;
    private Boolean player1sTurn = false;
    private Boolean awaitingPlayerChoice = false;
    private CardPile cardPile;  // 新增：棋盘现包含一个牌堆
    public Board() {
        initializeBoard();
        this.cardPile = new CardPile(); // 初始化牌堆
    }
    private void initializeBoard() {
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
    public String checkGridColor(Integer position) {
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

    public List<GridSquare> getGrid() {return this.grid; }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }

    public Boolean getCoinTossResult() {
        return coinTossResult;
    }

    public void setCoinTossResult(Boolean coinTossResult) {
        this.coinTossResult = coinTossResult;
    }

    public Boolean getPlayer1sTurn() {
        return player1sTurn;
    }

    public void setPlayer1sTurn(Boolean player1sTurn) {
        this.player1sTurn = player1sTurn;
    }

    public Boolean getAwaitingPlayerChoice() {
        return awaitingPlayerChoice;
    }

    public void setAwaitingPlayerChoice(Boolean awaitingPlayerChoice) {
        this.awaitingPlayerChoice = awaitingPlayerChoice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
