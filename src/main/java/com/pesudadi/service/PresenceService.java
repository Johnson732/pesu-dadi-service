package com.pesudadi.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class PresenceService {

    private final MatchmakingService matchmakingService;

    public PresenceService(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getFirstNativeHeader("sessionId");
        if (sessionId != null && !sessionId.isBlank()) {
            matchmakingService.handleUnexpectedDisconnect(sessionId);
        }
    }
}
