# PodCraft — Design Overview

This document describes the data models, HTTP APIs, and WebSocket (STOMP) messaging used in PodCraft for chat rooms, WebRTC signaling, and recorded video calls.

**Paste tip:** Copy from the raw editor view (not from inside a preview code block). This file uses headings and bullets only—no tables—so it pastes cleanly into Google Docs, Confluence, and similar tools.

---

## 1. Architecture (high level)

- Backend: Spring Boot, Spring MVC, Spring Security (form login), Spring WebSocket with STOMP over SockJS.
- Persistence: JPA entities backed by MySQL (UserRepository, RoomRepository, VideoCallRepository).
- Storage: Recorded call uploads via CloudStorageService (e.g. GCP), returning a public URL.
- Realtime: Clients connect to the /ws SockJS endpoint; STOMP uses application prefix /app, broker prefix /queue, and user-specific routing under /user.

---

## 2. Domain models

### 2.1 JPA entities

**User** — database table: users

- Fields: id, username, password, roles (e.g. USER).

**Room** — database table: rooms

- Fields: id, roomName (unique), roomCode, host, createdAt, isActive, recordingStatus, isMicOnByDefault, isCameraOnByDefault.

**VideoCall** — database table: videoCall

- Fields: id, callId (short UUID fragment), hostId, guestId (string usernames), startTime, duration in seconds, hostUrl, guestUrl (recording URLs).

### 2.2 DTOs and message payloads (non-JPA)

**RegisterDTO** — Registration form: username, password, confirmPassword. Validation: non-empty username; password minimum length 6.

**RoomDTO** — Currently holds host only (minimal DTO).

**VideoCallDTO** — Used when persisting a call after upload: roomCode, targetUser, startTime, endTime, duration, hostUrl, guestUrl.

**JoinRoomMessage** — WebSocket body for joining a room: roomCode.

**SignalMessage** — WebRTC signaling envelope: type, fromUser, toUser, data (string; often a JSON string for SDP or ICE). Practical types: call, offer, answer, candidate, end.

---

## 3. HTTP APIs and page routes

**Security:** Paths under /chat-lobby/ require authentication; most other HTTP matchers are permitted (see SecurityConfig). WebSocket handlers use the authenticated principal where applicable.

### 3.1 Pages (server-rendered)

- GET / — JspController — Landing page (index template).
- GET /chat-lobby — JspController — Lobby UI.
- GET /register — UserController — Registration form.
- POST /register — UserController — Creates user (noop password prefix in dev), validates DTO.
- GET /dashboard — DashBoardController — Dashboard with the signed-in user’s VideoCall list.
- GET /chat-lobby/room/{roomCode} — WebSocketEventListener — Video call page (videoChat template). Enforces at most two distinct users in the in-memory room map before join; model attributes: roomCode, username, host.

### 3.2 REST-style JSON and body endpoints

- GET /current-user — Returns plain text: username of the current Principal.
- POST /chat-lobby/create — JSON body shaped like Room (roomName, optional mic/camera defaults). Host is the current user; creates room via RoomService.
- GET /chat-lobby/rooms — Returns a JSON list of Room objects.
- DELETE /chat-lobby/delete/{roomCode} — Deletes the room by code (host-only enforcement is in the lobby UI, not the API).
- POST /save — JSON body: VideoCallDTO. Builds VideoCall, resolves host from the room, sets guest using targetUser vs current user, saves via VideoCallService.
- POST /uploadVideo — multipart form field file; response body is the uploaded file’s public URL (text).

### 3.3 Controller caveat

- DashBoardController also declares GET /user/{userId} and GET /{uuid} to return call data, but the class is a @Controller without class-level @ResponseBody. Those methods may not behave as JSON APIs unless you add @ResponseBody or move them to a @RestController—verify if a non-browser client depends on them.

---

## 4. WebSockets (STOMP)

### 4.1 Configuration (WebSocketConfig class)

