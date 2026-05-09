package com.pesudadi.service;

import com.pesudadi.dto.SessionEventResponse;
import com.pesudadi.dto.SessionStateResponse;
import com.pesudadi.dto.StartChatRequest;
import com.pesudadi.model.ChatRoom;
import com.pesudadi.model.SessionStatus;
import com.pesudadi.model.UserSession;
import com.pesudadi.repository.InMemoryQueueRepository;
import java.util.Optional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchmakingService {

    private final SessionService sessionService;
    private final InMemoryQueueRepository queueRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchmakingService(
            SessionService sessionService,
            InMemoryQueueRepository queueRepository,
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.sessionService = sessionService;
        this.queueRepository = queueRepository;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized SessionStateResponse startChat(StartChatRequest request) {
        UserSession userSession = sessionService.getSession(request.sessionId());
        sessionService.updatePreferences(userSession.getSessionId(), request.gender(), request.ageRange());

        if (userSession.getCurrentRoomId() != null) {
            closeRoomAndNotifyPartner(userSession.getCurrentRoomId(), userSession.getSessionId());
        } else {
            queueRepository.remove(userSession.getSessionId());
        }

        userSession.setConnected(true);
        Optional<String> partnerSessionId = queueRepository.popRandomExcluding(userSession.getSessionId());

        if (partnerSessionId.isPresent()) {
            UserSession partner = sessionService.getSession(partnerSessionId.get());
            ChatRoom room = chatService.createRoom(userSession.getSessionId(), partner.getSessionId());

            userSession.setStatus(SessionStatus.MATCHED);
            partner.setStatus(SessionStatus.MATCHED);

            sendSessionEvent(
                    userSession.getSessionId(),
                    "MATCH_FOUND",
                    SessionStatus.MATCHED,
                    room.getRoomId(),
                    "Partner found",
                    partner.getGender()
            );
            sendSessionEvent(
                    partner.getSessionId(),
                    "MATCH_FOUND",
                    SessionStatus.MATCHED,
                    room.getRoomId(),
                    "Partner found",
                    userSession.getGender()
            );

            return new SessionStateResponse(SessionStatus.MATCHED);
        }

        userSession.setStatus(SessionStatus.SEARCHING);
        queueRepository.add(userSession.getSessionId());
        sendSessionEvent(userSession.getSessionId(), "SEARCHING", SessionStatus.SEARCHING, null, "Searching for a partner", null);
        return new SessionStateResponse(SessionStatus.SEARCHING);
    }

    public synchronized SessionStateResponse disconnect(String sessionId) {
        UserSession userSession = sessionService.getSession(sessionId);
        queueRepository.remove(sessionId);

        if (userSession.getCurrentRoomId() != null) {
            closeRoomAndNotifyPartner(userSession.getCurrentRoomId(), sessionId);
        }

        userSession.setStatus(SessionStatus.DISCONNECTED);
        userSession.setConnected(false);
        sendSessionEvent(sessionId, "DISCONNECTED", SessionStatus.DISCONNECTED, null, "Session disconnected", null);
        return new SessionStateResponse(SessionStatus.DISCONNECTED);
    }

    void handleUnexpectedDisconnect(String sessionId) {
        UserSession userSession = sessionService.getSession(sessionId);
        if (!userSession.isConnected() && userSession.getStatus() == SessionStatus.DISCONNECTED) {
            return;
        }
        disconnect(sessionId);
    }

    private void closeRoomAndNotifyPartner(String roomId, String initiatorSessionId) {
        String partnerSessionId = chatService.getPartnerSessionId(roomId, initiatorSessionId);
        chatService.endRoom(roomId);

        UserSession partner = sessionService.getSession(partnerSessionId);
        partner.setStatus(SessionStatus.IDLE);
        sendSessionEvent(partnerSessionId, "PARTNER_DISCONNECTED", SessionStatus.IDLE, null, "Your partner disconnected", null);
    }

    private void sendSessionEvent(
            String sessionId,
            String type,
            SessionStatus status,
            String roomId,
            String message,
            com.pesudadi.model.Gender partnerGender
    ) {
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                new SessionEventResponse(type, status, sessionId, roomId, message, partnerGender)
        );
    }
}
