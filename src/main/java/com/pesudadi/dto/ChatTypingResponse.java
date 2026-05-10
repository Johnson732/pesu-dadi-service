package com.pesudadi.dto;

import java.time.Instant;

public record ChatTypingResponse(
        String type,
        String roomId,
        String senderSessionId,
        boolean typing,
        Instant timestamp
) {
}
