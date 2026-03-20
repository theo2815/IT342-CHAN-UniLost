# Mobile Phase 6 - Messaging

> **Status:** PENDING
> **Depends On:** Backend Phase 6 (must be completed first)

---

## Objective

Build chat screens for mobile and integrate with the backend messaging system using REST + WebSocket for real-time communication.

---

## Pre-Existing Work

- No dedicated messaging screen exists yet on mobile
- Backend will provide REST + WebSocket endpoints (built in Backend Phase 6)

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Create `MessagesScreen.kt` | Chat list screen showing all conversations |
| 2 | Create `ChatScreen.kt` | Individual chat conversation screen |
| 3 | Create `ChatApiService.kt` | Retrofit interface for chat endpoints |
| 4 | Create `ChatRepository.kt` | Repository layer |
| 5 | Create `ChatViewModel.kt` | ViewModel for chat screens |
| 6 | Implement WebSocket client | Real-time messages via OkHttp WebSocket or STOMP |
| 7 | Implement read receipts | Show read/unread indicators |
| 8 | Add to navigation graph | New screens in `NavGraph.kt` |
| 9 | Add unread badge to bottom nav | Show unread message count |

---

## Acceptance Criteria

- [ ] Users can view their chat list on mobile
- [ ] Users can send and receive messages in real-time
- [ ] Messages show read/unread status
- [ ] Chat screens are accessible from navigation
