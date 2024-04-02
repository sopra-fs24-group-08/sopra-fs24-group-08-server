package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameMoveDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

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
    public void updateGameState(GameMoveDTO move) {
        //Let others work on it
    }

    public Game getGame(Long gameId){
        Game game = gameRepository.findByGameId(gameId);
        return game;
    }
    public void GameState(Long gameId){


    }

    // Other methods...
}
