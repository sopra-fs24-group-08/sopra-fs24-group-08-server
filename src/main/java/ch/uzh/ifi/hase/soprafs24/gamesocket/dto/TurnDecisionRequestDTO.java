package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;



public class TurnDecisionRequestDTO {
    private Long gameId;
    private Long enemyId;
    private String message;

    public TurnDecisionRequestDTO(Long gameId, Long enemyId, String message) {
        this.gameId = gameId;
        this.enemyId = enemyId;
        this.message = message;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(Long enemyId) {
        this.enemyId = enemyId;
    }

}
