# Mobile Phase 9 - Push Notifications

> **Status:** PENDING
> **Depends On:** Backend Phase 9 (must be completed first)

---

## Objective

Integrate Firebase Cloud Messaging (FCM) for background push notifications and wire the existing notifications screen to the real API.

---

## Pre-Existing Work

- `NotificationsScreen.kt` built with mock data
- `MockNotifications.kt` provides data structure reference

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Add Firebase SDK dependency | `implementation 'com.google.firebase:firebase-messaging'` |
| 2 | Create `NotificationApiService.kt` | Retrofit interface for notification endpoints |
| 3 | Create `NotificationRepository.kt` | Repository layer |
| 4 | Create `NotificationViewModel.kt` | ViewModel for notifications |
| 5 | Wire `NotificationsScreen` to real API | Replace mock data |
| 6 | Implement `UniLostFirebaseService` | Extend `FirebaseMessagingService` for background push |
| 7 | Store FCM device token | Send to backend on login via `POST /api/users/fcm-token` |
| 8 | Implement notification channels | Android notification categories (claims, messages, handovers) |
| 9 | Implement notification tap -> navigation | Deep link to relevant screen on tap |

---

## Technical Details

```kotlin
class UniLostFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Send token to backend: POST /api/users/fcm-token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Display Android notification using NotificationCompat
    }
}
```

### Notification Channels
| Channel | Description |
|---------|-------------|
| `claims` | Claim updates (new, accepted, rejected) |
| `messages` | New chat messages |
| `handovers` | Handover status changes |
| `general` | Admin notifications, system alerts |

---

## Acceptance Criteria

- [ ] App receives push notifications when in background
- [ ] Notifications screen shows real data
- [ ] Tapping a notification navigates to the relevant screen
- [ ] FCM token is registered with backend on login
- [ ] Notification channels are properly configured
