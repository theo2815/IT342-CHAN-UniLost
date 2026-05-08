# UniLost — Manual Regression Checklist

**Branch under test:** `refactor/vertical-slice-architecture`
**Surfaces in scope:** Backend (Spring Boot), Website (React + Vite)
**Surface out of scope:** Mobile app (VSA refactor not started — separate engagement)

**Tester:** Theo Cedric Chan (solo)
**Date:** _____________
**Backend up at:** `http://localhost:8080`
**Website up at:** `http://localhost:5173`

> Walk this checklist after `mvnw spring-boot:run` and `npm run dev` are both up.
> Tick each row, paste a screenshot link in the Evidence column, and note any
> deviation. Anything that fails goes into the "Issues Found" section of the
> regression report.

---

## 1. Authentication (Phase 1)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| A1 | Register a new student | Open `/register`, enter university email + password, submit | Account created, redirected to login | ☐ | |
| A2 | Reject unknown email domain | Try registering with a non-whitelisted domain | "Email domain ... is not recognized" toast | ☐ | |
| A3 | Login with valid credentials | `/login` → enter email + pw | Redirected to `/dashboard`, JWT in localStorage | ☐ | |
| A4 | Login with bad password | `/login` → enter wrong pw | 401, "Invalid email or password" toast | ☐ | |
| A5 | Logout clears session | Header → Logout | localStorage `token` and `user` cleared, redirect to landing | ☐ | |
| A6 | Forgot password flow | `/forgot-password` → enter email → check Gmail inbox for OTP | OTP email received | ☐ | |
| A7 | Verify OTP + reset password | Paste OTP → set new password → login with new password | Login succeeds | ☐ | |
| A8 | JWT expiry handling | Edit localStorage `token` to one with past `exp` → reload | Auto-logout, redirect to landing | ☐ | |

## 2. Items (Phase 3)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| I1 | Browse item feed | Open `/items` | List of ACTIVE items with images | ☐ | |
| I2 | Search by keyword | Type "wallet" in search bar | Filtered to matching titles/descriptions | ☐ | |
| I3 | Filter by category | Pick category chip | Feed reduces to that category | ☐ | |
| I4 | Filter by campus | Pick campus chip | Feed reduces to that campus | ☐ | |
| I5 | View item detail | Click any item card | Detail page renders with title, description, status | ☐ | |
| I6 | Secret question hidden | View someone else's FOUND item detail | `secretDetailQuestion` not visible to non-owner | ☐ | |
| I7 | Post a new item | `/post-item` → fill form → upload image → submit | Item appears in feed with status ACTIVE | ☐ | |
| I8 | Edit own item | Detail of own ACTIVE item → Edit | Changes persist | ☐ | |
| I9 | Cannot edit non-ACTIVE item | Try to edit a CLAIMED item | Error: "Cannot edit an item that is claimed" | ☐ | |
| I10 | Soft-delete own item | Detail of own ACTIVE item → Delete | Item disappears from feed | ☐ | |
| I11 | Map view | `/map` | Pins for items with coordinates; campus default for those without | ☐ | |

## 3. Claims & Handover (Phase 4 + 7)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| C1 | Submit claim on FOUND item | As another student, click "Claim" → answer secret → submit | Claim PENDING; chat thread created | ☐ | |
| C2 | Self-claim blocked | Try to claim own item | "You cannot claim your own item" error | ☐ | |
| C3 | Duplicate claim blocked | Submit a second claim on the same FOUND item from same user | "You already have a pending claim" error | ☐ | |
| C4 | LOST item auto-accept | Submit a claim on a LOST item | Claim ACCEPTED immediately; item → CLAIMED | ☐ | |
| C5 | Accept claim (FOUND) | As poster of FOUND item, accept claim | Claim ACCEPTED; other PENDING claims auto-rejected; item → CLAIMED | ☐ | |
| C6 | Reject claim (FOUND) | As poster, reject a PENDING claim | Claim REJECTED; claimant notified | ☐ | |
| C7 | Cancel own pending claim | As claimant, cancel a PENDING claim | Claim CANCELLED | ☐ | |
| C8 | Mark item returned (handover step 1) | As physical holder, click "Mark as returned" in chat | Item → PENDING_OWNER_CONFIRMATION | ☐ | |
| C9 | Confirm receipt (handover step 2) | As owner, click "Confirm received" | Item → RETURNED, claim → COMPLETED, karma awarded (+10/+5) | ☐ | |
| C10 | Dispute handover | As owner, click "Dispute" instead | Item reverts to CLAIMED, mark cleared | ☐ | |
| C11 | Karma visible on leaderboard | After C9, open `/leaderboard` | Both users' karma updated | ☐ | |

