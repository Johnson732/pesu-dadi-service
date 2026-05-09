package com.pesudadi.model;

import java.time.Instant;

public record ChatMessage(
        String roomId,
        String senderSessionId,
        String content,
        Instant timestamp
) {
}
