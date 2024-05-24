package ch.uzh.ifi.hase.soprafs24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerQueueServiceTest {

    private PlayerQueueService playerQueueService;

    @BeforeEach
    void setUp() {
        playerQueueService = new PlayerQueueService();
    }

    @Test
    void testAddToQueue() {
        Long playerId = 1L;
        playerQueueService.addToQueue(playerId);
        assertTrue(playerQueueService.getQueue().containsKey(playerId));
    }

    @Test
    void testRemoveFromQueue() {
        Long playerId = 1L;
        playerQueueService.addToQueue(playerId);
        playerQueueService.removeFromQueue(playerId);
        assertFalse(playerQueueService.getQueue().containsKey(playerId));
    }

    @Test
    void testIsEligibleForMatch_NotEligible() {
        playerQueueService.addToQueue(1L);
        assertFalse(playerQueueService.isEligibleForMatch());
    }

    @Test
    void testIsEligibleForMatch_Eligible() {
        playerQueueService.addToQueue(1L);
        playerQueueService.addToQueue(2L);
        assertTrue(playerQueueService.isEligibleForMatch());
    }

    @Test
    void testQueueIntegrityAfterOperations() {
        playerQueueService.addToQueue(1L);
        playerQueueService.addToQueue(2L);
        playerQueueService.removeFromQueue(1L);

        Map<Long, String> queue = playerQueueService.getQueue();
        assertEquals(1, queue.size());
        assertTrue(queue.containsKey(2L));
    }
}
