# Phase 9 - Notifications & Alerts (Backend + Website)

> **Status:** PENDING
> **Priority:** COULD HAVE
> **Depends On:** Phase 4 (Claims), Phase 6 (Messaging), Phase 7 (Handover)

---

## Objective

Implement real-time in-app notifications and alerts via WebSockets to keep users informed about claim updates, new messages, handover status changes, and relevant item matches.

---

## Pre-Existing Work

- **Backend:** `NotificationEntity.java` and `NotificationRepository.java` already exist (no controller/service yet)
- **Website:** `Notifications/Notifications.jsx` and `NotificationDropdown.jsx` built with mock data
- **Mock Data:** `mockData/notifications.js` provides data structure reference

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `NotificationService.java` | Business logic for creating and managing notifications |
| 2 | Create `NotificationController.java` | REST endpoints for notification operations |
| 3 | WebSocket real-time delivery | Push notifications to active sessions instantly |
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
| `WebSocket` | `/ws/notifications` | Real-time notification stream |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire `Notifications` page to real API | Replace mock data |
| 2 | Wire `NotificationDropdown` to real API | Live unread count + recent items |
| 3 | Implement WebSocket connection | Real-time notification updates |
| 4 | Implement notification click -> navigation | Route to relevant page on click |
| 5 | Add browser notification support | `Notification API` for tab-unfocused alerts |
| 6 | Implement notification preferences in Settings | Toggle notification types |
| 7 | Create `notificationService.js` | API service for notification endpoints |

---

## Technical Details

### Notification Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | String | Recipient user ID |
| `type` | Enum | See notification types below |
| `title` | String | Notification headline |
| `message` | String | Notification body text |
| `referenceId` | String | ID of related item/claim/chat |
| `referenceType` | Enum | `ITEM`, `CLAIM`, `CHAT`, `HANDOVER` |
| `isRead` | Boolean | Whether user has seen it |
| `createdAt` | LocalDateTime | Timestamp |

### Notification Types

| Type | Trigger | Message Example |
|------|---------|-----------------|
| `NEW_CLAIM` | Someone claims your found item | "John submitted a claim on your item 'Blue Wallet'" |
| `CLAIM_ACCEPTED` | Your claim was accepted | "Your claim on 'Blue Wallet' was accepted!" |
| `CLAIM_REJECTED` | Your claim was rejected | "Your claim on 'Blue Wallet' was not accepted" |
| `NEW_MESSAGE` | New chat message received | "New message from Finder_123" |
| `HANDOVER_INITIATED` | Handover process started | "Handover started for 'Blue Wallet' - arrange a meeting" |
| `HANDOVER_CONFIRMED` | Other party confirmed handover | "The finder confirmed the handover for 'Blue Wallet'" |
| `HANDOVER_COMPLETED` | Both parties confirmed | "Handover complete! +10 Karma Points" |
| `ITEM_FLAGGED` | Your item was flagged (admin) | "An item in your campus was flagged for review" |
| `ITEM_TURNED_OVER` | Item turned over to office | "A found item matching your description was turned in" |
| `ACCOUNT_SUSPENDED` | Admin suspended account | "Your account has been suspended. Contact admin." |

### WebSocket Architecture

```
Client connects to: ws://localhost:8080/ws/notifications
Subscribe to: /user/queue/notifications

Backend publishes via SimpMessagingTemplate:
  messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
```

---

## Acceptance Criteria

- [ ] Notifications are created for all key events (claims, messages, handovers)
- [ ] Users see real-time notifications without page refresh (WebSocket)
- [ ] Unread notification count displays in the header/nav
- [ ] Clicking a notification navigates to the relevant page
- [ ] Users can mark notifications as read (individual and bulk)
- [ ] Users can configure notification preferences
