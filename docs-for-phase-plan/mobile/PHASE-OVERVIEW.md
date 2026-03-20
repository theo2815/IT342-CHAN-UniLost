# UniLost Mobile Phase Plan Overview

> **Platform:** Kotlin + Jetpack Compose (Android)
> **Architecture:** MVVM + Hilt DI + Retrofit + Room

---

## Prerequisites

Each mobile phase requires its corresponding **backend** to be completed first. The backend is built as part of the [Backend + Website track](../PHASE-OVERVIEW.md).

---

## Phase Roadmap

| Phase | Name | Status | Backend Required |
|-------|------|--------|-----------------|
| 1 | [Authentication](./PHASE-1-AUTHENTICATION.md) | COMPLETED | Phase 1 Backend |
| 2 | [User & Campus](./PHASE-2-USER-CAMPUS-MANAGEMENT.md) | COMPLETED | Phase 2 Backend |
| 3 | [Item Management](./PHASE-3-ITEM-MANAGEMENT.md) | PENDING | Phase 3 Backend |
| 4 | [Claim & Verification](./PHASE-4-CLAIM-VERIFICATION.md) | PENDING | Phase 4 Backend |
| 5 | [Admin Dashboard](./PHASE-5-ADMIN-DASHBOARD.md) | PENDING | Phase 5 Backend |
| 6 | [Messaging](./PHASE-6-MESSAGING.md) | PENDING | Phase 6 Backend |
| 7 | [Handover & Karma](./PHASE-7-HANDOVER-KARMA.md) | PENDING | Phase 7 Backend |
| 8 | [Campus Maps](./PHASE-8-CAMPUS-MAPS.md) | PENDING | Phase 8 Backend |
| 9 | [Push Notifications](./PHASE-9-NOTIFICATIONS.md) | PENDING | Phase 9 Backend |
| 10 | [Testing & Deployment](./PHASE-10-TESTING-DEPLOYMENT.md) | PENDING | Phase 10 Backend |

---

## Current Mobile State

### What Is Built
- Login & Register screens wired to real API
- `AuthRepository`, `AuthInterceptor`, `TokenManager` for JWT handling
- All other screens built with mock data (Dashboard, ItemFeed, ItemDetail, PostItem, MyItems, MyClaims, ClaimDetail, Notifications, Admin, Profile, Settings)
- Hilt DI configured, Retrofit + Room scaffolding in place
- Navigation graph with bottom nav

### Known Issues
- `GET /schools` call misaligned with backend `GET /api/campuses`
- Forgot-password flow not built yet on mobile
- Mock data structures slightly differ from backend responses
