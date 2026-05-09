package com.pesudadi.dto;

import jakarta.validation.constraints.NotBlank;

public record DisconnectRequest(@NotBlank String sessionId) {
}
