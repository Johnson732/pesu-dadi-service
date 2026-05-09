package com.pesudadi.repository;

import com.pesudadi.model.UserSession;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySessionRepository {

    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession save(UserSession session) {
        sessions.put(session.getSessionId(), session);
        return session;
    }

    public Optional<UserSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
