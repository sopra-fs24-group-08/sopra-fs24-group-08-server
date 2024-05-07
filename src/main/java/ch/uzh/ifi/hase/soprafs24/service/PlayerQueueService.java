package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerQueueService {
    private final Map<Long, String> playerQueue = new ConcurrentHashMap<>();

    public void addToQueue(Long playerId) {
        playerQueue.put(playerId, "");
    }

    public void removeFromQueue(Long playerId) {
        playerQueue.remove(playerId);
    }

    public boolean isEligibleForMatch() {
        return playerQueue.size() >= 2;
    }

    public Map<Long, String> getQueue() {
        return new ConcurrentHashMap<>(playerQueue);
    }
}