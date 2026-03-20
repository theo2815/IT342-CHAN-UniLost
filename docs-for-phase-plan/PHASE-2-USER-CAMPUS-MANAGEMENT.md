# Phase 2 - User & Campus Management (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** Phase 1 (Authentication)

---

## Objective

Build the foundational data layer for users and campuses, including CRUD operations, data seeding for all 8 participating universities, and domain-based campus assignment.

---

## Backend (Spring Boot) — Implemented

| Task | Endpoint / Component | Status |
|------|---------------------|--------|
| List all users (paginated, admin/faculty only) | `GET /api/users` | DONE |
| Get user by ID | `GET /api/users/{id}` | DONE |
| Update user profile | `PUT /api/users/{id}` | DONE |
| Delete user | `DELETE /api/users/{id}` | DONE |
| User service (CRUD + auth helpers) | `UserService.java` | DONE |
| List all campuses | `GET /api/campuses` | DONE |
| Get campus by ID | `GET /api/campuses/{id}` | DONE |
| Create campus | `POST /api/campuses` | DONE |
| Update campus | `PUT /api/campuses/{id}` | DONE |
| Delete campus | `DELETE /api/campuses/{id}` | DONE |
| Campus service with GeoJSON coordinates | `CampusService.java` | DONE |
| Data seeder (8 universities + admin account) | `DataSeeder.java` | DONE |

## Website (React + Vite) — Implemented

| Task | Page / Component | Status |
|------|-----------------|--------|
| Profile page (wired to API) | `Profile/Profile.jsx` | DONE |
| Settings page (wired to API) | `Settings/Settings.jsx` | DONE |
| Campus service API calls | `services/campusService.js` | DONE |
| User service API calls | `services/userService.js` | DONE |

---

## Technical Details

### User Entity Fields (Actual Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (MongoDB ObjectId) | Primary key |
| `fullName` | String | User's full name |
| `email` | String (unique) | University email address |
| `passwordHash` | String | BCrypt hashed (`@JsonIgnore`) |
| `role` | `Role` enum | `STUDENT`, `FACULTY`, `ADMIN` |
| `universityTag` | String | Reference to CampusEntity ID |
| `karmaScore` | Integer | Starts at 0, incremented on handover |
| `accountStatus` | `AccountStatus` enum | `ACTIVE`, `SUSPENDED`, `DEACTIVATED` |
| `lastLogin` | LocalDateTime | Updated on each login |
| `createdAt` | LocalDateTime | Account creation timestamp |
| `passwordResetToken` | String | BCrypt-encoded OTP (`@JsonIgnore`) |
| `passwordResetExpiry` | LocalDateTime | OTP expiry (`@JsonIgnore`) |
| `otpAttempts` | int | Failed OTP attempts counter |
| `otpLockoutUntil` | LocalDateTime | Lockout expiry timestamp |
| `otpVerified` | boolean | Whether OTP was verified for password reset |

### Campus Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (MongoDB ObjectId) | Primary key |
| `name` | String | Full university name |
| `domainWhitelist` | String | Email domain for validation (e.g., `cit.edu`) |
| `centerCoordinates` | GeoJsonPoint | Latitude/Longitude for map |

### Seeded Data (8 Universities)
CIT-U, USC, USJ-R, UC, UP Cebu, SWU, CNU, CTU

### Seeded Admin Account
- **Email:** `admin@cit.edu`
- **Password:** `admin123456`
- **Role:** `ADMIN`
