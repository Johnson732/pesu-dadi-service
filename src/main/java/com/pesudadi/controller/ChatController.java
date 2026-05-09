package com.pesudadi.controller;

import com.pesudadi.dto.DisconnectRequest;
import com.pesudadi.dto.SessionStateResponse;
import com.pesudadi.dto.StartChatRequest;
import com.pesudadi.service.MatchmakingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final MatchmakingService matchmakingService;

    public ChatController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SessionStateResponse startChat(@Valid @RequestBody StartChatRequest request) {
        return matchmakingService.startChat(request);
    }

    @PostMapping("/disconnect")
    public SessionStateResponse disconnect(@Valid @RequestBody DisconnectRequest request) {
        return matchmakingService.disconnect(request.sessionId());
    }
}
