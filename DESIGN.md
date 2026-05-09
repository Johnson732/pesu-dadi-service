# PesuDaDi Backend Design

## Goal

Build a simple MVP backend for an anonymous one-to-one random chat app.

The backend should support:

- anonymous temporary sessions
- random partner matching
- realtime one-to-one chat
- next chat flow
- disconnect cleanup

This MVP is intentionally simple:

- no login
- no database
- no message persistence
- no advanced filters in matching logic

Gender and age range can still be collected from the UI, but matching will be random in the first version.

---

## Backend Style

Use a layer-based package structure:

```text
src/main/java/com/pesudadi
├── PesuDaDiApplication.java
├── config
├── controller
├── dto
├── model
├── service
├── repository
├── exception
└── util
```

### Folder Responsibilities

- `config`
  - WebSocket and CORS configuration
- `controller`
  - REST controllers and WebSocket message controllers
- `dto`
  - request and response payload classes
- `model`
  - core runtime domain models
- `service`
  - application business logic
- `repository`
  - in-memory storage for sessions, queue, and active chat rooms
- `exception`
  - custom exceptions and global exception handling
- `util`
  - helper utilities and shared constants if needed

---

## MVP User Flow

1. User opens the app
2. Frontend creates an anonymous session
3. User selects gender and age range
4. User clicks `Start Chat`
5. Backend either:
   - puts the user into the waiting queue, or
   - matches the user with a random waiting partner
6. Both users receive a match event through WebSocket
7. Users chat in realtime
8. User can click:
   - `Next` to leave current chat and find a new one
   - `Disconnect` to leave completely
9. If one partner disconnects, the other user receives a partner-disconnected event

---

## API Design

Keep the REST API minimal.

### REST Endpoints

#### 1. `POST /api/session`

Creates an anonymous temporary session.

Response:

```json
{
  "sessionId": "uuid"
}
```

#### 2. `POST /api/chat/start`

Used for both:

- starting the first search
- finding the next chat

Behavior:

- if the user is idle, add to queue
- if the user is already in a room, end the current room first
- if a waiting partner exists, create a room immediately
- otherwise keep the user in searching state

Request:

```json
{
  "sessionId": "uuid",
  "gender": "MALE",
  "ageRange": "EIGHTEEN_TO_TWENTY_FOUR"
}
```

Response:

```json
{
  "status": "SEARCHING"
}
```

Note:

Actual match results should be delivered through WebSocket, not only through the REST response.

#### 3. `POST /api/chat/disconnect`

Used when user clicks `Disconnect` or leaves intentionally.

Behavior:

- remove user from queue
- end active room if present
- notify partner if required
- mark session disconnected

Request:

```json
{
  "sessionId": "uuid"
}
```

Response:

```json
{
  "status": "DISCONNECTED"
}
```

---

## WebSocket Design

WebSocket handles all realtime communication.

### Client Send Destinations

- `/app/chat.send`

### Client Subscription Destinations

- `/topic/session/{sessionId}`
- `/topic/room/{roomId}`

### Session Topic Events

`/topic/session/{sessionId}` should be used for:

- searching started
- match found
- partner disconnected
- room ended
- errors

Example event:

```json
{
  "type": "MATCH_FOUND",
  "roomId": "room-123"
}
```

### Room Topic Events

`/topic/room/{roomId}` should be used for:

- chat messages between matched users

Example message event:

```json
{
  "roomId": "room-123",
  "senderSessionId": "uuid",
  "content": "Hello!",
  "timestamp": "2026-05-09T11:10:00Z"
}
```

---

## Core Models

### `UserSession`

Stores temporary user state.

Fields:

- `sessionId`
- `gender`
- `ageRange`
- `status`
- `currentRoomId`
- `connected`
- `createdAt`

### `ChatRoom`

Stores active one-to-one chat room state.

Fields:

- `roomId`
- `userOneSessionId`
- `userTwoSessionId`
- `active`
- `createdAt`

### `ChatMessage`

Represents one realtime message.

Fields:

- `roomId`
- `senderSessionId`
- `content`
- `timestamp`

### `SessionStatus`

Enum values:

- `IDLE`
- `SEARCHING`
- `MATCHED`
- `DISCONNECTED`

---

## Service Responsibilities

### `SessionService`

- create session
- fetch session
- update session state
- mark session disconnected

### `MatchmakingService`

- manage waiting queue
- pick random waiting partner
- move users from queue to active room

### `ChatService`

- create rooms
- validate room membership
- publish chat messages
- end active room

### `PresenceService`

- handle disconnect cleanup
- notify partner when a user leaves unexpectedly

---

## Repository Plan

