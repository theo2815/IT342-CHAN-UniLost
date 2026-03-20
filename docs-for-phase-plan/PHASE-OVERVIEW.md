# UniLost - Development Phase Plan Overview

## Project Summary

**UniLost** is a centralized Lost and Found platform for the academic community across Cebu City. It connects students, faculty, and campus security from multiple universities (CIT-U, USC, USJ-R, UC, UP Cebu, SWU, CNU, CTU) through a single system for reporting and recovering missing items.

**Tech Stack:** Java 21 (Spring Boot 4.0.2) | MongoDB Atlas | React 19 + Vite 7 (Web) | Kotlin + Jetpack Compose (Mobile)

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
| 1 | [Authentication System](./PHASE-1-AUTHENTICATION.md) | **COMPLETED** | User registration, login, JWT, password reset with OTP |
| 2 | [User & Campus Management](./PHASE-2-USER-CAMPUS-MANAGEMENT.md) | **COMPLETED** | User CRUD, campus CRUD, data seeding, domain validation |
| 3 | [Item Management](./PHASE-3-ITEM-MANAGEMENT.md) | **COMPLETED** | Item posting, search, filters, image upload, soft deletes |
| 4 | [Claim & Verification](./PHASE-4-CLAIM-VERIFICATION.md) | **COMPLETED** | Secret Detail challenge, claim requests, verification flow |
| 5 | [Admin Dashboard](./PHASE-5-ADMIN-DASHBOARD.md) | **COMPLETED** | Campus-scoped moderation, flagging, user management |
| 6 | [Messaging](./PHASE-6-MESSAGING.md) | **COMPLETED** | WebSocket + REST chat, auto-create on claim, real-time messages |
| 7 | [Handover & Karma](./PHASE-7-HANDOVER-KARMA.md) | PENDING | Dual-confirm handover, karma leaderboard |
| 8 | [Campus Maps](./PHASE-8-CAMPUS-MAPS.md) | **COMPLETED** | Google Maps, pin placement, campus filtering |
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

> **Note:** Mobile phases depend on the backend being completed first in each corresponding phase.

---

## Current Project State

### What Is Built
- **Backend:** Auth (JWT HS256 24h, OTP password reset with lockout), User/Campus CRUD with pagination, Item CRUD (Cloudinary images, search, filters, soft delete, GPS coordinates), Claim system (secret detail verification, accept/reject with optimistic locking, auto-reject others), Admin dashboard (campus-scoped moderation, flagging, analytics, super admin cross-campus stats), Messaging (WebSocket STOMP + REST, auto-create on claim, read receipts, authenticated WebSocket), Map items endpoint (`GET /api/items/map`)
- **Website:** All pages wired to real API — auth flow, item feed, item detail, post item (with map pin placement), profile, settings, dashboard, claims (my claims, incoming claims, claim detail), admin (dashboard, items, users, claims, super admin panel), messaging (real-time WebSocket chat with unread badges), interactive Google Maps view with campus filtering
- **Mobile:** Login & Register wired to API, all other screens built with mock data
- **Design System:** Reusable UI components with dark/light theme support via CSS Custom Properties

### Cross-Cutting Concerns Implemented
- **3-tier rate limiting:** Auth 10/min, Write 30/min, Read 60/min per IP with `X-RateLimit-*` headers
- **Entities use Java enums:** `ItemStatus`, `Role`, `AccountStatus`, `ClaimStatus`
- **Batch DTO conversion** in all services prevents N+1 queries
- **JWT client-side expiry check** with auto-logout on malformed tokens
- **Atomic OTP lockout** via `MongoTemplate.findAndModify`
- **MongoDB indexes** for items, users, chats, messages collections
- **WebSocket authentication** via STOMP interceptor (JWT on CONNECT)
- **Frontend constants:** `constants/categories.js`, `constants/roles.js`
- **GlobalExceptionHandler** for centralized `@RestControllerAdvice` error handling

### Environment Variables Required
```
# website/.env
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key
```

### Immediate Next Step
**Phase 7 - Handover & Karma (Backend + Website)**
