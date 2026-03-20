# Mobile Phase 3 - Item Management

> **Status:** PENDING
> **Depends On:** Backend Phase 3 (must be completed first)

---

## Objective

Wire all existing item screens to the real backend API, replacing mock data with live data. Also fix deferred issues from Phases 1-2.

---

## Pre-Existing Work

- `ItemFeedScreen.kt`, `ItemDetailScreen.kt`, `PostItemScreen.kt`, `MyItemsScreen.kt` all built with mock data
- `MockItems.kt` provides data structure reference

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Create `ItemApiService.kt` | Retrofit interface for item endpoints |
| 2 | Create `ItemRepository.kt` | Repository layer for item operations |
| 3 | Create `ItemFeedViewModel.kt` | ViewModel for item feed screen |
| 4 | Wire `ItemFeedScreen` to real API | Replace mock data |
| 5 | Wire `PostItemScreen` to real API | Image upload with multipart |
| 6 | Wire `ItemDetailScreen` to real API | Fetch item by ID |
| 7 | Wire `MyItemsScreen` to real API | Fetch user's items |
| 8 | Wire Profile screen to real user API | Deferred from Mobile Phase 2 |
| 9 | Wire Settings screen to real API | Deferred from Mobile Phase 2 |
| 10 | Fix `GET /schools` -> `GET /api/campuses` mismatch | Align mobile API calls with backend |
| 11 | Build forgot-password flow | ForgotPasswordScreen, VerifyOtpScreen, ResetPasswordScreen |

---

## Acceptance Criteria

- [ ] All item screens display real API data
- [ ] Users can post items with image upload from mobile
- [ ] Search and filter work against real backend
- [ ] Profile and Settings use real user data
- [ ] Campus API alignment fixed
- [ ] Forgot-password flow works on mobile
