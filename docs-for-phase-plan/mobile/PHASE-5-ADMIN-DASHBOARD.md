# Mobile Phase 5 - Admin Dashboard

> **Status:** PENDING
> **Depends On:** Backend Phase 5 (must be completed first)

---

## Objective

Wire the existing admin screen to the real backend API and implement item flagging on mobile.

---

## Pre-Existing Work

- `AdminScreen.kt` built with mock data
- `MockAdminData.kt` provides data structure reference

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Create `AdminApiService.kt` | Retrofit interface for admin endpoints |
| 2 | Create `AdminRepository.kt` | Repository layer |
| 3 | Create `AdminViewModel.kt` | ViewModel for admin screens |
| 4 | Wire `AdminScreen` to real API | Replace mock data with campus-scoped data |
| 5 | Implement flagging UI | "Report" button on item cards for regular users |
| 6 | Implement admin actions | Suspend users, hide posts, mark turned-over-to-office |

---

## Acceptance Criteria

- [ ] Admin screen shows real campus-scoped data
- [ ] Admins can moderate content from mobile
- [ ] Regular users can flag suspicious posts
