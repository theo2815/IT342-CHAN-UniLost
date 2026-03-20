# Phase 6 - User-to-User Messaging (Backend + Website)

> **Status:** COMPLETED
> **Priority:** SHOULD HAVE
> **Depends On:** Phase 4 (Claim & Verification)

---

## Objective

Implement secure in-app messaging between finders and potential owners, triggered when a claim is initiated. Messages are delivered in real-time via WebSocket with authenticated STOMP connections.

---

## Pre-Existing Work

- **Backend:** `ChatEntity.java`, `MessageEntity.java`, `ChatRepository.java`, `MessageRepository.java` already existed (no controller/service)
- **Website:** `Messages/Messages.jsx` built with mock data

---

## Backend (Spring Boot) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | `ChatService.java` | DONE | Chat CRUD, message send/receive, read receipts, unread count aggregation |
| 2 | `ChatController.java` | DONE | REST endpoints for all chat operations |
| 3 | `WebSocketConfig.java` | DONE | STOMP over WebSocket at `/ws` with SockJS fallback |
| 4 | `WebSocketAuthInterceptor.java` | DONE | JWT authentication on STOMP CONNECT, blocks unauthorized SUBSCRIBE/SEND |
| 5 | Auto-create chat on claim | DONE | `ClaimService.submitClaim()` calls `ChatService.createChatForClaim()` |
| 6 | Real-time broadcast | DONE | `SimpMessagingTemplate.convertAndSend()` on message creation |
| 7 | Read receipts | DONE | `PUT /api/chats/{id}/read` marks all unread messages as read |
| 8 | Message length validation | DONE | Max 2000 characters per message |
| 9 | Unread count aggregation | DONE | Single MongoDB aggregation query (no N+1) |
| 10 | MongoDB indexes | DONE | 6 indexes on chats/messages collections |
| 11 | `spring-boot-starter-websocket` | DONE | Added to `pom.xml` |

### API Endpoints (Implemented)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/chats` | Auth | Get all chat rooms for the authenticated user (ordered by last message) |
| `GET` | `/api/chats/{chatId}` | Auth | Get a specific chat room with metadata |
| `GET` | `/api/chats/{chatId}/messages` | Auth | Get messages in a chat (paginated, newest first) |
| `POST` | `/api/chats/{chatId}/messages` | Auth | Send a message (max 2000 chars) |
| `PUT` | `/api/chats/{chatId}/read` | Auth | Mark all messages in a chat as read |
| `GET` | `/api/chats/unread-count` | Auth | Get total unread message count across all chats |
| `WebSocket` | `/ws` (STOMP) | JWT on CONNECT | Subscribe to `/topic/chat/{chatId}` for real-time messages |

## Website (React + Vite) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Wire `Messages` page | DONE | Full rewrite from mock to real API |
| 2 | Real-time WebSocket chat | DONE | `@stomp/stompjs` + `sockjs-client`, JWT in connect headers |
| 3 | Message input + send | DONE | Optimistic send with rollback on failure |
| 4 | Read receipts UI | DONE | Check/double-check icons for sent/read |
| 5 | `chatService.js` created | DONE | All chat API endpoints |
| 6 | Unread message badge | DONE | Header badge with 30s poll (pauses on hidden tab) |
| 7 | Chat list sidebar | DONE | Filterable (All/Unread), item title tags, relative timestamps |
| 8 | Quick reply buttons | DONE | "I'm here", "Running late", "Meet at Security" |
| 9 | Error handling | DONE | Error state for chat list, WebSocket error handlers |
| 10 | Mobile responsive | DONE | Sidebar toggle, back button on mobile |

---

## Technical Details

### Chat Entity Fields (Actual Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `itemId` | String | Reference to the item being discussed |
| `claimId` | String | Reference to the associated claim |
| `finderId` | String | User who found/reported the item |
| `ownerId` | String | User who is claiming ownership |
| `lastMessagePreview` | String | Preview of the most recent message (max 100 chars) |
| `lastMessageAt` | LocalDateTime | Timestamp of last message |
| `createdAt` | LocalDateTime | Chat creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

### Message Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `chatId` | String | Reference to parent chat |
| `senderId` | String | User who sent the message |
| `content` | String | Message text (max 2000 chars) |
| `isRead` | boolean | Whether recipient has read it |
| `createdAt` | LocalDateTime | Message timestamp |

### ChatDTO Fields
Includes resolved: `itemTitle`, `itemImageUrl`, `finderName`, `ownerName`, `otherParticipantId`, `otherParticipantName`, `unreadCount`

### MongoDB Indexes
| Index | Collection | Fields | Notes |
|-------|-----------|--------|-------|
| `idx_item_finder_owner` | chats | `(itemId, finderId, ownerId)` | **Unique** — prevents duplicate chats |
| `idx_finder_lastmsg` | chats | `(finderId, lastMessageAt desc)` | Chat list query |
| `idx_owner_lastmsg` | chats | `(ownerId, lastMessageAt desc)` | Chat list query |
| `idx_claimId` | chats | `(claimId)` | Claim-to-chat lookup |
| `idx_chat_created` | messages | `(chatId, createdAt desc)` | Paginated message retrieval |
| `idx_chat_read_sender` | messages | `(chatId, isRead, senderId)` | Unread count queries |

### WebSocket Architecture
```
Frontend:
  const client = new Client({
    webSocketFactory: () => new SockJS("/ws"),
    connectHeaders: { Authorization: "Bearer <token>" },
  });
  client.subscribe("/topic/chat/{chatId}", callback);

Backend:
  WebSocketAuthInterceptor validates JWT on STOMP CONNECT
  Blocks SUBSCRIBE/SEND if user is not authenticated
  ChatService broadcasts via SimpMessagingTemplate.convertAndSend()
```

### Chat Lifecycle
```
1. User submits a claim on a Found item (Phase 4)
2. System auto-creates a Chat room linking:
   - The Finder (item poster)
   - The Claimant (person claiming)
   - The Item and Claim IDs
3. Both users see the chat in their Messages list
4. Users communicate to arrange verification or meetup
5. Chat persists even after claim is resolved (for reference)
```

### Security
- STOMP CONNECT requires valid JWT token in `Authorization` header
- SUBSCRIBE and SEND blocked for unauthenticated connections
- Chat participation verified on every REST endpoint (403 if not finder/owner)
- Message content limited to 2000 characters
- Debounced `markRead` on frontend prevents server flooding

---

## Acceptance Criteria

- [x] Chat room is auto-created when a claim is submitted
- [x] Users can view their list of active chats
- [x] Users can send and receive messages in real-time
- [x] Messages show read/unread status (check/double-check icons)
- [x] Unread message count displays in navigation header
- [x] WebSocket connections are authenticated via JWT
- [x] Chat participation is enforced on all endpoints
- [x] Messages have max 2000 character limit
- [x] MongoDB indexes prevent performance degradation at scale

---

## Post-Implementation Audit Summary

A comprehensive audit was performed after Phase 6 implementation.

**3 CRITICAL, 5 HIGH, 7 MEDIUM issues found and fixed:**

| Severity | Count | Key Fixes |
|----------|-------|-----------|
| CRITICAL | 3 | WebSocket auth interceptor, debounced markRead, message length validation |
| HIGH | 5 | Stale async guard, optimistic rollback, N+1 unread aggregation, MongoDB indexes, unique chat dedup |
| MEDIUM | 7 | Malformed token handling, smart auto-scroll, visibility-aware polling, error states, ClaimDTO chatId |
