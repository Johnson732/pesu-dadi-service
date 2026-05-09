package com.pesudadi.controller;

import com.pesudadi.dto.ChatMessageRequest;
import com.pesudadi.dto.DisconnectRequest;
import com.pesudadi.service.ChatService;
import com.pesudadi.service.MatchmakingService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final MatchmakingService matchmakingService;

    public ChatWebSocketController(ChatService chatService, MatchmakingService matchmakingService) {
        this.chatService = chatService;
        this.matchmakingService = matchmakingService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatMessageRequest request) {
        chatService.sendMessage(request);
    }

    @MessageMapping("/chat.disconnect")
    public void disconnect(@Valid DisconnectRequest request) {
        matchmakingService.disconnect(request.sessionId());
    }
}
