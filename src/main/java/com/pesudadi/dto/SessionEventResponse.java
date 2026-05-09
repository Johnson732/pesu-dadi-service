package com.pesudadi.dto;

import com.pesudadi.model.Gender;
import com.pesudadi.model.SessionStatus;

public record SessionEventResponse(
        String type,
        SessionStatus status,
        String sessionId,
        String roomId,
        String message,
        Gender partnerGender
) {
}
