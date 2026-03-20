# Phase 5 - Admin Moderation Dashboard (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE
> **Depends On:** Phase 3 (Item Management), Phase 4 (Claim & Verification)

---

## Objective

Build the campus-scoped administrative system that allows university security teams to moderate content, manage users, handle physical item turnovers, and view campus-specific analytics.

---

## Pre-Existing Work

- **Backend:** `SecurityConfig.java` has role-based rules; `ADMIN` role exists in user entity
- **Website:** `AdminDashboard.jsx`, `AdminItems.jsx`, `AdminUsers.jsx`, `AdminClaims.jsx`, `SuperAdminPanel.jsx` all built with mock data
- **Mock Data:** `mockData/adminData.js` provides data structure reference

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `AdminController.java` | REST endpoints for admin operations |
| 2 | Create `AdminService.java` | Business logic with campus-scoping |
| 3 | Campus-scoped data access | Admins can only manage items/users within their university domain |
| 4 | Flagging system backend | Allow users to flag posts; admins review flagged content |
| 5 | User suspension/reactivation | Admin can change user status to `SUSPENDED` or reactivate |
| 6 | Item status: "Turned Over to Office" | New status for physical office turnover |
| 7 | Admin confirm return | Admin marks an item as returned when claimed from office |
| 8 | Campus analytics endpoints | Most common lost locations, recovery times, item counts |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | Campus-specific stats (items count, active claims, recovery rate) |
| `GET` | `/api/admin/items` | All items within admin's campus (with filters) |
| `GET` | `/api/admin/items/flagged` | Flagged items pending review |
| `PUT` | `/api/admin/items/{id}/status` | Update item status (hide, turned-over-to-office, admin-confirm-return) |
| `DELETE` | `/api/admin/items/{id}` | Force remove inappropriate content |
| `GET` | `/api/admin/users` | All users within admin's campus |
| `PUT` | `/api/admin/users/{id}/status` | Suspend or reactivate a user |
| `GET` | `/api/admin/claims` | All claims within admin's campus |
| `GET` | `/api/admin/analytics` | Campus analytics data |
| `POST` | `/api/items/{id}/flag` | User-facing: Flag an item as suspicious (not admin-only) |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire `AdminDashboard` to real API | Replace mock data with dashboard stats |
| 2 | Wire `AdminItems` page | Fetch and manage campus items |
| 3 | Wire `AdminUsers` page | View and manage campus users |
| 4 | Wire `AdminClaims` page | View campus claims |
| 5 | Implement flagging UI for regular users | "Report" button on item cards |
| 6 | Implement "Turned Over to Office" flow | Admin action on item detail |
| 7 | Create `adminService.js` | API service for admin endpoints |

---

## Technical Details

### Role-Based Access Control (RBAC)

```
STUDENT  -> Can post items, make claims, flag posts, view feed
FACULTY  -> Same as Student + priority visibility (future enhancement)
ADMIN    -> All of above + campus-scoped moderation powers
```

### Campus Scoping Logic
```java
// Admin can only access data within their university
String adminCampusId = currentUser.getCampusId();
List<Item> items = itemRepository.findByCampusId(adminCampusId);
```

### Flagging System

| Flag Reason | Description |
|-------------|-------------|
| `SPAM` | Irrelevant or repetitive post |
| `INAPPROPRIATE` | Offensive content |
| `FAKE` | Appears to be a fraudulent claim |
| `DUPLICATE` | Same item posted multiple times |

### Item Status Expanded (for Admin)
```
ACTIVE -> RESOLVED (owner found)
ACTIVE -> TURNED_OVER_TO_OFFICE (finder brought to security)
TURNED_OVER_TO_OFFICE -> RETURNED (admin confirms physical return)
ACTIVE -> HIDDEN (admin hides flagged content)
```

### Analytics Data Points
- Total items reported (lost vs found) per month
- Average recovery time (posting to resolution)
- Most common lost locations within campus
- Top item categories
- Active vs resolved ratio

---

## Acceptance Criteria

- [ ] Admins can only see and manage data from their own campus
- [ ] Admins can review and act on flagged posts (hide, delete)
- [ ] Admins can suspend and reactivate user accounts
- [ ] Admins can mark items as "Turned Over to Office"
- [ ] Admins can confirm returns for office turnovers
- [ ] Users can flag suspicious posts
- [ ] Campus analytics dashboard shows meaningful metrics
- [ ] Non-admin users cannot access admin endpoints (403)
