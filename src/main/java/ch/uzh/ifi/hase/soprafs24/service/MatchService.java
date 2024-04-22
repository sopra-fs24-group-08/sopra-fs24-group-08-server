package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;

@Service
@Transactional
public class MatchService {

    // private LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final GameService gameService;

    @Autowired
    public MatchService(@Qualifier("userRepository") UserRepository userRepository, GameService gameService) {
        this.userRepository = userRepository;
        this.gameService = gameService;
    }
    private final ConcurrentHashMap<Long, DeferredResult<GameMatchResultDTO>> presenceMap = new ConcurrentHashMap<>();// in <Long, User>, Long is the type of userId.
    private final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

    //put user into queue
    public void addUserToQueue(Long userId,DeferredResult<GameMatchResultDTO> deferredResult){
      User user = userRepository.findByid(userId);
      if (user.getInGame()){
        throw new ResponseStatusException(HttpStatus.CONFLICT, "This game invitation has already been processed.");
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
            game = gameService.startGame(firstId, secondId, game.getGameId());
            completeDeferredResult(firstId, secondId);
            completeDeferredResult(secondId, firstId);
        } else {
            // haven't delete them from hashmap
            if (firstId != null) queue.offer(firstId);
            if (secondId != null) queue.offer(secondId);
            break;
        }
      }
    }

    private void completeDeferredResult(Long userId, Long opponentId) {
      DeferredResult<GameMatchResultDTO> deferredResult = presenceMap.remove(userId);
      GameMatchResultDTO gameMatchResultDTO = new GameMatchResultDTO();
      User user = userRepository.findByid(userId);
      User opponent = userRepository.findByid(opponentId);
      if (user != null && opponent != null){
        gameMatchResultDTO.setUserId1(userId);
        gameMatchResultDTO.setUsername1(user.getUsername());
        gameMatchResultDTO.setUserId2(opponentId);
        gameMatchResultDTO.setUsername2(opponent.getUsername());        
      }else{
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userId can not be found.");
      }
      if (deferredResult != null) {
          deferredResult.setResult(gameMatchResultDTO);
      }
    }

    // cancel a user in queue
    public boolean cancelQueue(Long userId) {
      if (presenceMap.remove(userId) != null) {
          queue.remove(userId);
          return true;
      }
      return false;
  }
}
