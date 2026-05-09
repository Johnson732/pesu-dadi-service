package com.pesudadi;

import static org.assertj.core.api.Assertions.assertThat;

import com.pesudadi.dto.CreateSessionResponse;
import com.pesudadi.dto.SessionStateResponse;
import com.pesudadi.dto.StartChatRequest;
import com.pesudadi.model.AgeRange;
import com.pesudadi.model.SessionStatus;
import com.pesudadi.service.MatchmakingService;
import com.pesudadi.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PesuDaDiApplicationTests {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MatchmakingService matchmakingService;

    @Test
    void createsAnonymousSession() {
        CreateSessionResponse response = sessionService.createSession();

        assertThat(response.sessionId()).isNotBlank();
        assertThat(response.status()).isEqualTo(SessionStatus.IDLE);
    }

    @Test
    void matchesTwoUsersWhenSecondUserStartsChat() {
        CreateSessionResponse first = sessionService.createSession();
        CreateSessionResponse second = sessionService.createSession();

        SessionStateResponse firstState = matchmakingService.startChat(
                new StartChatRequest(first.sessionId(), com.pesudadi.model.Gender.MALE, AgeRange.EIGHTEEN_TO_TWENTY_FOUR)
        );
        SessionStateResponse secondState = matchmakingService.startChat(
                new StartChatRequest(second.sessionId(), com.pesudadi.model.Gender.FEMALE, AgeRange.TWENTY_FIVE_TO_THIRTY_FOUR)
        );

        assertThat(firstState.status()).isEqualTo(SessionStatus.SEARCHING);
        assertThat(secondState.status()).isEqualTo(SessionStatus.MATCHED);
        assertThat(sessionService.getSession(first.sessionId()).getStatus()).isEqualTo(SessionStatus.MATCHED);
        assertThat(sessionService.getSession(second.sessionId()).getStatus()).isEqualTo(SessionStatus.MATCHED);
        assertThat(sessionService.getSession(first.sessionId()).getCurrentRoomId()).isNotBlank();
        assertThat(sessionService.getSession(second.sessionId()).getCurrentRoomId()).isEqualTo(
                sessionService.getSession(first.sessionId()).getCurrentRoomId()
        );
    }
}
