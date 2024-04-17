package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;


@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;


    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,UserService userService,PlayerRepository playerRepository,UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public Game createGame(Long gameId){
        //add all the necessary logic for game creation
        Game game  = new Game();
        game = gameRepository.save(game);
        game.setGameId(gameId);
        return game;
    }

    public Game startGame(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        Game game = new Game();
        gameRepository.save(game);

        Player player1 = new Player(user1, game);
        Player player2 = new Player(user2, game);

        playerRepository.save(player1);
        playerRepository.save(player2);

        game.setPlayers(Arrays.asList(player1, player2));
        gameRepository.save(game); // This save might be redundant depending on your cascading settings.

        return game;
    }
    public void updateGameState(MoveDTO move) {
        //Let others work on it
    }

    public Game getGame(Long gameId){
        Game game = gameRepository.findById(gameId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Game was not found"));
        return game;
    }

    public Player getWinningPlayer(Long gameId){
        Game game = getGame(gameId);

        if (game.getPlayers().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No players inside of this game");
        }
        Player player = Collections.max(game.getPlayers(),new Comparator<Player>(){
            public int compare(Player player1,Player player2){
                return player1.getScore() - player2.getScore();
            }
        });
        return player;

    }

    public Player getWinner(Long gameId){
        Game game = getGame(gameId);
        Player player = getWinningPlayer(gameId);
        if (game.getGameStatus() == GameStatus.WINNER){
            return player;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Isn't finished yet");
    }

    public void gameFinished(Game game){
    }
    public void GameState(Long gameId){


    }
    public void startGameSession(Long userId1, Long userId2){
    }

    // Other methods...
}
