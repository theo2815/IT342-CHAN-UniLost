# Phase 4 - Claim & Verification System (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE
> **Depends On:** Phase 3 (Item Management)

---

## Objective

Implement the claim and verification workflow that allows users to prove ownership of found items through the "Secret Detail" challenge system, and manage claim requests between finders and potential owners.

---

## Pre-Existing Work

- **Backend:** `ClaimEntity.java` and `ClaimRepository.java` already exist (no controller/service yet)
- **Website:** `IncomingClaims.jsx`, `ClaimDetail.jsx`, `ClaimModal.jsx` built with mock data
- **Mock Data:** `mockData/claims.js` provides data structure reference

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create `ClaimService.java` | Business logic for claim lifecycle |
| 2 | Create `ClaimController.java` | REST endpoints for claim operations |
| 3 | Secret Detail verification logic | Compare claimant's answer against the item's secret detail |
| 4 | Claim status management | State transitions: `PENDING` -> `ACCEPTED`/`REJECTED` |
| 5 | Notification trigger hooks | Prepare hooks for Phase 9 notifications |
| 6 | Prevent duplicate claims | One active claim per user per item |
| 7 | Fix status naming: `ACCEPTED` vs `APPROVED` | Align backend and frontend terminology |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/claims` | Submit a new claim on a found item (includes secret detail answer) |
| `GET` | `/api/claims/{id}` | Get claim details |
| `GET` | `/api/claims/user/{userId}` | Get all claims made by a user |
| `GET` | `/api/claims/item/{itemId}` | Get all claims on a specific item |
| `PUT` | `/api/claims/{id}/verify` | Finder accepts or rejects a claim based on secret detail answer |
| `GET` | `/api/claims/incoming` | Get all claims on items posted by the authenticated user |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Wire `ClaimModal` to real API | Submit claim with secret detail answer |
| 2 | Wire `IncomingClaims` page | Fetch claims on user's found items |
| 3 | Wire `ClaimDetail` page | Display claim details, accept/reject actions |
| 4 | Create `claimService.js` | API service for all claim endpoints |
| 5 | Add claim count badge to navigation | Show pending claims count |

---

## Technical Details

### Claim Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `itemId` | String | Reference to the claimed item |
| `claimantId` | String | User who is claiming the item |
| `finderId` | String | User who posted the found item |
| `secretDetailAnswer` | String | Claimant's answer to the secret question |
| `status` | Enum | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED` |
| `message` | String | Optional message from claimant to finder |
| `createdAt` | LocalDateTime | Claim submission timestamp |
| `updatedAt` | LocalDateTime | Last status change timestamp |

### Claim Workflow

```
1. Claimant sees a Found item in the feed
2. Claimant clicks "This is mine" -> ClaimModal opens
3. Claimant answers the Secret Detail question + optional message
4. System creates claim with status: PENDING
5. Finder sees claim in "Incoming Claims" page
6. Finder reviews the answer:
   - If correct -> ACCEPTED (triggers handover flow in Phase 7)
   - If wrong  -> REJECTED (claimant notified)
7. Claimant can CANCEL their own pending claim
```

### Business Rules
- A user cannot claim their own item
- Only one active (PENDING) claim per user per item
- Only the finder can accept/reject claims
- Accepting one claim does NOT auto-reject others (finder decides individually)
- Secret detail answers are compared as case-insensitive strings

---

## Acceptance Criteria

- [ ] Users can submit claims on found items with a secret detail answer
- [ ] Finders can view all incoming claims on their items
- [ ] Finders can accept or reject individual claims
- [ ] Claimants can view their submitted claims and statuses
- [ ] Duplicate claims (same user, same item) are prevented
- [ ] Status naming is consistent across backend and website
