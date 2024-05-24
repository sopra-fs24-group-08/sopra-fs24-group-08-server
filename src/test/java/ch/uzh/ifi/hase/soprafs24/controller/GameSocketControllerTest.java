package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.SurrenderConfirmation;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websocket.WebSocketTestBase;
import ch.uzh.ifi.hase.soprafs24.util.MoveDTOMatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import static org.mockito.Mockito.*;

public class GameSocketControllerTest extends WebSocketTestBase {

    @MockBean
    private GameService gameService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void testHandleMove() throws Exception {
        MoveDTO expectedMove = new MoveDTO();
        expectedMove.setPlayerId(1L);
        expectedMove.setCardId(2L);
        expectedMove.setPosition(5);
        expectedMove.setMoveType(MoveType.PLACE);

        Long gameId = 1L;

        StompSession stompSession = connectToWebSocket("validToken");
        stompSession.send("/app/game/" + gameId + "/move", expectedMove);
        Thread.sleep(100); // Short delay to allow async handling

        verify(gameService).processMove(eq(gameId), argThat(new MoveDTOMatcher(expectedMove)));
    }

    @Test
    public void testHandlePlayerSurrender() throws Exception {
        String validToken = "validToken";
        Long gameId = 1L;
        SurrenderConfirmation surrenderConfirmation = new SurrenderConfirmation();
        surrenderConfirmation.setPlayerId(gameId);
        surrenderConfirmation.setSurrender(true);

        StompSession stompSession = connectToWebSocket(validToken);
        stompSession.send("/app/game/" + gameId + "/surrender", surrenderConfirmation);
        Thread.sleep(100);

        verify(gameService, times(1)).handlePlayerSurrender(gameId, surrenderConfirmation.getPlayerId());
    }
}
