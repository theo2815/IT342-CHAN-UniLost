# Phase 9 - Notifications & Alerts (Backend + Website)

> **Status:** PENDING
> **Priority:** COULD HAVE
> **Depends On:** Phase 4 (Claims), Phase 6 (Messaging), Phase 7 (Handover)

---

## Objective

Implement real-time in-app notifications and alerts via WebSockets to keep users informed about claim updates, new messages, handover status changes, and relevant item matches.

---

## Pre-Existing Work

- **Backend:** `NotificationEntity.java` and `NotificationRepository.java` already exist (entity + repo only, no controller/service)
- **Backend:** WebSocket infrastructure already exists from Phase 6 — `WebSocketConfig.java` (STOMP at `/ws`, SockJS fallback), `WebSocketAuthInterceptor.java` (JWT auth on CONNECT), `SimpMessagingTemplate` already used in `ChatService` for real-time chat messages
- **Backend:** `spring-boot-starter-websocket` already in `pom.xml`
- **Website:** `Notifications/Notifications.jsx` + `Notifications.css` exist with mock data
- **Website:** `NotificationDropdown.jsx` + `NotificationDropdown.css` exist with mock data
- **Website:** `Header.jsx` already has unread message badge with visibility-aware polling (Phase 6) — notification badge can follow same pattern
- **Website:** `@stomp/stompjs` and `sockjs-client` already installed (used by Messages page)

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `NotificationService.java` | Business logic for creating and managing notifications |
| 2 | Create `NotificationController.java` | REST endpoints for notification operations |
| 3 | WebSocket real-time delivery | Reuse existing STOMP infrastructure from Phase 6 (`/ws` endpoint) |
| 4 | Notification triggers | Hook into claim, message, handover, and admin events |
| 5 | Notification preferences | Allow users to configure which notifications they receive |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/notifications` | Get all notifications for the authenticated user (paginated) |
| `GET` | `/api/notifications/unread/count` | Get count of unread notifications |
| `PUT` | `/api/notifications/{id}/read` | Mark a single notification as read |
| `PUT` | `/api/notifications/read-all` | Mark all notifications as read |
| `DELETE` | `/api/notifications/{id}` | Delete a notification |
| `PUT` | `/api/notifications/preferences` | Update notification preferences |
| `WebSocket` | `/topic/notifications/{userId}` or `/user/queue/notifications` | Real-time notification stream (reuse existing `/ws` STOMP endpoint) |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire `Notifications` page to real API | Replace mock data |
| 2 | Wire `NotificationDropdown` to real API | Live unread count + recent items |
| 3 | Reuse STOMP WebSocket connection | Same pattern as Messages page — JWT in connectHeaders, subscribe to notification topic |
| 4 | Implement notification click -> navigation | Route to relevant page on click |
| 5 | Add browser notification support | `Notification API` for tab-unfocused alerts |
| 6 | Implement notification preferences in Settings | Toggle notification types |
| 7 | Create `notificationService.js` | API service for notification endpoints |
| 8 | Add notification unread badge in Header | Follow same visibility-aware polling pattern as unread message badge |

---

## Technical Details

### Notification Entity Fields (Current Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | String | Recipient user ID |
| `type` | String | `MESSAGE`, `CLAIM_UPDATE`, `HANDOVER_SUCCESS` |
| `content` | String | Notification body text |
| `linkId` | String | ID of related entity for navigation |
| `isRead` | boolean | Whether user has seen it |
| `createdAt` | LocalDateTime | Timestamp |

**Note:** Current entity uses `type` as String (not enum) with values `MESSAGE`, `CLAIM_UPDATE`, `HANDOVER_SUCCESS`. Fields differ from original plan — uses `content` + `linkId` instead of `title` + `message` + `referenceId` + `referenceType`. The type list may need to be expanded during implementation.

### Notification Repository (Current Implementation)
| Method | Description |
|--------|-------------|
| `findByUserIdOrderByCreatedAtDesc(String userId)` | Get all notifications for a user |
| `findByUserIdAndIsReadFalse(String userId)` | Get unread notifications |
| `countByUserIdAndIsReadFalse(String userId)` | Count unread notifications |

**TODOs in repo:** batch mark-as-read, auto-cleanup for old notifications

### Planned Notification Types

| Type | Trigger | Content Example |
|------|---------|-----------------|
| `CLAIM_UPDATE` | Claim accepted/rejected | "Your claim on 'Blue Wallet' was accepted!" |
| `MESSAGE` | New chat message received | "New message from John" |
| `HANDOVER_SUCCESS` | Handover completed | "Handover complete! +10 Karma" |
| *(Expand as needed)* | New claim on your item, item flagged, account suspended, etc. | — |

### WebSocket Architecture (Reuse Phase 6 Infrastructure)

```
Phase 6 already provides:
  - STOMP over WebSocket at /ws with SockJS fallback
  - WebSocketAuthInterceptor validates JWT on CONNECT
  - SimpMessagingTemplate for broadcasting

For notifications, add:
  - Subscribe to: /user/queue/notifications (user-specific)
  - Backend publishes via:
    messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
  - Frontend reuses same STOMP client pattern from Messages page
```

---

## Acceptance Criteria

- [ ] Notifications are created for all key events (claims, messages, handovers)
- [ ] Users see real-time notifications without page refresh (via existing WebSocket)
- [ ] Unread notification count displays in the header/nav
- [ ] Clicking a notification navigates to the relevant page
- [ ] Users can mark notifications as read (individual and bulk)
- [ ] Users can configure notification preferences
