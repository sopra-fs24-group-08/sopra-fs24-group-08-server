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

  public void removeEmitter(Long userId) {
      this.emitters.remove(userId);
  }

  public SseEmitter getEmitter(Long userId) {
      return this.emitters.get(userId);
  }

  public void sendToUser(Long userId, GameState gameState) {
  SseEmitter emitter = getEmitter(userId);
  if (emitter != null) {
      System.out.println("found emitter!");
      try {
          emitter.send(SseEmitter.event().data(gameState));
          System.out.println("sent data to client.");
      } catch (IOException e) {
          emitter.completeWithError(e);
      }
  } else {
      log.error("No emitter found for userId: {}", userId);
  }
  }

  public String sendMessage(Long senderId, Long receiverId, GameState gameState) {
      SseEmitter receiver = emitters.get(receiverId);
      if (receiver != null) {
          try {
              receiver.send(SseEmitter.event().name("gamestate").data(gameState));
              return "Message sent!";
          } catch (Exception e) {
              return "Error sending message: " + e.getMessage();
          }
      }
      return "Receiver not connected.";
  }

  public void fastExecution(Long gameId, Long userId, SseEmitter sseEmitter){
    try {
      Game game = gameService.findGame(gameId);
      if (game != null){
        Hibernate.initialize(game.getBoard().getGridSquares());
        // Hibernate.initialize(game.getPlayers());
        // System.out.printf("get board:", game.getBoard().getGridSquares());
        // BoardDTO boardDTO = DTOMapper.INSTANCE.convertEntityBoardDTO(game.getBoard());
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
}
