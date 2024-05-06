package ch.uzh.ifi.hase.soprafs24.entity;

public class MatchmakingResult {
    private final boolean matchFound;
    private final Long gameId;
    private final boolean isFirst;
    private final Long opponentId;
    private final String opponentName;

    public MatchmakingResult(boolean matchFound, Long gameId, boolean isFirst, Long opponentId,String opponentName) {
        this.matchFound = matchFound;
        this.gameId = gameId;
        this.isFirst = isFirst;
        this.opponentId = opponentId;
        this.opponentName = opponentName;
    }

    public String getOpponentName() {
        return opponentName;
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

