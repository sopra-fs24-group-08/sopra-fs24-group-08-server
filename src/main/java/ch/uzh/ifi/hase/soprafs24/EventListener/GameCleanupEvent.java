import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.springframework.context.ApplicationEvent;

public class GameCleanupEvent extends ApplicationEvent {
    private final Game game;

    public GameCleanupEvent(Object source, Game game) {
        super(source);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
