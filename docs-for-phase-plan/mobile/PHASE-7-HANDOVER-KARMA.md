# Mobile Phase 7 - Handover & Karma

> **Status:** PENDING
> **Depends On:** Backend Phase 7 (must be completed first)

---

## Objective

Implement handover confirmation flow and karma leaderboard on mobile.

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Create `HandoverApiService.kt` | Retrofit interface for handover endpoints |
| 2 | Create `HandoverRepository.kt` | Repository layer |
| 3 | Create `HandoverViewModel.kt` | ViewModel for handover screens |
| 4 | Build Handover Status screen | Show confirmation status for both parties |
| 5 | Implement "Confirm Handover" button | Each party clicks independently |
| 6 | Build Leaderboard screen | Karma rankings display (global + campus) |
| 7 | Create `LeaderboardViewModel.kt` | ViewModel for leaderboard |
| 8 | Show karma on profile | Wire to real user karma data |

---

## Acceptance Criteria

- [ ] Both parties can confirm handover from mobile
- [ ] Handover status screen shows real-time confirmation state
- [ ] Leaderboard displays real karma rankings
- [ ] Profile shows user's karma score
