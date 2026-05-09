package com.pesudadi.dto;

import java.time.Instant;

public record ChatMessageResponse(
        String type,
        String roomId,
        String senderSessionId,
        String content,
        Instant timestamp
) {
}
