package ch.uzh.ifi.hase.soprafs24.entity;

public class MatchmakingResult {
    private final boolean matchFound;
    private final Long gameId;
    private final boolean isFirst;
    private final Long opponentId;

    public MatchmakingResult(boolean matchFound, Long gameId, boolean isFirst, Long opponentId) {
        this.matchFound = matchFound;
        this.gameId = gameId;
        this.isFirst = isFirst;
        this.opponentId = opponentId;
    }

    public boolean isMatchFound() {
        return matchFound;
    }

    public Long getGameId() {
        return gameId;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public Long getOpponentId() {
        return opponentId;
    }
}

