package com.pesudadi.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryQueueRepository {

    private final List<String> waitingSessionIds = new ArrayList<>();
    private final Random random = new Random();

    public synchronized void add(String sessionId) {
        if (!waitingSessionIds.contains(sessionId)) {
            waitingSessionIds.add(sessionId);
        }
    }

    public synchronized void remove(String sessionId) {
        waitingSessionIds.remove(sessionId);
    }

    public synchronized Optional<String> popRandomExcluding(String excludedSessionId) {
        List<String> candidates = waitingSessionIds.stream()
                .filter(sessionId -> !sessionId.equals(excludedSessionId))
                .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        String selected = candidates.get(random.nextInt(candidates.size()));
        waitingSessionIds.remove(selected);
        return Optional.of(selected);
    }

    public synchronized boolean contains(String sessionId) {
        return waitingSessionIds.contains(sessionId);
    }
}