Use in-memory repositories for MVP.

### `InMemorySessionRepository`

- store all active sessions
- implementation idea: `ConcurrentHashMap<String, UserSession>`

### `InMemoryQueueRepository`

- store waiting users
- implementation idea: synchronized list or queue

### `InMemoryChatRoomRepository`

- store active rooms
- implementation idea: `ConcurrentHashMap<String, ChatRoom>`

No database is needed for the first version.

---

## Runtime State Flow

The backend does not use persistent storage for matchmaking in the MVP.

It only keeps short-lived runtime state in memory while the server is running.

This works because the app is anonymous and matching is random.

The backend only needs to know:

- who is currently waiting
- who is currently matched
- which two users belong to a room

If the server restarts, all of this state is lost, which is acceptable for the first version.

### In-Memory State

The MVP keeps three main runtime collections:

- `sessions`
  - stores temporary user state such as `sessionId`, `status`, and `currentRoomId`
- `waiting queue`
  - stores users who clicked `Start Chat` and are waiting for a partner
- `active rooms`
  - stores current one-to-one chat pairs

This is not long-term user storage.

It is only temporary live application state.

### Example Flow

Assume three users:

- `A`
- `B`
- `C`

#### Step 1: User A starts chat

- queue is empty
- backend adds `A` to waiting queue
- `A` status becomes `SEARCHING`

State:

```text
waiting queue: [A]
active rooms: []
```

#### Step 2: User B starts chat

- backend sees `A` is waiting
- backend randomly selects `A`
- backend removes `A` from queue
- backend creates room `R1`
- `A` and `B` become `MATCHED`

State:

```text
waiting queue: []
active rooms: [R1(A, B)]
```

#### Step 3: User C starts chat

- no user is waiting now
- backend adds `C` to queue
- `C` becomes `SEARCHING`

State:

```text
waiting queue: [C]
active rooms: [R1(A, B)]
```

#### Step 4: User A clicks Next

- backend closes room `R1`
- backend notifies `B` that partner disconnected
- backend immediately tries to rematch `A`
- backend sees `C` in queue
- backend creates room `R2`
- `A` and `C` become `MATCHED`

State:

```text
waiting queue: []
active rooms: [R2(A, C)]
```

#### Step 5: User C disconnects

- backend removes `C` from any active room or queue
- backend notifies `A`
- `A` returns to `IDLE`

State:

```text
waiting queue: []
active rooms: []
```

### Why This Is Enough For MVP

This approach is enough because the app currently does not need:

- account persistence
- chat history
- room recovery after restart
- multi-instance shared state

The backend only needs live state for active users.

### Limitation

If the server restarts or sleeps:

- waiting users are lost
- active rooms are lost
- users must reconnect and start again

This is acceptable for the anonymous MVP, especially on a low-cost single-instance deployment.

---

## Matching Strategy

Matching is random for MVP.

Rules:

- ignore advanced compatibility logic for now
- when a user starts search, check the waiting queue
- if queue is empty, store user as searching
- if queue has users, randomly pick one waiting partner
- remove both users from waiting flow
- create chat room
- notify both users

Later we can upgrade this to:

- gender-based matching
- age-range compatibility
- country preference
- anti-repeat matching

---

## Expected Java Classes

### `config`

- `WebSocketConfig`
- `CorsConfig`

### `controller`

- `HealthController`
- `SessionController`
- `ChatController`
- `ChatWebSocketController`

### `dto`

- `CreateSessionResponse`
- `StartChatRequest`
- `StartChatResponse`
- `DisconnectRequest`
- `SessionEventResponse`
- `ChatMessageRequest`
- `ChatMessageResponse`
- `ErrorResponse`

### `model`

- `UserSession`
- `ChatRoom`
- `ChatMessage`
- `SessionStatus`
- `Gender`
- `AgeRange`

### `service`

- `SessionService`
- `MatchmakingService`
- `ChatService`
- `PresenceService`

### `repository`

- `InMemorySessionRepository`
- `InMemoryQueueRepository`
- `InMemoryChatRoomRepository`

---

## Implementation Order

1. Create base package structure
2. Add `HealthController`
3. Add session creation flow
4. Add WebSocket configuration
5. Add in-memory queue and random matchmaking
6. Add chat room creation and realtime message flow
7. Add next chat behavior using the same start endpoint
8. Add disconnect handling and partner notification
9. Add basic tests for session, matchmaking, and disconnect flow

---

## Non-Goals For MVP

Do not build these yet:

- authentication
- database persistence
- saved chat history
- media sharing
- typing indicators
- online user counters
- moderation tools
- Redis or distributed scaling

These can be added after the simple MVP works reliably.
