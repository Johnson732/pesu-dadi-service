package com.pesudadi.dto;

import com.pesudadi.model.SessionStatus;

public record CreateSessionResponse(
        String sessionId,
        SessionStatus status
) {
}
