package com.pesudadi.dto;

import com.pesudadi.model.SessionStatus;

public record SessionStateResponse(
        SessionStatus status
) {
}
