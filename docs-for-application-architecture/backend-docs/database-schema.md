# Backend Database Schema

> MongoDB Atlas — all collections use String `@Id` fields (MongoDB ObjectId or custom IDs)

## Entity Relationship Diagram

```
CampusEntity ◄──── UserEntity (universityTag → campus.id)
                        │
                        ├── posts ──► ItemEntity (reporterId → user.id)
                        │                  │
                        │                  ├── has ──► ClaimEntity (itemId → item.id, claimantId → user.id)
                        │                  │               │
                        │                  │               ├── creates ──► ChatEntity (itemId, finderId, ownerId)
                        │                  │               │                    │
                        │                  │               │                    └── contains ──► MessageEntity (chatId, senderId)
                        │                  │               │
                        │                  │               └── creates ──► HandoverEntity (claimId, itemId)
                        │                  │
                        │                  └── triggers ──► NotificationEntity (userId)
                        │
                        └── receives ──► NotificationEntity
```

## Collections

### `users`

| Field | Type | Constraints | Notes |
|-------|------|------------|-------|
| id | String | @Id | MongoDB ObjectId |
| email | String | @Indexed(unique=true) | University email |
| passwordHash | String | — | BCrypt encoded |
| fullName | String | — | |
| universityTag | String | — | Foreign key → campuses.id |
| karmaScore | int | — | Default: 0 |
| role | String | — | `STUDENT` \| `FACULTY` \| `ADMIN` (default: STUDENT) |
| emailVerified | boolean | — | Default: false |
| accountStatus | String | — | `ACTIVE` \| `SUSPENDED` \| `DEACTIVATED` (default: ACTIVE) |
| createdAt | LocalDateTime | — | |
| lastLogin | LocalDateTime | — | |
| passwordResetToken | String | — | BCrypt-encoded OTP (nullable) |
| passwordResetExpiry | LocalDateTime | — | OTP expiration (nullable) |

**Repository Queries**: `findByEmail`, `existsByEmail`, `findByUniversityTag`, `findByPasswordResetToken`

### `campuses`

| Field | Type | Notes |
|-------|------|-------|
| id | String | Custom ID (e.g., "CIT-U-MAIN") |
| name | String | Full university name |
| domainWhitelist | String | Email domain (e.g., "cit.edu") |
| centerCoordinates | GeoJsonPoint | [longitude, latitude] |

**Repository Queries**: `findByDomainWhitelist`, `findByName`

### `items`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| reporterId | String | → users.id |
| campusId | String | → campuses.id |
| title | String | |
| type | String | `LOST` \| `FOUND` |
| category | String | `ELECTRONICS` \| `CLOTHING` \| `DOCUMENTS` \| `ACCESSORIES` \| `BOOKS` \| `KEYS` \| `BAGS` \| `OTHER` |
| location | GeoJsonPoint | [longitude, latitude] |
| blurredImageUrl | String | Shown to unverified users |
| originalImageUrl | String | Shown after claim verification |
| secretDetailQuestion | String | Verification question for claimants |
| description | String | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |
| isDeleted | boolean | Soft delete (default: false) |
| deletedAt | LocalDateTime | Nullable |

**Repository Queries**: `findByReporterId`, `findByCampusId`, `findByType`, `findByIsDeletedFalse`, `findByCampusIdAndIsDeletedFalse`

### `claims`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| itemId | String | → items.id |
| claimantId | String | → users.id |
| providedAnswer | String | Answer to item's secretDetailQuestion |
| status | String | `PENDING` \| `ACCEPTED` \| `REJECTED` (default: PENDING) |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

**Repository Queries**: `findByItemId`, `findByClaimantId`, `findByItemIdAndStatus`

### `chats`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| itemId | String | → items.id |
| finderId | String | → users.id (item reporter) |
| ownerId | String | → users.id (verified claimant) |
| lastMessagePreview | String | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

**Repository Queries**: `findByItemId`, `findByFinderIdOrOwnerId`, `findByItemIdAndFinderIdAndOwnerId`

### `messages`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| chatId | String | → chats.id |
| senderId | String | → users.id |
| content | String | |
| isRead | boolean | Default: false |
| createdAt | LocalDateTime | |

**Repository Queries**: `findByChatIdOrderByCreatedAtAsc`, `findByChatIdAndIsReadFalse`, `countByChatIdAndIsReadFalseAndSenderIdNot`

### `handovers`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| claimId | String | → claims.id |
| itemId | String | → items.id |
| finderConfirmed | boolean | Default: false |
| ownerConfirmed | boolean | Default: false |
| completedAt | LocalDateTime | Set when both confirmed (nullable) |
| createdAt | LocalDateTime | |

**Repository Queries**: `findByClaimId`, `findByItemId`

### `notifications`

| Field | Type | Notes |
|-------|------|-------|
| id | String | @Id |
| userId | String | → users.id (recipient) |
| type | String | `MESSAGE` \| `CLAIM_UPDATE` \| `HANDOVER_SUCCESS` |
| content | String | |
| linkId | String | Reference to related entity |
| isRead | boolean | Default: false |
| createdAt | LocalDateTime | |

**Repository Queries**: `findByUserIdOrderByCreatedAtDesc`, `findByUserIdAndIsReadFalse`, `countByUserIdAndIsReadFalse`

## Service Layer Summary

### UserService
- `createUser(RegisterRequest)` → validates email domain, hashes password, assigns campus
- `authenticate(email, password)` → BCrypt verify, check account status
- `requestPasswordReset(email)` → generates 6-digit OTP, BCrypt-encodes it, saves with 10-min expiry, sends email
- `verifyResetOtp(email, otp)` → BCrypt-matches OTP, checks expiry
- `resetPassword(email, otp, newPassword)` → verifies OTP, updates password hash, clears reset tokens
- `convertToDTO(entity)` → resolves campus reference to nested CampusDTO

### CampusService
- Full CRUD with `GeoJsonPoint` coordinates from `double[]` array
- `getCampusByDomain(domain)` → used during registration for email validation

### EmailService
- `sendPasswordResetOtp(email, otp)` → HTML email via Gmail SMTP with `MimeMessageHelper`
