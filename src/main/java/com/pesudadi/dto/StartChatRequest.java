package com.pesudadi.dto;

import com.pesudadi.model.AgeRange;
import com.pesudadi.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartChatRequest(
        @NotBlank String sessionId,
        @NotNull Gender gender,
        @NotNull AgeRange ageRange
) {
}
