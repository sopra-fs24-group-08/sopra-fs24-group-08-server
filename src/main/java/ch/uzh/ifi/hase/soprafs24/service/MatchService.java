package ch.uzh.ifi.hase.soprafs24.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.SseManagerService;

@Service
@Transactional
public class MatchService {

  // private LobbyRepository lobbyRepository;
  private final UserRepository userRepository;
  private final GameService gameService;
  private final PlayerRepository playerRepository;
  private final SseManagerService sseManagerService;

  @Autowired
  public MatchService(@Qualifier("userRepository") UserRepository userRepository, 
                      @Qualifier("playerRepository") PlayerRepository playerRepository,GameService gameService, 
                      SseManagerService sseManagerService) {
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.gameService = gameService;
    this.sseManagerService = sseManagerService;
  }
  private final ConcurrentHashMap<Long, DeferredResult<GameMatchResultDTO>> presenceMap = new ConcurrentHashMap<>();// in <Long, User>, Long is the type of userId.
  private final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

  //put user into queue
  public void addUserToQueue(Long userId,DeferredResult<GameMatchResultDTO> deferredResult){
    User user = userRepository.findByid(userId);
    Player player = playerRepository.findByUser(user);
    if (player != null){
      throw new ResponseStatusException(HttpStatus.CONFLICT, "You can't get in queue while playing a game!");
    }
    if (presenceMap.putIfAbsent(userId, deferredResult) == null){
      queue.offer(userId);
      // match immidiately
      matchUsers();
    }
  }

  // check queue in a fixed frequency
  @Scheduled(fixedRate = 10000)  // execute every 10 seconds
  public void matchUsers() {
    while (true){
      Long firstId = queue.poll();
      Long secondId = queue.poll();
      if (firstId != null && secondId != null) {
        //create a game, start a game.
        System.out.println("Matched " + firstId + " with " + secondId);
        Game game = gameService.createGame();
        System.out.printf("create a game with Id: %d\n", game.getGameId());
        game = gameService.startGame(game.getGameId(), firstId, secondId);
        System.out.printf("####2##board: %s", game.getBoard().getSquareColor(0));
        completeDeferredResult(firstId, secondId, game.getGameId());
        completeDeferredResult(secondId, firstId, game.getGameId());
      } else {
        // haven't delete them from hashmap
        if (firstId != null) queue.offer(firstId);
        if (secondId != null) queue.offer(secondId);
        break;
      }
    }
  }

  private void completeDeferredResult(Long userId, Long opponentId, Long gameId) {
    DeferredResult<GameMatchResultDTO> deferredResult = presenceMap.remove(userId);
    User user = userRepository.findByid(userId);
    User opponent = userRepository.findByid(opponentId);
    GameMatchResultDTO gameMatchResultDTO = new GameMatchResultDTO();
    if (user != null && opponent != null){
      gameMatchResultDTO = setGameMatchResultDTO(user, opponent, gameId);    
    }else{
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userId can not be found.");
    }
    if (deferredResult != null) {
      deferredResult.setResult(gameMatchResultDTO);
    }
  }

  // cancel a user in queue
  public String cancelQueue(Long userId) {
    DeferredResult<GameMatchResultDTO> deferredResult = presenceMap.remove(userId);
    GameMatchResultDTO gameMatchResultDTO = new GameMatchResultDTO();
    if (deferredResult != null) {
      queue.remove(userId);
      deferredResult.setResult(gameMatchResultDTO);
      return "You're leave the queue now.";
    }
    return "You're not in the queue before.";
  }

  //set game match result dto
  public GameMatchResultDTO setGameMatchResultDTO(User user1, User user2, Long gameId){
    GameMatchResultDTO gameMatchResultDTO = new GameMatchResultDTO();
    gameMatchResultDTO.setUserId1(user1.getId());
    gameMatchResultDTO.setUsername1(user1.getUsername());
    gameMatchResultDTO.setUserId2(user2.getId());
    gameMatchResultDTO.setUsername2(user2.getUsername());  
    gameMatchResultDTO.setGameId(gameId); 
    return gameMatchResultDTO;     
  }

}
