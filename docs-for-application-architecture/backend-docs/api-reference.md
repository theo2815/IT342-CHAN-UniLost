# Backend API Reference

> Base URL: `/api`

## Auth Endpoints (`/api/auth`)

All public (no token required) except `/me`.

| Method | Path | Request Body | Response | Notes |
|--------|------|-------------|----------|-------|
| POST | `/auth/register` | `RegisterRequest` | `UserDTO` | Validates email domain against campus whitelist |
| POST | `/auth/login` | `LoginRequest` | `AuthResponse` | Returns JWT + user data |
| POST | `/auth/forgot-password` | `{ email }` | `{ message }` | Sends 6-digit OTP via email |
| POST | `/auth/verify-otp` | `{ email, otp }` | `{ message }` | Validates OTP (10-min expiry) |
| POST | `/auth/reset-password` | `{ email, otp, newPassword }` | `{ message }` | Resets password, clears OTP |
| GET | `/auth/me` | — | `UserDTO` | **Requires auth** |

### Request/Response Shapes

```
RegisterRequest { fullName: String, email: String, password: String }
LoginRequest    { email: String, password: String }
AuthResponse    { token: String, type: "Bearer", user: UserDTO }
```

## User Endpoints (`/api/users`)

| Method | Path | Response | Auth |
|--------|------|----------|------|
| GET | `/users` | `List<UserDTO>` | ADMIN or FACULTY |
| GET | `/users/{id}` | `UserDTO` | Authenticated |
| PUT | `/users/{id}` | `UserDTO` | Authenticated (own profile) |
| DELETE | `/users/{id}` | 204 No Content | Authenticated (own profile) |

### UserDTO Shape

```
UserDTO {
  id: String
  email: String
  fullName: String
  universityTag: String        // Campus ID reference
  karmaScore: int
  role: String                 // STUDENT | FACULTY | ADMIN
  emailVerified: boolean
  accountStatus: String        // ACTIVE | SUSPENDED | DEACTIVATED
  createdAt: LocalDateTime
  campus: CampusDTO            // Resolved campus object
}
```

## Campus Endpoints (`/api/campuses`)

| Method | Path | Response | Auth |
|--------|------|----------|------|
| GET | `/campuses` | `List<CampusDTO>` | Public |
| GET | `/campuses/{id}` | `CampusDTO` | Public |
| GET | `/campuses/domain/{domain}` | `CampusDTO` | Public |
| POST | `/campuses` | `CampusDTO` | ADMIN |
| PUT | `/campuses/{id}` | `CampusDTO` | ADMIN |
| DELETE | `/campuses/{id}` | 204 No Content | ADMIN |

### CampusDTO Shape

```
CampusDTO {
  id: String
  name: String
  domainWhitelist: String        // e.g., "cit.edu"
  centerCoordinates: double[2]   // [longitude, latitude]
}
```

## Planned Endpoints (Not Yet Implemented)

### Phase 4 — Items (`/api/items`)
- CRUD for lost/found items
- GeoJSON queries for nearby items
- Full-text search
- Cloud image storage (blurred + original)

### Phase 5 — Claims (`/api/claims`)
- Submit claim with secret answer
- Accept/reject claims
- Pending claims count

### Phase 6 — Chat (`/api/chats`, `/api/messages`)
- Create chat between finder and verified claimant
- WebSocket real-time messaging
- Message read receipts

### Phase 7 — Handover (`/api/handovers`)
- Create handover from accepted claim
- Dual confirmation (finder + owner)
- Karma score updates on completion

### Phase 8 — Notifications (`/api/notifications`)
- User notification feed
- Mark read / mark all read
- Push notification integration

## Error Response Pattern

Controllers return `ResponseEntity` with appropriate HTTP status:

```json
// Error (string body)
Status: 400/401/403/404/409
Body: "Error message describing what went wrong"

// Success (object body)
Status: 200/201
Body: { ...DTO fields }
```

## Authentication Header

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Required for all endpoints except:
- All `/api/auth/**` (except `/auth/me`)
- GET `/api/campuses` and `/api/campuses/**`
