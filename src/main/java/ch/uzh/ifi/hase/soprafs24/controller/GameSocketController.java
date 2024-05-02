/*
package ch.uzh.ifi.hase.soprafs24.controller;

package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.events.GameStateChangeEvent;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GameWebsocketController {

    @Autowired
    private SimpMessagingTemplate msgTemplate;

    private final GameService gameService;

    private final MatchService matchService;

    public GameWebsocketController(GameService gameService, MatchService matchService) {
        this.gameService = gameService;
        this.matchService = matchService;
    }

    @EventListener
    public void handleGameStateChange(GameStateChangeEvent gameStateChangeEvent) {
        this.updateGameState(gameStateChangeEvent.getGameId(), gameStateChangeEvent.getGameState());
    }

    @MessageMapping("/games/{lobbyId}/start")
    public void startGame(@DestinationVariable String lobbyId) {
        // Retrieve game settings and players from the lobby
        Lobby lobby = matchService.getLobbyById(lobbyId);
        GameSettings settings = lobby.getSettings();
        List<Player> players = lobby.getPlayers();
        // Start the game
        gameService.runGame(lobbyId, settings, players);
    }

    @MessageMapping("/games/{lobbyId}/close-inputs")
    public void closeInputs(@DestinationVariable String lobbyId) {
        gameService.closeInputs(lobbyId);
    }

    @MessageMapping("/games/{lobbyId}/answers/{username}")
    public void receiveAnswers(@DestinationVariable String lobbyId,
                               @DestinationVariable String username,
                               @Payload List<Answer> answers
    ) {
        // the category string is converted to an instance in the ANSWER class
        gameService.setAnswers(lobbyId, username, answers);
    }

    @MessageMapping("/games/{gameId}/state")
    public void updateClients(@DestinationVariable  String gameId) {
        GameState gameState = gameService.getGameState(gameId);
        updateGameState(gameId, gameState);
    }

    @MessageMapping("/games/{lobbyId}/doubt/{username}")
    public void receivedDoubt(@DestinationVariable String lobbyId,
                              @DestinationVariable String username,
                              @Payload List<Vote> votes) {
        gameService.doubtAnswers(lobbyId, username, votes);
    }


    */

    /*private void updateGameState(String lobbyId, GameState gameState) {
        GameStateDTO gameStateDTO = new GameStateDTO();
        // convert Players
        List<PlayerGetDTO> playerGetDTOS = new ArrayList<>();
        for (Player player : gameState.getPlayers()) {
            playerGetDTOS.add(DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player));
        }
        // set other fields
        gameStateDTO.setGamePhase(gameState.getCurrentPhase());
        gameStateDTO.setCurrentLetter(gameState.getCurrentLetter());
        gameStateDTO.setPlayers(playerGetDTOS);
        gameStateDTO.setCurrentRoundNumber(gameState.getCurrentRoundNumber());

        // send DTO
        msgTemplate.convertAndSend(
                String.format("/topic/games/%s/state", lobbyId),
                gameStateDTO
        );
    }
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.msgTemplate = messagingTemplate;
    }*/