## 4. Messaging (Phase 6)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| M1 | Chat list | `/messages` | List of chats sorted by last message | ☐ | |
| M2 | Send a message | Open a chat → type → send | Message appears immediately for both peers (real-time STOMP) | ☐ | |
| M3 | System messages on claim accept | Trigger C5 | "Claim accepted!" structured message in chat | ☐ | |
| M4 | System messages on handover | Trigger C8 / C9 | HANDOVER_REQUEST / HANDOVER_CONFIRMED system messages | ☐ | |
| M5 | Unread badge | Receive a message in another chat | Sidebar shows unread count | ☐ | |

## 5. Notifications (Phase 9)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| N1 | Notification on claim received | Trigger C1 → check poster's bell | New unread notification | ☐ | |
| N2 | Notification on claim accepted | Trigger C5 → check claimant's bell | New unread notification | ☐ | |
| N3 | Mark all as read | Bell dropdown → "Mark all read" | Unread count → 0 | ☐ | |
| N4 | Real-time push | Have two browsers open, trigger N1 | Bell updates without page refresh | ☐ | |

## 6. Admin Dashboard (Phase 5)

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| AD1 | Admin can sign in | Login as admin (seeded) | Sees admin nav links | ☐ | |
| AD2 | Non-admin blocked from admin pages | As STUDENT, hit `/admin/items` | 403 / redirect | ☐ | |
| AD3 | Admin items list | `/admin/items` | All items, including HIDDEN, with flag counts | ☐ | |
| AD4 | Admin users list | `/admin/users` | All users with karma + role | ☐ | |
| AD5 | Suspend a user | Pick a user → Suspend | User blocked from login (returns 403) | ☐ | |
| AD6 | Audit log | `/admin/audit-logs` | Recent admin actions logged | ☐ | |
| AD7 | Campus management | `/admin/campuses` → create/update | Campus list updates | ☐ | |
| AD8 | Force-complete handover | Pick a stuck claim → force complete | Item → RETURNED, both karmas awarded | ☐ | |
| AD9 | Analytics page | `/admin/analytics` | Charts render with real numbers | ☐ | |
| AD10 | Health check | `/admin/health` | DB + cache + websocket green | ☐ | |

## 7. Cross-cutting

| ID | Requirement | Steps | Expected | Pass/Fail | Evidence |
|----|-------------|-------|----------|-----------|----------|
| X1 | Light theme | Toggle to light | Tokens render correctly, no contrast bug | ☐ | |
| X2 | Dark theme | Toggle to dark | Tokens render correctly | ☐ | |
| X3 | Mobile-width responsive | Resize to <500px | Header, feed, modals all readable | ☐ | |
| X4 | Toast not alert() | Trigger any error | Toast component appears (no native `alert`) | ☐ | |
| X5 | Confirm dialog | Trigger any destructive action | `ConfirmDialog` component appears | ☐ | |
| X6 | Rate limit | Spam-login 30 times in a minute | 429 + `Retry-After` header | ☐ | |

---

## Summary

- **Tests run:** ____ / 60
- **Passed:** ____
- **Failed:** ____
- **Blocked / N/A:** ____

## Issues found (if any)

| ID | Test row | Severity | Notes |
|----|----------|----------|-------|
| | | | |
