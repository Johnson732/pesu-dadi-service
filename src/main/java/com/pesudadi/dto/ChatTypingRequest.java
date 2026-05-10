package com.pesudadi.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatTypingRequest(
        @NotBlank String sessionId,
        @NotBlank String roomId,
        boolean typing
) {
}
