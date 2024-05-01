package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Service
public class EventService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SseManagerService sseManagerService;

    public EventService(SseManagerService sseManagerService) {
        this.sseManagerService = sseManagerService;
    }

    public void sendToUser(Long userId, GameState gameState) {
        SseEmitter emitter = sseManagerService.getEmitter(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(gameState));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        } else {
            log.error("No emitter found for userId: {}", userId);
        }
    }
}

