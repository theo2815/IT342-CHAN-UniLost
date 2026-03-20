# Phase 2 - User & Campus Management (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** Phase 1 (Authentication)

---

## Objective

Build the foundational data layer for users and campuses, including CRUD operations, data seeding for all 8 participating universities, and domain-based campus assignment.

---

## Backend (Spring Boot)

| Task | Endpoint / Component | Status |
|------|---------------------|--------|
| List all users | `GET /api/users` | DONE |
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

## Website (React + Vite)

| Task | Page / Component | Status |
|------|-----------------|--------|
| Profile page | `Profile/Profile.jsx` | DONE (mock data) |
| Settings page | `Settings/Settings.jsx` | DONE (mock data) |
| Campus service API calls | `services/campusService.js` | DONE |

---

## Technical Details

### User Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (MongoDB ObjectId) | Primary key |
| `fullName` | String | User's full name |
| `email` | String (unique) | University email address |
| `password` | String | BCrypt hashed |
| `studentId` | String | Student/Faculty ID number |
| `role` | Enum | `STUDENT`, `FACULTY`, `ADMIN` |
| `campusId` | String | Reference to CampusEntity |
| `karmaPoints` | Integer | Starts at 0, incremented on handover |
| `status` | Enum | `ACTIVE`, `SUSPENDED`, `DEACTIVATED` |
| `lastLogin` | LocalDateTime | Updated on each login |
| `createdAt` | LocalDateTime | Account creation timestamp |

### Campus Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (MongoDB ObjectId) | Primary key |
| `name` | String | Full university name |
| `shortName` | String | Abbreviation (e.g., CIT-U) |
| `emailDomain` | String | Domain for validation (e.g., `cit.edu`) |
| `address` | String | Physical address |
| `coordinates` | GeoJsonPoint | Latitude/Longitude for map |

### Seeded Data (8 Universities)
1. Cebu Institute of Technology - University (CIT-U)
2. University of San Carlos (USC)
3. University of San Jose - Recoletos (USJ-R)
4. University of Cebu (UC)
5. University of the Philippines Cebu (UP Cebu)
6. Southwestern University (SWU)
7. Cebu Normal University (CNU)
8. Cebu Technological University (CTU)

### Seeded Admin Account
- **Email:** `admin@cit.edu`
- **Password:** `admin123456`
- **Role:** `ADMIN`

---

## Outstanding Items (Deferred)

| Item | Deferred To |
|------|------------|
| Wire Profile page to real API (replace mock data) | Phase 3 |
| Wire Settings page to real API | Phase 3 |
