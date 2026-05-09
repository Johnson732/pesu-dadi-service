package com.pesudadi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank String sessionId,
        @NotBlank String roomId,
        @NotBlank @Size(max = 1000) String content
) {
}
