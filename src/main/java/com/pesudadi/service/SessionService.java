package com.pesudadi.service;

import com.pesudadi.dto.CreateSessionResponse;
import com.pesudadi.exception.ResourceNotFoundException;
import com.pesudadi.model.AgeRange;
import com.pesudadi.model.ChatRoom;
import com.pesudadi.model.Gender;
import com.pesudadi.model.SessionStatus;
import com.pesudadi.model.UserSession;
import com.pesudadi.repository.InMemoryChatRoomRepository;
import com.pesudadi.repository.InMemorySessionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final InMemorySessionRepository sessionRepository;
    private final InMemoryChatRoomRepository chatRoomRepository;

    public SessionService(InMemorySessionRepository sessionRepository, InMemoryChatRoomRepository chatRoomRepository) {
        this.sessionRepository = sessionRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    public CreateSessionResponse createSession() {
        UserSession session = new UserSession(UUID.randomUUID().toString(), SessionStatus.IDLE, true, Instant.now());
        sessionRepository.save(session);
        return new CreateSessionResponse(session.getSessionId(), session.getStatus());
    }

    public UserSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
    }

    public void updatePreferences(String sessionId, Gender gender, AgeRange ageRange) {
        UserSession session = getSession(sessionId);
        session.setGender(gender);
        session.setAgeRange(ageRange);
    }

    public void attachRoom(String sessionId, String roomId) {
        UserSession session = getSession(sessionId);
        session.setCurrentRoomId(roomId);
    }

    public void detachRoom(String sessionId) {
        UserSession session = getSession(sessionId);
        session.setCurrentRoomId(null);
    }

    public void saveRoom(ChatRoom room) {
        chatRoomRepository.save(room);
    }

    public ChatRoom getRoom(String roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found: " + roomId));
    }

    public void removeRoom(String roomId) {
        chatRoomRepository.remove(roomId);
    }
}
