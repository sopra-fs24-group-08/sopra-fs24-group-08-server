/*
package ch.uzh.ifi.hase.soprafs24.EventListener;

import ch.uzh.ifi.hase.soprafs24.EventListener.GameFinishedEvent;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.gamesocket.mapper.DTOSocketMapper;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;
    private finale GameService gameservice;

    public GameEventListener (SimpMessagingTemplate messagingTemplate, PlayerRepository playerRepository) {
        this.messagingTemplate = messagingTemplate;
        this.playerRepository = playerRepository;
    }


    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        Game game = event.getGame();
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            Long playerId = player.getId();
            notifyGameEnd(DTOSocketMapper.INSTANCE.convertEntityToGameStateDTOForPlayer(game, playerId),playerId);
            gameService.

        }

    }

    private void notifyGameEnd(GameStateDTO finalGameState,Long playerId) {
        messagingTemplate.convertAndSend("/topic/game/" + finalGameState.getGameId()+"/"+playerId, finalGameState);
        System.out.println(finalGameState.getGameId()+" Messaging Player from this Game about it ending");

    }
}*/
