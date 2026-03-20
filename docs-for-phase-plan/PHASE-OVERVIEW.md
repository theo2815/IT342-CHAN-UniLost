# UniLost - Development Phase Plan Overview

## Project Summary

**UniLost** is a centralized Lost and Found platform for the academic community across Cebu City. It connects students, faculty, and campus security from multiple universities (CIT-U, USC, USJ-R, UC, UP Cebu, SWU, CNU, CTU) through a single system for reporting and recovering missing items.

**Tech Stack:** Java 21 (Spring Boot) | MongoDB Atlas | React + Vite (Web) | Kotlin + Jetpack Compose (Mobile)

---

## Document Structure

This phase plan is split into **two tracks** so they can be worked on independently:

| Track | Location | Focus |
|-------|----------|-------|
| **Backend + Website** | This folder (`docs-for-phase-plan/`) | Spring Boot API + React frontend |
| **Mobile** | [`docs-for-phase-plan/mobile/`](./mobile/) | Kotlin + Jetpack Compose Android app |

> **Current Focus: Backend + Website**

---

## Phase Roadmap (Backend + Website)

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| 1 | [Authentication System](./PHASE-1-AUTHENTICATION.md) | COMPLETED | User registration, login, JWT, password reset with OTP |
| 2 | [User & Campus Management](./PHASE-2-USER-CAMPUS-MANAGEMENT.md) | COMPLETED | User CRUD, campus CRUD, data seeding, domain validation |
| 3 | [Item Management](./PHASE-3-ITEM-MANAGEMENT.md) | COMPLETED | Item posting, search, filters, image upload, soft deletes |
| 4 | [Claim & Verification](./PHASE-4-CLAIM-VERIFICATION.md) | PENDING | Secret Detail challenge, claim requests, verification flow |
| 5 | [Admin Dashboard](./PHASE-5-ADMIN-DASHBOARD.md) | PENDING | Campus-scoped moderation, flagging, user management |
| 6 | [Messaging](./PHASE-6-MESSAGING.md) | PENDING | In-app chat between finder and claimant |
| 7 | [Handover & Karma](./PHASE-7-HANDOVER-KARMA.md) | PENDING | Dual-confirm handover, karma leaderboard |
| 8 | [Campus Maps](./PHASE-8-CAMPUS-MAPS.md) | PENDING | Leaflet.js map, pin placement, heatmaps |
| 9 | [Notifications](./PHASE-9-NOTIFICATIONS.md) | PENDING | Real-time WebSocket notifications |
| 10 | [Testing & Deployment](./PHASE-10-TESTING-DEPLOYMENT.md) | PENDING | Testing, security, production deploy |

## Phase Roadmap (Mobile) - [View All](./mobile/)

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Authentication](./mobile/PHASE-1-AUTHENTICATION.md) | COMPLETED |
| 2 | [User & Campus](./mobile/PHASE-2-USER-CAMPUS-MANAGEMENT.md) | COMPLETED |
| 3 | [Item Management](./mobile/PHASE-3-ITEM-MANAGEMENT.md) | PENDING |
| 4 | [Claim & Verification](./mobile/PHASE-4-CLAIM-VERIFICATION.md) | PENDING |
| 5 | [Admin Dashboard](./mobile/PHASE-5-ADMIN-DASHBOARD.md) | PENDING |
| 6 | [Messaging](./mobile/PHASE-6-MESSAGING.md) | PENDING |
| 7 | [Handover & Karma](./mobile/PHASE-7-HANDOVER-KARMA.md) | PENDING |
| 8 | [Campus Maps](./mobile/PHASE-8-CAMPUS-MAPS.md) | PENDING |
| 9 | [Push Notifications](./mobile/PHASE-9-NOTIFICATIONS.md) | PENDING |
| 10 | [Testing & Deployment](./mobile/PHASE-10-TESTING-DEPLOYMENT.md) | PENDING |

---

## Phase Dependencies

```
Phase 1 (Auth) -----> Phase 2 (User & Campus)
                          |
                          v
                      Phase 3 (Items) ---------> Phase 4 (Claims)
                          |                          |
                          v                          v
                      Phase 5 (Admin) <--------- Phase 7 (Handover & Karma)
                          |                          |
                          v                          v
                      Phase 8 (Maps)            Phase 6 (Messaging)
                                                     |
                                                     v
                                                Phase 9 (Notifications)
                                                     |
                                                     v
                                                Phase 10 (Deploy)
```

> **Note:** Mobile phases depend on the backend being completed first in each corresponding phase. Complete the backend + website track before starting the same mobile phase.

---

## Current Project State

### What Is Built
- **Backend:** Auth endpoints, User CRUD, Campus CRUD, JWT security, data seeder (8 universities), Item CRUD with search/filters, Cloudinary image upload, GlobalExceptionHandler, soft deletes
- **Website:** All 23 page components built — auth pages, item feed, item detail, post item, profile, settings, dashboard all wired to real API
- **Mobile:** Login & Register wired to API, all other screens built with mock data
- **Design System:** Reusable UI components with dark/light theme support

### Resolved Issues (from previous phases)
1. ~~Claim status mismatch~~ — Backend uses `ACCEPTED/REJECTED`, frontend mock uses `APPROVED/REJECTED` (will be addressed in Phase 4)
2. ~~Item categories mismatch~~ — `WALLETS` category added, all 9 categories validated via `@Pattern`
3. ~~No GlobalExceptionHandler~~ — `@RestControllerAdvice` handling 400/403/404/413/500
4. **No JWT refresh token rotation** — Still pending (future enhancement)

### Immediate Next Step
**Phase 4 - Claim & Verification (Backend + Website)**
