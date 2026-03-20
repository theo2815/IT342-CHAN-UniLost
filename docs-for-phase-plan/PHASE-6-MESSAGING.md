# Phase 6 - User-to-User Messaging (Backend + Website)

> **Status:** PENDING
> **Priority:** SHOULD HAVE
> **Depends On:** Phase 4 (Claim & Verification)

---

## Objective

Implement secure in-app messaging between finders and potential owners, triggered when a claim is initiated. Messages are privacy-first -- real names and emails are masked by usernames until users choose to share them.

---

## Pre-Existing Work

- **Backend:** `ChatEntity.java`, `MessageEntity.java`, `ChatRepository.java`, `MessageRepository.java` already exist (no controller/service yet)
- **Website:** `Messages/Messages.jsx` built with mock data

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `ChatService.java` | Business logic for chat room management |
| 2 | Create `MessageService.java` | Business logic for sending/receiving messages |
| 3 | Create `ChatController.java` | REST endpoints for chat operations |
| 4 | Create `MessageController.java` | REST + WebSocket endpoints for messages |
| 5 | Auto-create chat room on claim initiation | When a claim is created, open a chat between finder and claimant |
| 6 | WebSocket integration | Real-time message delivery for active sessions |
| 7 | Message read receipts | Track whether messages have been read |
| 8 | Privacy controls | Mask real identity until explicitly shared |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/chats` | Get all chat rooms for the authenticated user |
| `GET` | `/api/chats/{id}` | Get a specific chat room with metadata |
| `GET` | `/api/chats/{chatId}/messages` | Get messages in a chat (paginated) |
| `POST` | `/api/chats/{chatId}/messages` | Send a message in a chat |
| `PUT` | `/api/chats/{chatId}/read` | Mark all messages in a chat as read |
| `WebSocket` | `/ws/chat/{chatId}` | Real-time message stream |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire `Messages` page to real API | Replace mock data with chat list |
| 2 | Build real-time chat UI | WebSocket connection for live messages |
| 3 | Implement message input + send | POST new messages |
| 4 | Implement read receipts UI | Show read/unread indicators |
| 5 | Create `chatService.js` | API service for chat endpoints |
| 6 | Add unread message badge to navigation | Show count of unread messages |

---

## Technical Details

### Chat Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `itemId` | String | Reference to the item being discussed |
| `claimId` | String | Reference to the associated claim |
| `participants` | List<String> | User IDs of finder and claimant |
| `lastMessage` | String | Preview of the most recent message |
| `lastMessageAt` | LocalDateTime | Timestamp of last message |
| `createdAt` | LocalDateTime | Chat creation timestamp |

### Message Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `chatId` | String | Reference to parent chat |
| `senderId` | String | User who sent the message |
| `content` | String | Message text |
| `isRead` | Boolean | Whether recipient has read it |
| `createdAt` | LocalDateTime | Message timestamp |

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

### Privacy Model
- Chat displays usernames only (not full names or emails)
- Users can voluntarily share contact info in messages
- Admin cannot read private messages (moderation is post-flagged only)

### WebSocket Configuration
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // STOMP over WebSocket at /ws
    // Message broker: /topic (broadcasts), /queue (user-specific)
    // App destination prefix: /app
}
```

---

## Acceptance Criteria

- [ ] Chat room is auto-created when a claim is submitted
- [ ] Users can view their list of active chats
- [ ] Users can send and receive messages in real-time
- [ ] Messages show read/unread status
- [ ] User identity is masked (username only) in chat
- [ ] Unread message count displays in navigation
