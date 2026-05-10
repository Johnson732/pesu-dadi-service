package com.pesudadi.controller;

import com.pesudadi.dto.ChatMessageRequest;
import com.pesudadi.dto.ChatTypingRequest;
import com.pesudadi.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatMessageRequest request) {
        chatService.sendMessage(request);
    }

    @MessageMapping("/chat.typing")
    public void sendTyping(@Valid ChatTypingRequest request) {
        chatService.sendTyping(request);
    }
}
