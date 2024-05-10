package ch.uzh.ifi.hase.soprafs24.EventListener;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.context.ApplicationEvent;

public class GameEndEvent extends ApplicationEvent {
    private final Game game;
    private final Player winner;
    private final Player loser;

    public GameEndEvent(Object source, Game game, Player winner, Player loser) {
        super(source);
        this.game = game;
        this.winner = winner;
        this.loser = loser;
    }

    public Game getGame() {
        return game;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getLoser() {
        return loser;
    }
}
