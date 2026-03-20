# Mobile Phase 4 - Claim & Verification

> **Status:** PENDING
> **Depends On:** Backend Phase 4 (must be completed first)

---

## Objective

Wire existing claim screens to the real backend API and implement the Secret Detail answer flow on mobile.

---

## Pre-Existing Work

- `MyClaimsScreen.kt`, `ClaimDetailScreen.kt` built with mock data
- `MockClaims.kt` provides data structure reference

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Create `ClaimApiService.kt` | Retrofit interface for claim endpoints |
| 2 | Create `ClaimRepository.kt` | Repository layer for claim operations |
| 3 | Create `ClaimViewModel.kt` | ViewModel for claim screens |
| 4 | Wire `MyClaimsScreen` to real API | Replace mock data |
| 5 | Wire `ClaimDetailScreen` to real API | Fetch and display claim details |
| 6 | Implement claim submission flow | Secret Detail answer input + submit |
| 7 | Implement accept/reject UI for finders | Action buttons on incoming claims |

---

## Acceptance Criteria

- [ ] Users can submit claims with secret detail answers from mobile
- [ ] Finders can accept/reject claims on mobile
- [ ] My Claims screen shows real claim statuses
