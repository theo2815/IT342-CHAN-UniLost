# Admin Panel — Phase Plan

## Overview

UniLost has a **single admin account** (`admin@unilost.com`) with full platform-wide access. There are no per-school admins or faculty roles — the admin is the developer/operator who manages the entire system.

### Design Principles

- **Single admin, full visibility** — one account sees and controls everything across all campuses
- **Minimal, clean UI** — admin pages are functional tools, not marketing dashboards
- **Secure by default** — admin endpoints are gated by `ROLE_ADMIN` at both Spring Security and method level
- **No scope leaks** — admin features are fully separated from the user-facing app

---

## Admin Responsibilities

| Area | What the Admin Does |
|------|---------------------|
| **Items** | Review flagged items, hide/unhide listings, mark items as turned over or returned, soft-delete inappropriate posts |
| **Users** | View all users across campuses, suspend/reactivate student accounts, monitor account statuses |
| **Claims** | View all claims, force-complete handovers when users fail to confirm |
| **Campuses** | View cross-campus stats (users, items, recovery rate), edit campus name and email domain |
| **Analytics** | View item status distribution, lost vs found counts, top categories, top locations, recovery rate |
| **Moderation** | Act on flagged items (spam, inappropriate, fake, duplicate), remove harmful content |

---

## Roles

| Role | Description |
|------|-------------|
| `STUDENT` | Default role. Regular platform user. |
| `ADMIN` | Single admin account (`admin@unilost.com`). Full system access. |

No other roles exist. The `FACULTY` and `SUPER_ADMIN` roles have been removed.

---

## Phase Breakdown

### Phase A — Admin Dashboard (Done)

**Route:** `/admin`

- [x] Global stats cards: Total Users, Active Items, Pending Claims, Recovered This Month, Suspended Users
- [x] Quick links to Items, Users, Claims, Campus management
- [x] Admin role badge
- [x] Stats are platform-wide (not campus-scoped)

### Phase B — Items Management (Done)

**Route:** `/admin/items`

- [x] Searchable, filterable table of all items across all campuses
- [x] Filter by type (LOST/FOUND) and status
- [x] Actions: Hide, Unhide, Turn Over to Office, Confirm Return, Soft Delete
- [x] Flagged items view (`/admin/items/flagged`)
- [x] Delete requires confirmation with reason

### Phase C — Users Management (Done)

**Route:** `/admin/users`

- [x] Searchable table of all users across all campuses
- [x] Filter by role (Student, Admin) and account status (Active, Suspended)
- [x] Suspend/reactivate student accounts with reason modal
- [x] Admin accounts cannot be suspended
- [x] Shows campus, karma, join date

### Phase D — Claims Management (Done)

**Route:** `/admin/claims`

- [x] Tabbed view: All, Pending, Accepted, Rejected, Cancelled
- [x] Shows item title, claimant, poster, status, date
- [x] Force-complete handover action
- [x] Paginated

### Phase E — Campus Management (Done)

**Route:** `/admin/campuses`

- [x] Cross-campus statistics grid (users, items, recovery rate per campus)
- [x] School settings table with edit capability
- [x] Edit campus name and email domain

### Phase F — Analytics (Done)

**Route:** `/admin/analytics`

- [x] Backend: Status counts, type counts, top categories, top locations, recovery rate
- [x] Frontend: Dedicated analytics page with charts (recharts — pie, bar, area charts for status distribution, lost vs found, top categories, top locations, items over time, campus comparison)

### Phase G — Future Enhancements (Done)

| Feature | Description | Status |
|---------|-------------|--------|
| **Audit Logs** | Log admin actions (suspend user, delete item, status changes) with timestamps. Route: `/admin/audit-logs` | Done |
| **Reports Dashboard** | Charts for item trends over time, campus comparisons, user growth (merged with Phase F Analytics) | Done |
| **Bulk Actions** | Select multiple items/users and apply actions in batch (checkbox selection + bulk toolbar) | Done |
| **Export Data** | CSV export of users, items, analytics (Export CSV buttons on Items, Users pages) | Done |
| **Admin Notifications** | Notify admin when flag count exceeds threshold (3+ flags triggers ITEM_FLAG_THRESHOLD notification) | Done |
| **System Health** | Monitor MongoDB status, JVM memory, uptime, collection counts. Route: `/admin/health` | Done |

---

## Access Control

### Backend Security

```
SecurityConfig:
  /api/admin/**  →  hasRole('ADMIN')
  /api/campuses (POST/PUT/DELETE)  →  hasRole('ADMIN')
  /api/users (GET all)  →  hasRole('ADMIN')

AdminController:
  @PreAuthorize("hasRole('ADMIN')")  (class-level)

AdminService.resolveAdmin():
  Verifies the caller has Role.ADMIN (throws ForbiddenException otherwise)
```

### Frontend Security

```
AdminRoute component:
  1. Check token validity (isAuthenticated)
  2. Call /auth/me to sync user from server
  3. Verify role === 'ADMIN'
  4. Redirect to /items if not admin

Header component:
  Shows "Admin Panel" link only if role === 'ADMIN'
```

### Key Security Guarantees

1. **Server-authoritative role** — JWT token role is ignored; the server re-fetches the role from DB on every request
2. **No client-side role escalation** — frontend checks are UX convenience; backend enforces all access
3. **Admin cannot be suspended** — `AdminService.updateUserStatus()` rejects status changes on ADMIN accounts
4. **Soft deletes only** — items are never hard-deleted; `isDeleted` flag preserves data for audit

---

## Data Model

The admin interacts with these entities:

| Entity | Admin Actions |
|--------|--------------|
| `UserEntity` | View, suspend, reactivate |
| `ItemEntity` | View, change status (ACTIVE/HIDDEN/TURNED_OVER/RETURNED), soft-delete |
| `ClaimEntity` | View, force-complete handover |
| `CampusEntity` | View stats, edit name and domain |

The admin account itself is seeded by `DataSeeder` on application startup using the `admin.seed-password` property.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Platform-wide stats |
| GET | `/api/admin/items` | All items (search, filter, paginate) |
| GET | `/api/admin/items/flagged` | Flagged items |
| PUT | `/api/admin/items/{id}/status` | Change item status |
| DELETE | `/api/admin/items/{id}` | Soft-delete item |
| GET | `/api/admin/users` | All users (search, filter, paginate) |
| PUT | `/api/admin/users/{id}/status` | Suspend/reactivate user |
| GET | `/api/admin/claims` | All claims (filter by status, paginate) |
| PUT | `/api/admin/claims/{id}/force-complete` | Force-complete handover |
| GET | `/api/admin/analytics` | Item analytics |
| GET | `/api/admin/campus-stats` | Per-campus statistics |
| GET | `/api/admin/audit-logs` | Audit log entries (paginated, filterable) |
| GET | `/api/admin/item-trends` | Items created per month (last N months) |
| GET | `/api/admin/health` | System health (MongoDB, memory, uptime) |
| GET | `/api/admin/export/users` | Export all users as CSV |
| GET | `/api/admin/export/items` | Export all items as CSV |
| GET | `/api/admin/export/analytics` | Export analytics summary as CSV |
| PUT | `/api/admin/items/bulk-status` | Bulk update item statuses |
| DELETE | `/api/admin/items/bulk-delete` | Bulk soft-delete items |
| PUT | `/api/admin/users/bulk-status` | Bulk update user statuses |

All endpoints require `Authorization: Bearer <token>` with an ADMIN-role token.
