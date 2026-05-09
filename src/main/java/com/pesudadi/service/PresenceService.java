package com.pesudadi.service;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class PresenceService {

    private static final long DISCONNECT_GRACE_PERIOD_SECONDS = 10;

    private final MatchmakingService matchmakingService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "presence-disconnect-grace");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();

    public PresenceService(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getFirstNativeHeader("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        ScheduledFuture<?> pendingTask = pendingDisconnects.remove(sessionId);
        if (pendingTask != null) {
            pendingTask.cancel(false);
        }

        matchmakingService.markConnected(sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getFirstNativeHeader("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        ScheduledFuture<?> previousTask = pendingDisconnects.remove(sessionId);
        if (previousTask != null) {
            previousTask.cancel(false);
        }

        ScheduledFuture<?> pendingTask = scheduler.schedule(() -> {
            pendingDisconnects.remove(sessionId);
            matchmakingService.handleUnexpectedDisconnect(sessionId);
        }, DISCONNECT_GRACE_PERIOD_SECONDS, TimeUnit.SECONDS);

        pendingDisconnects.put(sessionId, pendingTask);
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
