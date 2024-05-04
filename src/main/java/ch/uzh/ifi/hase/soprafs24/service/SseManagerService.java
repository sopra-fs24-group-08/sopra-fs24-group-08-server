package ch.uzh.ifi.hase.soprafs24.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GameState;
import ch.uzh.ifi.hase.soprafs24.rest.dto.BoardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
@Transactional
public class SseManagerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
  private GameService gameService;

  public SseManagerService(GameService gameService){
    this.gameService = gameService;
  }

  public void addEmitter(Long userId, SseEmitter emitter) {
      System.out.println("emitter created.");
      this.emitters.put(userId, emitter);
  }

  public SseEmitter getEmitter(Long userId) {
      return this.emitters.get(userId);
  }

  public SseEmitter checkAvaiblableEmitter(Long userId){
    SseEmitter sseEmitter = getEmitter(userId);
    if (sseEmitter == null){
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user has not establish connection with server.");
    }
    return sseEmitter;
  }


  public void sendMessage(Long gameId, Long userId, SseEmitter sseEmitter){
    try {
      Game game = gameService.retrieveGameState(gameId);
      if (game != null){
        Hibernate.initialize(game.getBoard().getGridSquares());
        GameStateDTO gameStateDTO = DTOMapper.INSTANCE.convertEntityToGameStateDTO(game);
        for (PlayerDTO playerdto: gameStateDTO.getPlayers()){
          if (playerdto.getId() != userId){
            playerdto.setCards(null);
          }
        }
        sseEmitter.send(SseEmitter.event().name("GAME_STATE").data(gameStateDTO));
      }else{
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coundn't find the game.");
      }

    } catch (Exception e) {
      sseEmitter.completeWithError(e);
    }
  }

  public void sendMessageAfterUpdate(Long gameId, Long userId){
    System.out.println("****sendMessageAfterUpdate****");
    SseEmitter sseEmitter = checkAvaiblableEmitter(userId);
    Long opponentId = gameService.getOpponent(gameId, userId).getId();
    SseEmitter opponentSseEmitter = checkAvaiblableEmitter(opponentId);
    sendMessage(gameId, userId, sseEmitter);
    sendMessage(gameId, opponentId, opponentSseEmitter);
  }

  public void removeEmitter(Long playerId) {
    SseEmitter emitter = emitters.remove(playerId);
    if (emitter != null) {
        emitter.complete();
    }
  }

  public void cleanUp(Long gameId){
    List<Long> players = gameService.getPlayerId(gameId);
    if (gameService.wipeGame(gameId)){
      for (Long playerId: players){
        removeEmitter(playerId);
      }
    }
  }
}
