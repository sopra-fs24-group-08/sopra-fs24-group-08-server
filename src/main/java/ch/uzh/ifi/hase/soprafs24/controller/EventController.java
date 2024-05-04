package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ch.uzh.ifi.hase.soprafs24.entity.GameState;
import ch.uzh.ifi.hase.soprafs24.rest.dto.BoardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.service.SseManagerService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class EventController {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final SseManagerService sseManagerService;
  private final GameService gameService;

  private EventController(SseManagerService sseManagerService, GameService gameService) {
      this.sseManagerService = sseManagerService;
      this.gameService = gameService;
  }
    

  @GetMapping("/stream/{gameId}/{userId}")
  public SseEmitter stream(@PathVariable Long gameId, @PathVariable Long userId) {
    SseEmitter sseEmitter = new SseEmitter(GlobalConstants.SSE_TIMEOUT);

    sseEmitter.onCompletion(() -> {
      log.info("[{}]结束连接...................", userId);
      sseManagerService.removeEmitter(userId);
    });
    //超时回调
    sseEmitter.onTimeout(() -> {
      log.info("[{}]连接超时...................", userId);
    });
    //异常回调
    sseEmitter.onError(
      throwable -> {
          try {
            log.info("[{}]连接异常,{}", userId, throwable.toString());
            sseEmitter.send(SseEmitter.event()
                    .id(userId.toString())
                    .name("发生异常！")
                    .data("发生异常请重试！")
                    .reconnectTime(3000));
                    sseManagerService.removeEmitter(userId);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
    );
    try {
      executor.execute(() -> {sseManagerService.sendMessage(gameId, userId, sseEmitter);});
      sseEmitter.send(SseEmitter.event().reconnectTime(5000));
    } catch (IOException e) {
      e.printStackTrace();
    }
    sseManagerService.addEmitter(userId, sseEmitter);
    log.info("[{}]创建sse连接成功!", userId);
    return sseEmitter;
  }
}

