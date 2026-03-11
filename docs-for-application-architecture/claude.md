# UniLost — AI Agent Architecture Reference

> **Project State**: `DEVELOPMENT` — Phase 1–3 backend complete; all frontend UI built with mock data. See [Phase Plan](#development-phase-roadmap) below.
>
> **Purpose**: Root entry point for AI coding agents. Read this file first, then navigate to subdirectory docs as needed. Optimized for minimal token consumption.

## Project Overview

**UniLost** is a lost-and-found platform for universities in Cebu, Philippines. Users report lost/found items, submit ownership claims with secret verification, and coordinate handovers — all scoped to their campus via email domain whitelisting.

## Monorepo Structure

```
IT342-CHAN-UniLost/
├── backend/          → Spring Boot 4.0.2 REST API (Java 21, MongoDB)
├── mobile/           → Android app (Kotlin, Jetpack Compose, Hilt)
├── website/          → React SPA (React 19, Vite 7, Axios)
├── docs/             → Project planning & task checklists
└── docs-for-application-architecture/
    ├── claude.md              ← YOU ARE HERE
    ├── backend-docs/          → API, entities, security, services
    ├── mobile-docs/           → Android architecture, screens, DI
    └── website-docs/          → React architecture, components, design system
```

## Tech Stack Summary

| Layer | Technology | Key Details |
|-------|-----------|-------------|
| **Backend** | Spring Boot 4.0.2, Java 21 | MongoDB Atlas, JWT (HS256, 24h), BCrypt, Gmail SMTP |
| **Mobile** | Android SDK 34, Kotlin 1.9.10 | Jetpack Compose, Hilt 2.48, Retrofit 2.9.0, Room 2.5.2, MVVM |
| **Website** | React 19.2.0, Vite 7.3.1 | React Router 7.13, Axios 1.13.4, Lucide React, CSS Custom Properties |

## Core Domain Model

```
User → posts → Item (LOST | FOUND)
User → submits → Claim → on Item
Claim (ACCEPTED) → creates → Chat → contains → Messages
Claim (ACCEPTED) → creates → Handover (finder + owner confirm)
Handover (both confirmed) → completes → Item recovered
System → sends → Notification → to User
User → belongs to → Campus (validated via email domain)
```

## Roles & Permissions

| Role | Capabilities |
|------|-------------|
| **STUDENT** | Post items, submit claims, chat, view own profile |
| **FACULTY** | All student capabilities + cross-campus admin panel |
| **ADMIN** | All faculty capabilities + campus CRUD, user management |

## Shared Constants

**8 Campuses** (seeded on backend startup):
CIT-U, USC, USJ-R, UC, UP Cebu, SWU, CNU, CTU

**Item Categories** (9 in frontend mock data, 8 in backend entity):
Electronics, Documents, Clothing, Accessories, Books, Bags, Keys, Wallets, Other

> **Note**: Backend `ItemEntity` defines 8 categories (no WALLETS). Frontend mock data includes Wallets as a 9th category. Align when implementing Phase 4 Items CRUD.

**Item Types**: LOST, FOUND

**Item Statuses**: ACTIVE, CLAIMED, HANDED_OVER, EXPIRED, CANCELLED

**Claim Statuses**: PENDING, ACCEPTED, REJECTED

> **Note**: Backend `ClaimEntity` uses ACCEPTED/REJECTED. Frontend mock data (both website and mobile) uses APPROVED/REJECTED. Align naming when implementing Phase 5 Claims CRUD.

## Authentication Flow (All Clients)

1. User registers with university email → backend validates domain against campus whitelist
2. Login → POST `/api/auth/login` → returns `{ token, type: "Bearer", user: UserDTO }`
3. Token stored: `localStorage` (web) / `DataStore` (mobile)
4. All subsequent requests include `Authorization: Bearer {token}` header
5. Backend `JwtAuthenticationFilter` validates token, sets SecurityContext
6. 401 response → client clears auth state and redirects to login

## Password Reset Flow

1. POST `/api/auth/forgot-password` with email → 6-digit OTP sent via Gmail SMTP
2. POST `/api/auth/verify-otp` with email + OTP → validates (10-min expiry)
3. POST `/api/auth/reset-password` with email + OTP + newPassword → updates password

## API Base URLs

| Client | Base URL |
|--------|----------|
| Website (dev) | `http://localhost:8080/api` |
| Mobile (emulator) | `http://10.0.2.2:8080/api` |

## Development Phase Roadmap

### Phase Summary

| Phase | Feature | Backend | Website | Mobile |
|-------|---------|---------|---------|--------|
| 1-3 | Auth, Users, Campuses | ✅ Done | ✅ Done | ✅ Done |
| 4 | Items CRUD, Images, Geospatial | 🔲 | 🔲 (UI ready, mock data) | 🔲 (UI ready, mock data) |
| 5 | Claims, Verification | 🔲 | 🔲 (UI ready, mock data) | 🔲 (UI ready, mock data) |
| 6 | Chat, WebSocket messaging | 🔲 | 🔲 (UI ready, mock data) | 🔲 (UI ready, mock data) |
| 7 | Handover, Karma scoring | 🔲 | 🔲 (UI ready, mock data) | 🔲 (UI ready, mock data) |
| 8 | Notifications, Push | 🔲 | 🔲 (UI ready, mock data) | 🔲 (UI ready, mock data) |

### Phase 4 — Items (Next Priority)

**Backend**:
- [ ] `ItemController` + `ItemService` — CRUD endpoints at `/api/items`
- [ ] Add `WALLETS` to `ItemEntity` category enum (align with frontend mock data's 9 categories)
- [ ] Cloud image storage (blurred + original URLs)
- [ ] GeoJSON spatial queries (`ItemRepository`)
- [ ] Full-text search on title/description
- [ ] SecurityConfig: add item endpoint authorization rules

**Website**:
- [ ] Replace mock data in `ItemFeed`, `ItemDetail`, `PostItem` with API calls
- [ ] `itemService.js` — new service file for item CRUD
- [ ] Image upload integration in `PostItem` (currently uses `ImageUpload` component with mock)

**Mobile**:
- [ ] Add item endpoints to `AppApi.kt` (currently template-only)
- [ ] Replace `MockItems.kt` data with API integration
- [ ] `ItemRepository.kt` — new repository for item operations
- [ ] Image upload integration

### Phase 5 — Claims

**Backend**:
- [ ] `ClaimController` + `ClaimService` — CRUD at `/api/claims`
- [ ] Secret detail answer verification logic
- [ ] Pending claims count per item
- [ ] SecurityConfig: add claim endpoint rules

**Website**:
- [ ] Wire `ClaimModal` to API (currently mock)
- [ ] Wire `IncomingClaims`, `ClaimDetail` pages to API
- [ ] `claimService.js` — new service file

**Mobile**:
- [ ] Replace `MockClaims.kt` with API integration
- [ ] Wire `MyClaimsScreen`, `ClaimDetailScreen` to API

### Phase 6 — Chat & Messaging

**Backend**:
- [ ] `ChatController` + `ChatService`, `MessageController` + `MessageService`
- [ ] WebSocket configuration (Spring WebSocket + STOMP)
- [ ] SecurityConfig: WebSocket security, chat endpoint rules
- [ ] Message read receipts, pagination

**Website**:
- [ ] Wire `Messages` page to WebSocket
- [ ] Real-time message delivery

**Mobile**:
- [ ] WebSocket client integration
- [ ] Chat screen API connectivity

### Phase 7 — Handover & Karma

**Backend**:
- [ ] `HandoverController` + `HandoverService`
- [ ] Dual confirmation logic (finder + owner)
- [ ] Karma score increment on completion
- [ ] SecurityConfig: handover endpoint rules

**Website / Mobile**:
- [ ] Wire handover UI to API
- [ ] Karma display updates

### Phase 8 — Notifications & Push

**Backend**:
- [ ] `NotificationController` + `NotificationService`
- [ ] Push notification integration (FCM for mobile)
- [ ] Batch mark-as-read
- [ ] Rate limiting for auth endpoints
- [ ] Auto-cleanup for old notifications

**Website**:
- [ ] Wire `Notifications` page and `NotificationDropdown` to API

**Mobile**:
- [ ] Firebase Cloud Messaging (FCM) integration
- [ ] Wire `NotificationsScreen` to API

### Cross-Platform Gaps to Address

- **School vs Campus naming**: Mobile uses `School` model (`schoolId, name, shortName, city, emailDomain`); backend uses `CampusEntity` (`id, name, domainWhitelist, centerCoordinates`). Mobile `AuthApiService` calls `GET /schools` but backend serves `GET /api/campuses`. Needs alignment.
- **`schoolService.js` (website)**: Calls `/schools` endpoints — backend has no `/api/schools` route. Should use `campusService.js` instead, or create a backend alias.
- **JWT refresh token rotation**: Not yet implemented (planned Phase 3+).
- **`GlobalExceptionHandler`**: Backend has none — controllers use inline try-catch. Should centralize.
- **`UserProbe.java`**: Deactivated diagnostic tool in backend root package (`@Component` commented out). Safe to remove or keep for debugging.

## Documentation Map

| Need | Read |
|------|------|
| Backend API endpoints | [backend-docs/api-reference.md](backend-docs/api-reference.md) |
| Database entities & schema | [backend-docs/database-schema.md](backend-docs/database-schema.md) |
| Backend architecture & security | [backend-docs/architecture.md](backend-docs/architecture.md) |
| Android app structure | [mobile-docs/architecture.md](mobile-docs/architecture.md) |
| Android screens & navigation | [mobile-docs/screens-and-navigation.md](mobile-docs/screens-and-navigation.md) |
| React app structure | [website-docs/architecture.md](website-docs/architecture.md) |
| React components & design system | [website-docs/components.md](website-docs/components.md) |
| React pages & routing | [website-docs/pages-and-routing.md](website-docs/pages-and-routing.md) |
| CSS design system & theming | [website-docs/design-system.md](website-docs/design-system.md) |
