# Phase 4 - Claim & Verification System (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** Phase 3 (Item Management)

---

## Objective

Implement the claim and verification workflow that allows users to prove ownership of found items through the "Secret Detail" challenge system, and manage claim requests between finders and potential owners.

---

## Pre-Existing Work

- **Backend:** `ClaimEntity.java` and `ClaimRepository.java` already existed (no controller/service)
- **Website:** `IncomingClaims.jsx`, `ClaimDetail.jsx`, `ClaimModal.jsx` built with mock data

---

## Backend (Spring Boot) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | `ClaimService.java` | DONE | Full claim lifecycle: submit, accept, reject, cancel, with batch DTO conversion |
| 2 | `ClaimController.java` | DONE | REST endpoints with pagination, ownership checks |
| 3 | Secret Detail verification | DONE | Finder reviews claimant's answer to item's secret detail question |
| 4 | Claim status management | DONE | `PENDING` -> `ACCEPTED`/`REJECTED`/`CANCELLED` via `ClaimStatus` enum |
| 5 | Duplicate claim prevention | DONE | One PENDING claim per user per item |
| 6 | Auto-reject on accept | DONE | Accepting one claim auto-rejects all other PENDING claims on the same item |
| 7 | Item status auto-update | DONE | Item status changes to `CLAIMED` when a claim is accepted |
| 8 | Optimistic locking | DONE | `@Version` field on `ClaimEntity` prevents concurrent modification |
| 9 | Auto-create chat on claim | DONE | `ChatService.createChatForClaim()` called on claim submission |
| 10 | `ClaimDTO.java` + `ClaimRequest.java` | DONE | Response DTO with resolved item/claimant/finder info + chatId |

### API Endpoints (Implemented)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/claims` | Auth | Submit a new claim (includes secret detail answer + message) |
| `GET` | `/api/claims/{id}` | Auth | Get claim details (claimant, finder, or admin only) |
| `GET` | `/api/claims/my` | Auth | Get current user's submitted claims (paginated) |
| `GET` | `/api/claims/incoming` | Auth | Get claims on current user's items (paginated) |
| `GET` | `/api/claims/item/{itemId}` | Auth | Get all claims on a specific item (finder or admin only) |
| `PUT` | `/api/claims/{id}/accept` | Auth | Accept a claim (finder or admin only) |
| `PUT` | `/api/claims/{id}/reject` | Auth | Reject a claim (finder or admin only) |
| `PUT` | `/api/claims/{id}/cancel` | Auth | Cancel own pending claim |

## Website (React + Vite) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Wire `ClaimModal` | DONE | Submit claim with secret detail answer via `claimService` |
| 2 | Wire `IncomingClaims` page | DONE | Paginated fetch, approve/reject with immediate refresh |
| 3 | Wire `ClaimDetail` page | DONE | Full claim detail view with status badge, AbortController |
| 4 | Wire `MyClaims` page | DONE | Paginated list of user's submitted claims |
| 5 | `claimService.js` created | DONE | All claim API endpoints with `{ success, data, error }` pattern |
| 6 | Pagination on all claim pages | DONE | ChevronLeft/ChevronRight page controls |

---

## Technical Details

### Claim Entity Fields (Actual Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `itemId` | String | Reference to the claimed item (`@Indexed`) |
| `claimantId` | String | User making the claim (`@Indexed`) |
| `finderId` | String | User who posted the found item (`@Indexed`) |
| `providedAnswer` | String | Claimant's answer to the secret question |
| `message` | String | Optional message from claimant to finder |
| `status` | `ClaimStatus` enum | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED` |
| `createdAt` | LocalDateTime | Claim submission timestamp |
| `updatedAt` | LocalDateTime | Last status change timestamp |
| `version` | Long | Optimistic locking (`@Version`) |

### ClaimDTO Fields
Includes resolved: `itemTitle`, `itemType`, `itemImageUrl`, `claimantName`, `claimantSchool`, `finderName`, `secretDetailQuestion` (finder/admin only), `chatId`

### Claim Workflow
```
1. Claimant sees a Found item in the feed
2. Claimant clicks "This is mine" -> ClaimModal opens
3. Claimant answers the Secret Detail question + optional message
4. System creates claim with status: PENDING
5. System auto-creates a Chat room between finder and claimant
6. Finder sees claim in "Incoming Claims" page
7. Finder reviews the answer:
   - If correct -> ACCEPTED (all other PENDING claims auto-rejected, item -> CLAIMED)
   - If wrong  -> REJECTED
8. Claimant can CANCEL their own pending claim
```

### Business Rules
- A user cannot claim their own item
- Only one active (PENDING) claim per user per item
- Only the finder (or admin) can accept/reject claims
- Accepting one claim auto-rejects all other PENDING claims on the same item
- Item status changes to `CLAIMED` when a claim is accepted

---

## Acceptance Criteria

- [x] Users can submit claims on found items with a secret detail answer
- [x] Finders can view all incoming claims on their items
- [x] Finders can accept or reject individual claims
- [x] Accepting a claim auto-rejects all other pending claims
- [x] Claimants can view their submitted claims and statuses
- [x] Duplicate claims (same user, same item) are prevented
- [x] Chat room auto-created on claim submission
- [x] Optimistic locking prevents concurrent claim modification
