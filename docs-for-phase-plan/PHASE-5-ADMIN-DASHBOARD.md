# Phase 5 - Admin Moderation Dashboard (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** Phase 3 (Item Management), Phase 4 (Claim & Verification)

---

## Objective

Build the campus-scoped administrative system that allows university security teams to moderate content, manage users, handle flagged items, and view campus-specific analytics.

---

## Pre-Existing Work

- **Backend:** `SecurityConfig.java` had role-based rules; `ADMIN` role existed in user entity
- **Website:** `AdminDashboard.jsx`, `AdminItems.jsx`, `AdminUsers.jsx`, `AdminClaims.jsx`, `SuperAdminPanel.jsx` built with mock data

---

## Backend (Spring Boot) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | `AdminController.java` | DONE | REST endpoints for admin operations with `@PreAuthorize` |
| 2 | `AdminService.java` | DONE | Campus-scoped business logic with batch DTO conversion |
| 3 | Campus-scoped data access | DONE | Admin's `universityTag` used to filter items/users/claims |
| 4 | Flagging system | DONE | `flagCount` field on items, admin can view flagged items |
| 5 | User suspension/reactivation | DONE | Admin can update `AccountStatus` to `SUSPENDED`/`ACTIVE` |
| 6 | Item status management | DONE | Admin can update `ItemStatus` (ACTIVE, CLAIMED, RETURNED, etc.) |
| 7 | Campus analytics | DONE | Dashboard stats: item counts, user counts, claim counts, recovery rate |
| 8 | Super Admin cross-campus stats | DONE | Faculty-only endpoint for all campus statistics |

### API Endpoints (Implemented)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/admin/dashboard` | ADMIN/FACULTY | Campus-specific stats |
| `GET` | `/api/admin/items` | ADMIN/FACULTY | All items within admin's campus (paginated) |
| `GET` | `/api/admin/items/flagged` | ADMIN/FACULTY | Flagged items pending review |
| `PUT` | `/api/admin/items/{id}/status` | ADMIN/FACULTY | Update item status |
| `DELETE` | `/api/admin/items/{id}` | ADMIN/FACULTY | Force delete item |
| `GET` | `/api/admin/users` | ADMIN/FACULTY | All users within admin's campus (paginated) |
| `PUT` | `/api/admin/users/{id}/status` | ADMIN/FACULTY | Suspend or reactivate a user |
| `GET` | `/api/admin/claims` | ADMIN/FACULTY | All claims within admin's campus (paginated) |
| `GET` | `/api/admin/campus-stats` | FACULTY | Cross-campus statistics (Super Admin) |

## Website (React + Vite) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Wire `AdminDashboard` | DONE | Real API stats with loading/error states |
| 2 | Wire `AdminItems` page | DONE | Paginated items list with status actions |
| 3 | Wire `AdminUsers` page | DONE | Paginated users list with suspend/activate actions |
| 4 | Wire `AdminClaims` page | DONE | Paginated claims list |
| 5 | Wire `SuperAdminPanel` | DONE | Cross-campus stats grid, school management table with edit |
| 6 | `adminService.js` created | DONE | All admin API endpoints |

---

## Technical Details

### Role-Based Access Control (Actual Implementation)
```
STUDENT  -> Can post items, make claims, flag posts, view feed, chat
ADMIN    -> All of above + campus-scoped moderation powers
FACULTY  -> All of ADMIN + cross-campus super admin panel
```

### Campus Scoping Logic
```java
String adminCampusId = currentUser.getUniversityTag();
// All admin queries filter by campusId matching adminCampusId
```

### MongoDB Indexes for Admin Queries
- `idx_campus_status_deleted` — compound on `(campusId, status, isDeleted)`
- `idx_campus_deleted_flagged` — compound on `(campusId, isDeleted, flagCount)`
- `idx_campus_account_status` — compound on `(universityTag, accountStatus)`

---

## Acceptance Criteria

- [x] Admins can only see and manage data from their own campus
- [x] Admins can view and act on flagged posts
- [x] Admins can suspend and reactivate user accounts
- [x] Admins can update item status
- [x] Campus analytics dashboard shows meaningful metrics
- [x] Faculty (Super Admin) can view cross-campus statistics
- [x] Non-admin users cannot access admin endpoints (403)
