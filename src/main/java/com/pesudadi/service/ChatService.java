package com.pesudadi.service;

import com.pesudadi.dto.ChatMessageRequest;
import com.pesudadi.dto.ChatMessageResponse;
import com.pesudadi.exception.InvalidRequestException;
import com.pesudadi.exception.ResourceNotFoundException;
import com.pesudadi.model.ChatMessage;
import com.pesudadi.model.ChatRoom;
import java.time.Instant;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(SessionService sessionService, SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    public ChatRoom createRoom(String firstSessionId, String secondSessionId) {
        ChatRoom room = new ChatRoom(UUID.randomUUID().toString(), firstSessionId, secondSessionId, Instant.now(), true);
        sessionService.saveRoom(room);
        sessionService.attachRoom(firstSessionId, room.getRoomId());
        sessionService.attachRoom(secondSessionId, room.getRoomId());
        return room;
    }

    public void sendMessage(ChatMessageRequest request) {
        ChatRoom room = sessionService.getRoom(request.roomId());
        if (!room.isActive()) {
            throw new InvalidRequestException("Chat room is no longer active");
        }
        if (!room.includes(request.sessionId())) {
            throw new InvalidRequestException("Session does not belong to the chat room");
        }

        ChatMessage message = new ChatMessage(
                request.roomId(),
                request.sessionId(),
                request.content().trim(),
                Instant.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + request.roomId(),
                new ChatMessageResponse("CHAT_MESSAGE", message.roomId(), message.senderSessionId(), message.content(), message.timestamp())
        );
    }

    public ChatRoom endRoom(String roomId) {
        ChatRoom room = sessionService.getRoom(roomId);
        room.setActive(false);
        sessionService.detachRoom(room.getUserOneSessionId());
        sessionService.detachRoom(room.getUserTwoSessionId());
        sessionService.removeRoom(roomId);
        return room;
    }

    public String getPartnerSessionId(String roomId, String sessionId) {
        ChatRoom room = sessionService.getRoom(roomId);
        String partnerSessionId = room.partnerFor(sessionId);
        if (partnerSessionId == null) {
            throw new ResourceNotFoundException("Partner not found for session");
        }
        return partnerSessionId;
    }
}
