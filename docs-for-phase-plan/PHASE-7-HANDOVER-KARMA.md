# Phase 7 - Handover System & Karma (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE (Handover) / SHOULD HAVE (Karma)
> **Depends On:** Phase 4 (Claim & Verification)

---

## Objective

Implement the dual-confirmation handover process where both parties confirm the physical return of an item, and the community Karma system that rewards finders for successful returns.

---

## Pre-Existing Work

- **Backend:** `HandoverEntity.java` and `HandoverRepository.java` already exist (no controller/service yet)
- **Website:** Mock data includes handover status fields in items and claims
- **User Entity:** `karmaPoints` field already exists in `UserEntity.java`

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `HandoverService.java` | Business logic for handover lifecycle |
| 2 | Create `HandoverController.java` | REST endpoints for handover operations |
| 3 | Dual-confirmation logic | Both finder and claimant must confirm before item is marked Returned |
| 4 | Admin override for office returns | Admin can single-handedly confirm returns for office turnovers |
| 5 | Karma points increment | Auto-increment finder's karma on successful handover |
| 6 | Leaderboard queries | Global and per-campus karma rankings |
| 7 | Item status update on completion | Auto-update item status to `RETURNED` after confirmed handover |

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
| 1 | Build Handover Status Screen | Show confirmation status for both parties |
| 2 | Implement "Confirm Handover" button | Each party clicks independently |
| 3 | Wire `Leaderboard` page to real API | Replace mock data with real rankings |
| 4 | Show karma score on Profile page | Display user's karma and rank |
| 5 | Create `handoverService.js` | API service for handover endpoints |
| 6 | Create `leaderboardService.js` | API service for leaderboard endpoints |

---

## Technical Details

### Handover Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `itemId` | String | Reference to the item |
| `claimId` | String | Reference to the accepted claim |
| `finderId` | String | User who found the item |
| `claimantId` | String | User who claimed the item |
| `finderConfirmed` | Boolean | Has the finder confirmed the handover? |
| `claimantConfirmed` | Boolean | Has the claimant confirmed the handover? |
| `adminConfirmed` | Boolean | Has an admin confirmed (office returns only)? |
| `status` | Enum | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `completedAt` | LocalDateTime | When both parties confirmed |
| `createdAt` | LocalDateTime | Handover initiation timestamp |

### Handover Workflow

```
PEER-TO-PEER HANDOVER:
1. Claim is ACCEPTED by finder (Phase 4)
2. System creates Handover with status: PENDING
3. Finder and claimant arrange meeting (via chat - Phase 6)
4. After physical exchange:
   - Finder clicks "Confirm Handover" -> finderConfirmed = true
   - Claimant clicks "Confirm Handover" -> claimantConfirmed = true
5. When BOTH confirmed:
   - Handover status -> COMPLETED
   - Item status -> RETURNED
   - Finder's karmaPoints += 10

OFFICE TURNOVER:
1. Finder brings item to Campus Security Office
2. Admin marks item as "Turned Over to Office" (Phase 5)
3. Owner comes to office and provides identification
4. Admin clicks "Admin Confirm Return"
   - adminConfirmed = true
   - Handover status -> COMPLETED
   - Item status -> RETURNED
   - Finder's karmaPoints += 10
```

### Karma Points System

| Action | Points |
|--------|--------|
| Successful peer handover (finder) | +10 |
| Successful office turnover (finder) | +10 |
| Helpful flag that leads to moderation | +2 |

### Leaderboard Query
```
// Global: Top 50 users by karmaPoints
db.users.find({status: "ACTIVE"}).sort({karmaPoints: -1}).limit(50)

// Campus: Top 50 within a specific campus
db.users.find({campusId: "xxx", status: "ACTIVE"}).sort({karmaPoints: -1}).limit(50)
```

---

## Acceptance Criteria

- [ ] Handover record is created when a claim is accepted
- [ ] Both parties can independently confirm the handover
- [ ] Item status auto-updates to RETURNED after dual confirmation
- [ ] Admin can single-handedly confirm office returns
- [ ] Finder receives karma points on successful handover
- [ ] Global and campus-specific leaderboards display correctly
- [ ] User profile shows karma score and rank