- STOMP endpoint: /ws with SockJS; allowed origin patterns: *.
- Broker prefix: /queue (simple in-memory broker).
- Application destination prefix: /app (clients send to destinations starting with /app).
- User destination prefix: /user (used with convertAndSendToUser).

**Note:** The file lives under src/main/java/com/pawan/podcraft/websockets/ but the Java package is com.pawan.riverside.websockets. Confirm Spring component scanning if WebSockets fail to register.

### 4.2 Client to server (@MessageMapping)

From the browser, destinations are prefixed with /app (example: send to /app/joinRoom).

- /app/joinRoom — WebSocketEventListener.joinRoom — Registers the Principal’s username in roomUsers for the given roomCode, updates userRoomMap, broadcasts online users.
- /app/requestOnlineUsers/{roomCode} — WebSocketEventListener.handleRequestOnlineUsers — Calls broadcastOnlineUsers for that room.
- /app/signal/{roomCode} — VideoCallController.handleSignal — Relays WebRTC signaling to the target user’s queue (see signal routing below).
- /app/{roomCode}/guestUrl — sendGuestMessage — Broadcasts guest recording URL.
- /app/{roomCode}/hostUrl — sendHostMessage — Broadcasts host recording URL.
- /app/ping — pong — Echo; used for latency measurement.
- /app/{roomCode}/endTime — sendEndMessage — Broadcasts end time string.
- /app/{roomCode}/startTime — sendStartMessage — Broadcasts start time string.
- /app/{roomCode}/flag — sendFlagMessage — Broadcasts flag 0 or 1 for coordinating dual upload and save.

**Signal routing (handleSignal)**

- Input: SignalMessage with toUser, type, and data (except for type call).
- If type is call, the payload delivered to the recipient is the caller’s username (Principal name), not message.data.
- Otherwise the payload is message.data.
- Server sends with: convertAndSendToUser(toUser, "/queue/{roomCode}/{type}", payload).
- Client subscribes (see call.js): /user/queue/{roomCode}/{type} for call, offer, answer, candidate, end.

### 4.3 Server to client (typical subscriptions in call.js)

- /user/queue/{roomCode}/call — Incoming ring; body is caller username.
- /user/queue/{roomCode}/offer — SDP offer (JSON body).
- /user/queue/{roomCode}/answer — SDP answer (JSON body).
- /user/queue/{roomCode}/candidate — ICE candidate (JSON body).
- /user/queue/{roomCode}/end — Remote hangup.
- /queue/pong — Ping or pong for latency.
- /queue/onlineUsers/{roomCode} — JSON array of usernames in the room.
- /queue/{roomCode}/guestUrl — Guest recording URL.
- /queue/{roomCode}/hostUrl — Host recording URL.
- /queue/{roomCode}/flag — Coordination flag for saving call metadata.
- /queue/{roomCode}/startTime — Call start time string.
- /queue/{roomCode}/endTime — Call end time string.

### 4.4 Session lifecycle

- On SessionDisconnectEvent, WebSocketEventListener removes the user from roomUsers and userRoomMap and rebroadcasts online users (with a null guard on roomCode when broadcasting).

---

## 5. End-to-end flows (reference)

1. Lobby: REST creates and lists rooms; user opens /chat-lobby/room/{roomCode}.
2. Room presence: On the video page, client sends STOMP joinRoom and subscribes to onlineUsers.
3. WebRTC: call, then offer, answer, candidate, end via /app/signal/{roomCode} and per-user queues; media is peer-to-peer, not through the server.
4. Recording: Each client POSTs to /uploadVideo, then publishes URLs via guestUrl and hostUrl over STOMP; when both URLs and flag conditions match, client POSTs /save with VideoCallDTO.

---

## 6. Key frontend files

- static/js/call.js — STOMP connection, signaling, RTCPeerConnection, recording upload coordination.
- static/js/chat-lobby.js — Room create/list/delete via REST; navigation into the video room.
- templates/videoChat.html — Video call UI; server injects username, host, roomCode.

---

Document generated from the PodCraft repository (controllers, entities, and client scripts).
