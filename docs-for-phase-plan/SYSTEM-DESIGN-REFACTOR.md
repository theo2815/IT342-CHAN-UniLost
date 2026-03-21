# System Design Refactor — UniLost

> **Created:** March 21, 2026  
> **Status:** PLANNED  
> **Scope:** Backend (Spring Boot) · Frontend (React + Vite) · Mobile (Kotlin)  
> **Depends On:** Phase 4 (Claims), Phase 6 (Messaging), Phase 7 (Handover)

---

## Purpose

This document captures three structural refactors that simplify the UniLost platform, centralize user interactions around **Profile** and **Chat**, and strengthen the handover flow with dual-confirmation security.

| # | Refactor | Goal |
|---|----------|------|
| 1 | [User Role Simplification](#1-user-role-simplification) | Reduce four roles to three; remove `FACULTY` and `SUPER_ADMIN`, add `GUEST` |
| 2 | [Handover System Redesign](#2-handover-system-redesign-chat-based-flow) | Chat-driven claim→handover flow with dual confirmation |
| 3 | [Profile-Centric Navigation](#3-profile-centric-navigation-refactor) | Merge My Items / My Claims into Profile tabs |

---

## 1. User Role Simplification

### Current State

```java
// Role.java (current)
public enum Role {
    STUDENT, FACULTY, ADMIN, SUPER_ADMIN
}
```

- `FACULTY` is used alongside `ADMIN` in `@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")` and `SecurityConfig`.
- `SUPER_ADMIN` exists in the enum but has no dedicated authorization rules.
- The system conflates campus-admin duties (`FACULTY`) with system-admin duties (`ADMIN`).

### Target State

| Role | Description | Access Level |
|------|-------------|-------------|
| **STUDENT** | Primary user — students from any university | Full read/write: post items, submit claims, chat, handover, profile |
| **GUEST** | Unauthenticated / read-only visitor | Read-only: Item Feed, Campus Map, Leaderboard |
| **ADMIN** | UniLost system administrator (developer-level) | Everything a `STUDENT` can do + admin dashboard, user management, campus CRUD, item moderation, handover override |

### Changes Required

#### Backend

| File / Area | Change |
|-------------|--------|
| `Role.java` | Remove `FACULTY` and `SUPER_ADMIN`. Add `GUEST` (used only as a conceptual label; guests are unauthenticated and do not have a stored role). Final enum: `STUDENT`, `ADMIN` |
| `SecurityConfig.java` | Replace all `hasAnyRole('ADMIN', 'FACULTY')` with `hasRole('ADMIN')`. Guest access is already handled by `.permitAll()` on public GET endpoints |
| `AdminController.java` | Change class-level `@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")` → `@PreAuthorize("hasRole('ADMIN')")`. Remove the `FACULTY`-only `getCrossCampusStats()` guard — make it `ADMIN`-only or remove if unused |
| `AdminService.java` | Audit `verifyCampusAccess()` — decide whether `ADMIN` is campus-scoped or global. Recommendation: `ADMIN` is **global** (no campus scoping) |
| `DataSeeder.java` | Update seeded users — remove any `FACULTY` or `SUPER_ADMIN` test accounts. Seed one `ADMIN` and several `STUDENT` accounts |
| `UserEntity.java` | Default role remains `Role.STUDENT`. Existing `FACULTY`/`SUPER_ADMIN` documents in MongoDB should be migrated to `ADMIN` or `STUDENT` as appropriate |
| `AuthService.java` | Ensure registration always assigns `STUDENT`. Admin accounts are created manually or via seeder only |

#### Frontend (React)

| File / Area | Change |
|-------------|--------|
| Route guards / `AdminRoute.jsx` | Replace any `role === 'FACULTY'` checks with `role === 'ADMIN'` |
| UI conditions (sidebar, nav) | Remove FACULTY-specific menu items or labels |
| Constants (if any role list exists) | Update to `['STUDENT', 'ADMIN']` |
| Login / registration forms | No role picker — all registrations are `STUDENT` |

#### Database (MongoDB)

| Action | Details |
|--------|---------|
| Migration script | Update all documents where `role = "FACULTY"` → `role = "ADMIN"` and `role = "SUPER_ADMIN"` → `role = "ADMIN"` |
| Validation | After migration, verify no documents have roles outside `{STUDENT, ADMIN}` |

#### Guest User Behavior

Guests are **not stored in the database** — they are simply unauthenticated visitors. Access is controlled by existing `permitAll()` rules in `SecurityConfig`:

| Endpoint | Guest Access |
|----------|-------------|
| `GET /api/items`, `GET /api/items/**` | ✅ Item feed (read-only) |
| `GET /api/campuses`, `GET /api/campuses/**` | ✅ Campus data / maps |
| `GET /api/users/leaderboard` | ✅ Karma leaderboard |
| `POST /api/claims`, `POST /api/chats/**` | ❌ Requires authentication |
| `/api/admin/**` | ❌ Requires `ADMIN` role |

---

## 2. Handover System Redesign (Chat-Based Flow)

### Core Principle

> All claim, verification, and handover interactions occur **inside the Chat/Messaging system**. The chat is the single source of truth for the item recovery lifecycle after a claim is submitted.

### Updated End-to-End Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  STEP 1 — ITEM POSTING (Finder)                                │
│  Finder posts a Found item with:                                │
│    • Blurred image (auto-blurred on upload)                     │
│    • Secret Detail question (e.g., "What sticker is on it?")    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  STEP 2 — CLAIM INITIATION (Owner)                              │
│  Owner clicks "I think this is mine" on the item card           │
│  Submits:                                                       │
│    • Secret Detail answer                                       │
│    • Optional message to finder                                 │
│                                                                 │
│  → System creates/opens a Chat conversation                     │
│  → System sends a structured CLAIM message in the chat          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  STEP 3 — CHAT-BASED VERIFICATION (Finder reviews in chat)      │
│  Finder receives the claim as a special message containing:     │
│    • Claim answer (Secret Detail response)                      │
│    • Owner's optional message                                   │
│    • Item card preview (thumbnail, title, status)               │
│                                                                 │
│  Finder can Accept or Reject the claim directly from chat       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  STEP 4 — NEGOTIATION & MEETUP (Both parties)                   │
│  Users communicate via chat to arrange a physical meetup        │
│  Quick-reply buttons: "I'm here", "Running late", "Meet at     │
│  Security Office"                                               │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  STEP 5 — HANDOVER CONFIRMATION (Dual-Confirmation)             │
│                                                                 │
│  Step 5a — Finder clicks "Mark as Returned to Owner"            │
│    → Item status: ACTIVE → PENDING_OWNER_CONFIRMATION           │
│    → System sends status-change message in chat                 │
│                                                                 │
│  Step 5b — Owner clicks "Confirm Item Received"                 │
│    → Item status: PENDING_OWNER_CONFIRMATION → RETURNED         │
│    → Handover record completed                                  │
│    → Finder's karmaScore incremented (+10)                      │
│    → System sends "Handover Complete" message in chat            │
└─────────────────────────────────────────────────────────────────┘
```

### Item Status Flow (Revised)

```
ACTIVE ──(claim accepted)──► CLAIMED ──(finder marks returned)──► PENDING_OWNER_CONFIRMATION ──(owner confirms)──► RETURNED
```

| Status | Meaning | Who Can Transition |
|--------|---------|--------------------|
| `ACTIVE` | Item is posted, open for claims | System (on post) |
| `CLAIMED` | A claim has been accepted by the finder | System (on `acceptClaim`) |
| `PENDING_OWNER_CONFIRMATION` | Finder has marked the item as physically returned | **Finder only** |
| `RETURNED` | Owner has confirmed receipt — handover complete | **Owner only** |

### Anti-Misuse Safeguards

| Safeguard | Implementation |
|-----------|---------------|
| **Dual confirmation required** | Item cannot reach `RETURNED` without both Finder and Owner confirming independently |
| **Strict RBAC on transitions** | Finder → `PENDING_OWNER_CONFIRMATION`; Owner → `RETURNED`. No cross-role transitions allowed |
| **Idempotency** | Repeated confirmation calls for the same step are no-ops (return current state, no error) |
| **Audit trail** | `HandoverEntity` stores `finderConfirmedAt`, `ownerConfirmedAt`, `completedAt` timestamps and corresponding user IDs |
| **Prevent false "returned" claims** | Owner must explicitly confirm — a finder alone cannot close the loop |
| **Timeout handling** (optional future) | If `PENDING_OWNER_CONFIRMATION` persists beyond N days, flag for admin review |

### Backend Changes

#### HandoverEntity (Updated Fields)

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `claimId` | String | Reference to the accepted claim |
| `itemId` | String | Reference to the item |
| `finderId` | String | User who found/reported the item |
| `ownerId` | String | User who claimed the item (owner) |
| `status` | `HandoverStatus` | `PENDING`, `PENDING_OWNER_CONFIRMATION`, `COMPLETED` |
| `finderConfirmed` | boolean | Finder has confirmed physical handover |
| `finderConfirmedAt` | LocalDateTime | Timestamp of finder confirmation |
| `ownerConfirmed` | boolean | Owner has confirmed receipt |
| `ownerConfirmedAt` | LocalDateTime | Timestamp of owner confirmation |
| `completedAt` | LocalDateTime | When both confirmations received |
| `createdAt` | LocalDateTime | Handover initiation timestamp |

#### New Enum: HandoverStatus

```java
public enum HandoverStatus {
    PENDING,                      // Handover record created, awaiting meetup
    PENDING_OWNER_CONFIRMATION,   // Finder confirmed, waiting for owner
    COMPLETED                     // Both confirmed — item is RETURNED
}
```

#### API Endpoints (Handover)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/handover/initiate` | System (internal) | Auto-created when a claim is accepted |
| `POST` | `/api/handover/{id}/finder-confirm` | Auth (Finder only) | Finder marks item as physically returned |
| `POST` | `/api/handover/{id}/owner-confirm` | Auth (Owner only) | Owner confirms receipt |
| `GET` | `/api/handover/{id}` | Auth | Get handover status |
| `GET` | `/api/handover/item/{itemId}` | Auth | Get handover for a specific item |
| `POST` | `/api/admin/handover/{id}/force-complete` | Admin | Admin override for edge cases |

#### Chat Message Types

Introduce a `messageType` field on `MessageEntity` to support structured messages:

| Type | Trigger | Content |
|------|---------|---------|
| `TEXT` | User sends a message | Free-form text (existing behavior) |
| `CLAIM` | Claim submitted | Structured: Secret Detail answer, owner message, item card |
| `CLAIM_ACCEPTED` | Finder accepts claim | System notification in chat |
| `CLAIM_REJECTED` | Finder rejects claim | System notification in chat |
| `HANDOVER_FINDER_CONFIRMED` | Finder clicks "Mark as Returned" | System notification + status update |
| `HANDOVER_COMPLETED` | Owner clicks "Confirm Received" | System notification + karma awarded |

```java
public enum MessageType {
    TEXT,
    CLAIM,
    CLAIM_ACCEPTED,
    CLAIM_REJECTED,
    HANDOVER_FINDER_CONFIRMED,
    HANDOVER_COMPLETED
}
```

#### MessageEntity Changes

| New Field | Type | Description |
|-----------|------|-------------|
| `messageType` | `MessageType` | Defaults to `TEXT` for backward compatibility |
| `metadata` | `Map<String, Object>` | Structured data for non-TEXT messages (e.g., claim details, item preview data) |

### Frontend Changes (Website)

#### Chat UI Enhancements

| Component | Change |
|-----------|--------|
| Message Bubble | Render differently based on `messageType`: `CLAIM` → item card + answer preview; `HANDOVER_*` → status banner |
| Item Card Preview in Chat | Small inline card: blurred thumbnail, item title, current status badge |
| Claim Action Buttons | Inside `CLAIM` message: "Accept Claim" / "Reject Claim" (visible to Finder only) |
| Handover Buttons | Conditional on role and state: |
| | **Finder** sees "Mark as Returned to Owner" when status is `CLAIMED` |
| | **Owner** sees "Confirm Item Received" when status is `PENDING_OWNER_CONFIRMATION` |
| Status Indicators | Inline badges in chat header: `Active`, `Pending Confirmation`, `Returned` |

#### Handover Service (`handoverService.js`)

```javascript
// New API service
export const handoverService = {
  getByItemId: (itemId) => api.get(`/handover/item/${itemId}`),
  finderConfirm: (handoverId) => api.post(`/handover/${handoverId}/finder-confirm`),
  ownerConfirm: (handoverId) => api.post(`/handover/${handoverId}/owner-confirm`),
  getStatus: (handoverId) => api.get(`/handover/${handoverId}`),
};
```

---

## 3. Profile-Centric Navigation Refactor

### Current State

| Page | Route | Purpose |
|------|-------|---------|
| My Items | `/my-items` | List of items posted by the current user |
| My Claims | `/my-claims` | List of claims submitted by the current user |
| Profile | `/profile` | User info, karma score, settings |

Three separate pages with independent routing, components, and API calls.

### Target State

| Action | Before | After |
|--------|--------|-------|
| Click "My Items" | Navigate to `/my-items` | Navigate to `/profile?tab=my-items` |
| Click "My Claims" | Navigate to `/my-claims` | Navigate to `/profile?tab=my-claims` |
| Click "Profile" | Navigate to `/profile` | Navigate to `/profile` (default tab: overview) |

### Profile Page Tabs

| Tab | Query Param | Content |
|-----|-------------|---------|
| **Overview** | `?tab=overview` (default) | User info, karma score, avatar, university |
| **My Items** | `?tab=my-items` | Paginated list of posted items with status badges |
| **My Claims** | `?tab=my-claims` | Paginated list of submitted claims with status badges |

### Frontend Changes

| Area | Change |
|------|--------|
| **Routing** | Remove `/my-items` and `/my-claims` routes. Add redirect rules: `/my-items` → `/profile?tab=my-items`, `/my-claims` → `/profile?tab=my-claims` |
| **Profile Page** | Add tab navigation driven by `useSearchParams()`. Render the correct tab content based on `tab` query parameter |
| **Sidebar / Nav** | Update "My Items" and "My Claims" links to point to `/profile?tab=my-items` and `/profile?tab=my-claims` respectively |
| **Components** | Move item-listing and claim-listing logic into tab sub-components within the Profile page. Reuse existing service calls (`itemService`, `claimService`) |
| **Cleanup** | Remove standalone `MyItems.jsx` and `MyClaims.jsx` page components (or keep as thin wrappers that redirect). Remove unused imports and route definitions |

### Implementation Notes

```jsx
// Profile.jsx — tab routing via query params
import { useSearchParams } from 'react-router-dom';

function Profile() {
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = searchParams.get('tab') || 'overview';

  return (
    <div>
      <TabBar activeTab={activeTab} onChange={(tab) => setSearchParams({ tab })} />
      {activeTab === 'overview'  && <ProfileOverview />}
      {activeTab === 'my-items'  && <MyItemsTab />}
      {activeTab === 'my-claims' && <MyClaimsTab />}
    </div>
  );
}
```

```jsx
// App.jsx — redirect old routes
<Route path="/my-items" element={<Navigate to="/profile?tab=my-items" replace />} />
<Route path="/my-claims" element={<Navigate to="/profile?tab=my-claims" replace />} />
```

---

## Migration Checklist

### Phase A — User Role Simplification

- [ ] Update `Role.java` enum → `{ STUDENT, ADMIN }`
- [ ] Update `SecurityConfig.java` → replace `FACULTY` references with `ADMIN`
- [ ] Update `AdminController.java` → `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Update `DataSeeder.java` → remove `FACULTY`/`SUPER_ADMIN` seeded accounts
- [ ] Write MongoDB migration script: `FACULTY` → `ADMIN`, `SUPER_ADMIN` → `ADMIN`
- [ ] Update frontend role guards and constants
- [ ] Test: STUDENT can post items, submit claims, chat
- [ ] Test: ADMIN can access `/api/admin/**` endpoints
- [ ] Test: Unauthenticated users (GUEST) can only access public GET endpoints

### Phase B — Handover System Redesign

- [ ] Add `HandoverStatus` enum
- [ ] Update `HandoverEntity` with new fields (`finderId`, `ownerId`, `status`, timestamps)
- [ ] Add `MessageType` enum and `messageType` + `metadata` fields to `MessageEntity`
- [ ] Create `HandoverService.java` with dual-confirmation logic
- [ ] Create `HandoverController.java` with RBAC-guarded endpoints
- [ ] Hook handover creation into `ClaimService.acceptClaim()`
- [ ] Emit structured chat messages (`CLAIM`, `HANDOVER_*`) on state transitions
- [ ] Create `handoverService.js` on frontend
- [ ] Update Chat UI to render structured message types (item card, claim details, status banners)
- [ ] Add conditional Finder/Owner buttons in chat
- [ ] Add chat-header status indicators
- [ ] Test: Full flow ACTIVE → CLAIMED → PENDING_OWNER_CONFIRMATION → RETURNED
- [ ] Test: Finder cannot confirm as Owner (and vice versa)
- [ ] Test: Repeated confirmations are idempotent
- [ ] Test: Karma score increments on completion

### Phase C — Profile-Centric Navigation

- [ ] Add tab routing (`useSearchParams`) to `Profile.jsx`
- [ ] Create `MyItemsTab` and `MyClaimsTab` sub-components
- [ ] Update sidebar/nav links to `/profile?tab=*`
- [ ] Add redirect routes for `/my-items` → `/profile?tab=my-items`, `/my-claims` → `/profile?tab=my-claims`
- [ ] Remove or deprecate standalone `MyItems.jsx` and `MyClaims.jsx` pages
- [ ] Verify deep-linking works (`/profile?tab=my-claims` opens correct tab)
- [ ] Test: Back/forward browser navigation preserves tab state

---

## Architectural Alignment

| Layer | Technology | Impact |
|-------|-----------|--------|
| Backend | Spring Boot 4.0.2 + Spring Security | Role enum change, `SecurityConfig` rules, new Handover controller/service |
| Database | MongoDB Atlas | Role field migration, new `HandoverStatus` enum, `MessageType` field on messages |
| Frontend (Web) | React 19 + Vite 7 + React Router | Route changes, Profile tab refactor, Chat UI structured messages |
| Frontend (Mobile) | Kotlin + Jetpack Compose | Same role/handover/profile changes (deferred until mobile catches up to Phase 7+) |
| Real-Time | WebSocket (STOMP over SockJS) | Structured message types broadcast alongside text messages |

---

## Risk & Considerations

| Risk | Mitigation |
|------|------------|
| Existing `FACULTY` users in production MongoDB | Run migration script before deploying role enum change. Add a startup validator that logs warnings for unknown roles |
| Backward compatibility of `MessageType` | Default to `TEXT` for all existing messages. Frontend must gracefully handle messages without `messageType` |
| Chat UI complexity increase | Use a message renderer factory — each `MessageType` maps to a dedicated React component |
| HANDED_OVER (deprecated status) | Keep in enums for backward compat with old MongoDB documents; exclude from new flows and UI |
