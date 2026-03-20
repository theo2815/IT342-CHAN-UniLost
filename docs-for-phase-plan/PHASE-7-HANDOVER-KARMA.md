# Phase 7 - Handover System & Karma (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE (Handover) / SHOULD HAVE (Karma)
> **Depends On:** Phase 4 (Claim & Verification), Phase 6 (Messaging — for chat-based meetup coordination)

---

## Objective

Implement the dual-confirmation handover process where both parties confirm the physical return of an item, and the community Karma system that rewards finders for successful returns.

---

## Pre-Existing Work

- **Backend:** `HandoverEntity.java` and `HandoverRepository.java` already exist (entity + repo only, no controller/service)
- **Backend:** `UserEntity.java` already has `karmaScore` field (int, default 0) — ready for increment logic
- **Backend:** Claim accept flow (Phase 4) and auto-chat creation (Phase 6) are complete — handover initiation should hook into claim acceptance
- **Backend:** `ClaimService.java` has `acceptClaim()` which auto-rejects other claims and sets item to `CLAIMED` — handover creation should trigger here
- **Website:** `ClaimDetail.jsx` has a mock handover stepper (needs wiring to real API)
- **Website:** `Leaderboard/Leaderboard.jsx` exists with mock data (needs wiring to real API)
- **Website:** `Profile/Profile.jsx` already displays `karmaScore` from user data

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `HandoverService.java` | Business logic for handover lifecycle |
| 2 | Create `HandoverController.java` | REST endpoints for handover operations |
| 3 | Dual-confirmation logic | Both finder and owner must confirm before item is marked Returned |
| 4 | Admin override for office returns | Admin can single-handedly confirm returns for office turnovers |
| 5 | Karma score increment | Auto-increment finder's `karmaScore` on successful handover |
| 6 | Leaderboard queries | Global and per-campus karma rankings |
| 7 | Item status update on completion | Auto-update item status to `RETURNED` after confirmed handover |
| 8 | Hook into `ClaimService.acceptClaim()` | Auto-create handover record when a claim is accepted |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/handover/initiate` | Create a handover record after claim is accepted |
| `POST` | `/api/handover/{id}/confirm` | Confirm handover (called by each party independently) |
| `GET` | `/api/handover/{id}` | Get handover status |
| `GET` | `/api/handover/item/{itemId}` | Get handover for a specific item |
| `POST` | `/api/admin/handover/{id}/confirm` | Admin override confirmation for office returns |
| `GET` | `/api/users/leaderboard` | Global karma leaderboard (paginated) |
| `GET` | `/api/users/leaderboard/campus/{campusId}` | Campus-specific leaderboard |
| `GET` | `/api/users/{id}/karma` | Get user's karma score and rank |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire handover stepper in `ClaimDetail` | Replace mock stepper with real API data |
| 2 | Implement "Confirm Handover" button | Each party clicks independently |
| 3 | Wire `Leaderboard` page to real API | Replace mock data with real rankings |
| 4 | Karma score already on Profile | Already displayed from `user.karmaScore` — verify updates after handover |
| 5 | Create `handoverService.js` | API service for handover endpoints |
| 6 | Create `leaderboardService.js` | API service for leaderboard endpoints |

---

## Technical Details

### Handover Entity Fields (Current Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `claimId` | String | Reference to the accepted claim |
| `itemId` | String | Reference to the item |
| `finderConfirmed` | boolean | Has the finder confirmed the handover? |
| `ownerConfirmed` | boolean | Has the owner/claimant confirmed the handover? |
| `completedAt` | LocalDateTime | When both parties confirmed |
| `createdAt` | LocalDateTime | Handover initiation timestamp |

**Note:** Current entity uses `ownerConfirmed` (not `claimantConfirmed`). It does not yet have `finderId`/`claimantId` fields (must be resolved via claim), `adminConfirmed` field (for office returns), or a `status` enum. These may need to be added during implementation.

### Handover Repository (Current Implementation)
| Method | Description |
|--------|-------------|
| `findByClaimId(String claimId)` | Get handover for a specific claim |
| `findByItemId(String itemId)` | Get handover for a specific item |

### Handover Workflow

```
PEER-TO-PEER HANDOVER:
1. Claim is ACCEPTED by finder (Phase 4)
2. System creates Handover record (hook into ClaimService.acceptClaim())
3. Finder and claimant arrange meeting via Chat (Phase 6)
4. After physical exchange:
   - Finder clicks "Confirm Handover" -> finderConfirmed = true
   - Owner clicks "Confirm Handover" -> ownerConfirmed = true
5. When BOTH confirmed:
   - completedAt = now
   - Item status -> RETURNED
   - Finder's karmaScore += 10

OFFICE TURNOVER:
1. Finder brings item to Campus Security Office
2. Admin marks item as "Turned Over to Office" (Phase 5)
3. Owner comes to office and provides identification
4. Admin clicks "Admin Confirm Return"
   - Both confirmations set to true (or adminConfirmed field added)
   - Item status -> RETURNED
   - Finder's karmaScore += 10
```

### Karma Score System

| Action | Points |
|--------|--------|
| Successful peer handover (finder) | +10 |
| Successful office turnover (finder) | +10 |
| Helpful flag that leads to moderation | +2 |

**Field name:** `karmaScore` (not `karmaPoints`) in `UserEntity.java`

### Leaderboard Query
```
// Global: Top 50 users by karmaScore
db.users.find({accountStatus: "ACTIVE"}).sort({karmaScore: -1}).limit(50)

// Campus: Top 50 within a specific campus
db.users.find({universityTag: "xxx", accountStatus: "ACTIVE"}).sort({karmaScore: -1}).limit(50)
```

---

## Acceptance Criteria

- [ ] Handover record is auto-created when a claim is accepted
- [ ] Both parties can independently confirm the handover
- [ ] Item status auto-updates to RETURNED after dual confirmation
- [ ] Admin can single-handedly confirm office returns
- [ ] Finder receives karma score on successful handover
- [ ] Global and campus-specific leaderboards display correctly
- [ ] User profile shows karma score (already displayed, verify live updates)
