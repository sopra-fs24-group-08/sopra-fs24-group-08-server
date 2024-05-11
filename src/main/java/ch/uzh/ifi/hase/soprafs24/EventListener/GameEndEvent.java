package ch.uzh.ifi.hase.soprafs24.EventListener;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class GameEndEvent extends ApplicationEvent {
    private final Game game;
    private final Player winner;
    private final Player loser;
    private final Map<Long, GameStateDTO> gameStateDTOs;

    public GameEndEvent(Object source, Game game, Player winner, Player loser, Map<Long, GameStateDTO> gameStateDTOs) {
        super(source);
        this.game = game;
        this.winner = winner;
        this.loser = loser;
        this.gameStateDTOs = gameStateDTOs;
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

    public Map<Long, GameStateDTO> getGameStateDTOs() {
        return gameStateDTOs;
    }
}
