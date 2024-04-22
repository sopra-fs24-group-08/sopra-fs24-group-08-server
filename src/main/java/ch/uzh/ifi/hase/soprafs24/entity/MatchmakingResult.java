package ch.uzh.ifi.hase.soprafs24.entity;

public class MatchmakingResult {
    private boolean matchFound;
    private Long gameId;
    private Long firstPlayerId;
    private Long secondPlayerId;

    public MatchmakingResult(boolean matchFound, Long gameId, Long firstPlayerId, Long secondPlayerId) {
        this.matchFound = matchFound;
        this.gameId = gameId;
        this.firstPlayerId = firstPlayerId;
        this.secondPlayerId = secondPlayerId;
    }

    public boolean isMatchFound() {
        return matchFound;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getFirstPlayerId() {
        return firstPlayerId;
    }

    public Long getSecondPlayerId() {
        return secondPlayerId;
    }
}
