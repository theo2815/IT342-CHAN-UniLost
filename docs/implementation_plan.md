# UniLost – Implementation Plan

## Cebu City Academic Lost & Found Network

---

## Current State Assessment

### What Already Exists

| Layer | Status | Details |
|-------|--------|---------|
| **Spring Boot Backend** | Partial | Auth (register/login), User CRUD, School CRUD, JWT filter chain, MongoDB Atlas connection |
| **React Frontend** | Partial | Login, Register, Dashboard (placeholder), Profile, Settings (theme toggle), Protected routes, Axios interceptors |
| **Kotlin Mobile** | Partial | Login, Register, Dashboard (placeholder), Hilt DI, Retrofit + AuthInterceptor, DataStore token storage |
| **MongoDB** | Partial | `users` and `schools` collections only |
| **Security** | Scaffolded | JWT works but secret is hardcoded, user endpoints are publicly accessible, no role-based access |

### What Needs to Change Before Building UniLost Features

The project was originally scaffolded as "HulamPay" (a marketplace). The core auth infrastructure is reusable, but:

1. **Rename** package references, database name, and branding from HulamPay to UniLost
2. **Add role-based access** (STUDENT, ADMIN, SUPER_ADMIN) to UserEntity
3. **Enforce university email domain validation** during registration
4. **Secure all endpoints** that are currently public for development
5. **Move JWT secret** to environment variables

---

## Data Model Design

### MongoDB Collections

#### `users` (modify existing)
```json
{
  "_id": "ObjectId",
  "firstName": "String",
  "lastName": "String",
  "email": "String (unique, must match school emailDomain)",
  "password": "String (BCrypt)",
  "phoneNumber": "String",
  "profilePicture": "String (URL/path)",
  "studentIdNumber": "String (unique)",
  "school": "DBRef → schools",
  "role": "String (STUDENT | ADMIN | SUPER_ADMIN)",
  "karmaScore": "Integer (default: 0)",
  "totalItemsReturned": "Integer (default: 0)",
  "totalItemsClaimed": "Integer (default: 0)",
  "isVerified": "Boolean (default: false)",
  "isBanned": "Boolean (default: false)",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

#### `schools` (modify existing)
```json
{
  "_id": "ObjectId",
  "name": "String (e.g., Cebu Institute of Technology - University)",
  "shortName": "String (e.g., CIT-U)",
  "city": "String (Cebu City)",
  "emailDomain": "String (e.g., cit.edu)",
  "address": "String",
  "latitude": "Double",
  "longitude": "Double",
  "campusBoundary": "GeoJSON Polygon",
  "isActive": "Boolean (default: true)",
  "createdAt": "LocalDateTime"
}
```

#### `items` (new)
```json
{
  "_id": "ObjectId",
  "type": "String (LOST | FOUND)",
  "title": "String",
  "description": "String",
  "category": "String (ELECTRONICS | DOCUMENTS | CLOTHING | ACCESSORIES | BOOKS | BAGS | KEYS | WALLET | OTHER)",
  "images": ["String (URLs — blurred for FOUND items in public view)"],
  "originalImages": ["String (URLs — unblurred, only visible to poster and verified claimant)"],
  "secretDetail": "String (encrypted — only the poster knows this)",
  "locationFound": "String (freetext description)",
  "locationCoordinates": { "type": "Point", "coordinates": [lng, lat] },
  "dateLostOrFound": "LocalDateTime",
  "status": "String (ACTIVE | CLAIMED | MATCHED | HANDED_OVER | EXPIRED | CANCELLED)",
  "postedBy": "DBRef → users",
  "school": "DBRef → schools",
  "isAnonymous": "Boolean (default: false)",
  "expiresAt": "LocalDateTime (auto-set, e.g., 30 days after posting)",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

#### `claims` (new)
```json
{
  "_id": "ObjectId",
  "item": "DBRef → items",
  "claimant": "DBRef → users (the person claiming ownership)",
  "itemPoster": "DBRef → users (the person who posted the item)",
  "secretDetailAttempt": "String (what the claimant submitted as verification)",
  "secretDetailMatch": "Boolean (set by poster after review)",
  "claimMessage": "String (optional message from claimant)",
  "status": "String (PENDING | APPROVED | REJECTED | HANDED_OVER | CANCELLED)",
  "posterConfirmedHandover": "Boolean (default: false)",
  "claimantConfirmedHandover": "Boolean (default: false)",
  "adminOverrideHandover": "Boolean (default: false)",
  "adminNotes": "String (optional)",
  "reviewedBy": "DBRef → users (admin who reviewed, if applicable)",
  "handoverLocation": "String",
  "handoverDate": "LocalDateTime",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

#### `notifications` (new)
```json
{
  "_id": "ObjectId",
  "recipient": "DBRef → users",
  "type": "String (CLAIM_RECEIVED | CLAIM_APPROVED | CLAIM_REJECTED | HANDOVER_CONFIRMED | ITEM_EXPIRED | ADMIN_ACTION | KARMA_UPDATE)",
  "title": "String",
  "message": "String",
  "relatedItem": "DBRef → items (optional)",
  "relatedClaim": "DBRef → claims (optional)",
  "isRead": "Boolean (default: false)",
  "createdAt": "LocalDateTime"
}
```

#### `admin_actions` (new)
```json
{
  "_id": "ObjectId",
  "admin": "DBRef → users",
  "school": "DBRef → schools",
  "actionType": "String (ITEM_REMOVED | USER_BANNED | USER_UNBANNED | CLAIM_OVERRIDDEN | HANDOVER_FORCED | ITEM_EDITED)",
  "targetUser": "DBRef → users (optional)",
  "targetItem": "DBRef → items (optional)",
  "targetClaim": "DBRef → claims (optional)",
  "reason": "String",
  "createdAt": "LocalDateTime"
}
```

---

## Phase 1: Rebrand, Harden Auth & Add Roles ✅ COMPLETED

### Status: COMPLETED

### What Was Implemented

#### Backend Changes
- **Rebranded** database name from `hulampay` to `unilost` in `MongoConfig.java`
- **Updated** `application.properties` with `spring.application.name=unilost-backend`, JWT env properties
- **Updated** `pom.xml` display name to `unilost-backend`
- **Updated** `.env` with `MONGODB_DATABASE=unilost`, `JWT_SECRET`, `JWT_EXPIRATION`
- **Added `role` field** to `UserEntity` (STUDENT/ADMIN/SUPER_ADMIN), plus `karmaScore`, `totalItemsReturned`, `totalItemsClaimed`, `isVerified`, `isBanned`, `updatedAt`
- **Added `shortName`** and `isActive` to `SchoolEntity`
- **Updated DTOs**: `UserDTO` (role, karmaScore, isVerified, isBanned), `SchoolDTO` (shortName)
- **Hardened JWT**: `JwtUtils` now reads secret from `@Value("${jwt.secret}")`, embeds role in claims, added `getRoleFromToken()`
- **Updated `JwtAuthenticationFilter`**: extracts role from JWT, sets `ROLE_` prefixed `SimpleGrantedAuthority`
- **Updated `SecurityConfig`**: `@EnableMethodSecurity`, RBAC matchers (ADMIN/SUPER_ADMIN for admin endpoints, SUPER_ADMIN for school creation, authenticated for all others)
- **Updated `UserService`**: email domain validation on registration, auto school assignment via `SchoolRepository.findByEmailDomain()`, banned user check on login
- **Updated `AuthController`**: `/me` endpoint looks up user by email from SecurityContext, passes role to `generateToken()`
- **Updated `UserController`**: removed public `createUser`, added ownership checks for PUT/DELETE
- **Updated `SchoolController`**: removed `@CrossOrigin` annotation
- **Updated `SchoolService`**: filters by `isActive`, supports `shortName`
- **Created `DataSeeder`**: seeds 8 Cebu City universities (CIT-U, USC, USJ-R, UC, UP Cebu, SWU, CNU, CTU) + SUPER_ADMIN account (`admin@unilost.com` / `admin123456`)

#### Frontend (React) Changes
- **Rebranded** `index.html` title to "UniLost - Campus Lost & Found"
- **Updated `authService.js`**: removed schoolId from register, added `getUserRole()`, `isAdmin()`, `isSuperAdmin()`
- **Updated `Header.jsx`**: UniLost branding with Search icon, role badge display, conditional Admin Panel link
- **Updated `Header.css`**: added `.dropdown-role` styles
- **Updated `Login.jsx`**: UniLost branding, "Find What's Lost. Across Campus." tagline, multi-campus features
- **Updated `Register.jsx`**: removed school dropdown, added email domain auto-detection with real-time school name display, supported domains hint
- **Updated `Register.css`**: added `.school-detected`, `.email-hint`, `.required` styles
- **Updated `Dashboard.jsx`**: replaced marketplace stats (Revenue, Orders) with lost & found stats (Items Reported, Items Recovered, Pending Claims, Active Listings)
- **Updated `Profile.jsx`**: replaced "My Listings"/"Rentals"/"Post Item" with "My Reports"/"Claims"/"Recovered"/"Report Item", added role badge and karma score display
- **Updated `Settings.jsx`**: replaced "Hulampay" with "UniLost" in theme preferences text

#### Mobile (Kotlin) Changes
- **Updated `strings.xml`**: app name from "My Application" to "UniLost"
- **Updated `User.kt`**: added `role`, `karmaScore`, `isVerified`, `isBanned` fields
- **Updated `School.kt`**: added `shortName` field
- **Updated `TokenManager.kt`**: added `ROLE_KEY` for storing user role in DataStore, added `saveRole()` and `role` Flow
- **Updated `LoginScreen.kt`**: rebranded "HulamPay" to "UniLost", changed "Your campus marketplace awaits" to "Your campus lost & found network"
- **Updated `RegisterScreen.kt`**: removed school dropdown, added email domain auto-detection with real-time school display, rebranded to "UniLost" / "Join the Cebu City campus lost & found network"
- **Updated `RegisterViewModel.kt`**: removed `schoolId` parameter (backend auto-detects from email domain), relaxed validation for optional fields
- **Updated `AuthRepository.kt`**: saves user role via `tokenManager.saveRole()` on login
- **Updated `DashboardScreen.kt`**: rebranded to "Welcome to UniLost"

### Notes
- Java package name kept as `com.hulampay` to avoid breaking the entire package structure; only display names, database name, and branding text were changed
- Token refresh endpoint (`POST /api/auth/refresh`) deferred to Phase 6 for security hardening
- Rate limiting deferred to Phase 6

---

## Phase 2: Lost & Found Item Lifecycle

### Objectives
- Users can post lost items and found items
- Found item images are blurred in public listings
- Items are searchable across campuses with filters
- Items auto-expire after 30 days

### Backend Tasks

1. **Item Entity & Repository**
   - Create `ItemEntity` with all fields from data model above
   - Create `ItemRepository` extending `MongoRepository` with:
     - `findBySchool()` — campus-scoped queries
     - `findByType()` — filter LOST vs FOUND
     - `findByCategory()`
     - `findByStatus()`
     - `findByPostedBy()`
     - Full-text search index on `title` and `description`
   - Create `ItemDTO` and `CreateItemDTO`

2. **Item Service**
   - `createItem()` — validate fields, set status to ACTIVE, set expiresAt to +30 days
   - `getItems()` — paginated, filterable (type, category, school, status, keyword)
   - `getItemById()` — return full item; blur image URLs if the requester is not the poster
   - `getItemsByUser()` — user's own postings
   - `updateItem()` — only by poster, only if status is ACTIVE
   - `cancelItem()` — soft-cancel by poster
   - `expireItems()` — scheduled task to mark ACTIVE items past expiresAt as EXPIRED

3. **Item Controller** (`/api/items`)
   - `POST /api/items` — create new item (authenticated)
   - `GET /api/items` — list items with query params (type, category, school, keyword, page, size)
   - `GET /api/items/{id}` — single item detail
   - `GET /api/items/my` — current user's items
   - `PUT /api/items/{id}` — update own item
   - `DELETE /api/items/{id}` — cancel/remove own item

4. **Image Handling**
   - Create image upload endpoint: `POST /api/items/{id}/images`
   - Store images on local filesystem or a configurable path (keep it simple, no cloud for now)
   - For FOUND items: generate a blurred version server-side using Java `BufferedImage` + Gaussian blur
   - Serve blurred images at `/api/images/blurred/{filename}`
   - Serve original images at `/api/images/original/{filename}` (only accessible to poster and approved claimant)

5. **Scheduled Expiration**
   - Use `@Scheduled` annotation to run daily
   - Query items where `status = ACTIVE AND expiresAt < now()`
   - Update status to EXPIRED
   - Create notification for the poster

### Frontend Tasks

1. **Item Posting Form**
   - Page: `/post-item`
   - Fields: type (Lost/Found toggle), title, description, category dropdown, date, location (text), image upload (max 3)
   - For FOUND items: show "Secret Detail" field with helper text: "Enter a detail only the true owner would know (e.g., sticker on the back, scratch on the left side)"
   - Preview before submission

2. **Item Feed / Search Page**
   - Page: `/items` (main feed after login, replaces placeholder dashboard)
   - Card grid layout showing: blurred image (if FOUND), title, category badge, school badge, date, status
   - Filter bar: type (Lost/Found/All), category, school, keyword search
   - Pagination or infinite scroll
   - Click card → detail page

3. **Item Detail Page**
   - Page: `/items/{id}`
   - Full description, location, date, poster info (name, school — no contact yet)
   - If FOUND: blurred image shown; "I think this is mine" button → opens claim flow
   - If LOST: "I found this" button → opens claim flow
   - If own item: edit/cancel buttons

4. **My Items Page**
   - Page: `/my-items`
   - Tabs: Active, Claimed, Handed Over, Expired, Cancelled
   - Quick stats at top (counts per status)

### Mobile Tasks

1. **Item Feed Screen**
   - Replace placeholder dashboard with scrollable item feed
   - Pull-to-refresh, lazy loading
   - Filter chips: type, category
   - Search bar

2. **Item Detail Screen**
   - Display full item info with blurred/original images
   - Action buttons based on context (claim / edit / cancel)

3. **Post Item Screen**
   - Form with image picker from gallery/camera
   - Category spinner, date picker, location text input
   - Secret detail field for FOUND items

4. **My Items Screen**
   - List of user's posted items with status indicators

### Security Considerations
- Validate that only the item poster can update/cancel their own items
- Blurred images must be served for FOUND items to all users except the poster
- Secret detail must never be exposed in any API response except to the poster
- Sanitize all text inputs to prevent injection
- Limit image upload size (e.g., 5MB per image, 3 images max)

---

## Phase 3: Claim & Verification Workflow

### Objectives
- Users can submit claims on items
- Secret detail verification is handled manually by the item poster
- Dual-confirmation handover (both parties confirm)
- Full claim lifecycle tracking

### Backend Tasks

1. **Claim Entity & Repository**
   - Create `ClaimEntity` with all fields from data model
   - Create `ClaimRepository` with:
     - `findByItem()`
     - `findByClaimant()`
     - `findByItemPoster()`
     - `findByStatus()`
     - `findByItemAndClaimant()` — prevent duplicate claims

2. **Claim Service**
   - `createClaim(itemId, claimantId, secretDetailAttempt, message)`
     - Validate: item exists, is ACTIVE, claimant is not the poster
     - Prevent duplicate claims from same user on same item
     - Set status to PENDING
     - Create notification for item poster
   - `getClaim(claimId)` — accessible to claimant, poster, and campus admin
   - `getClaimsForItem(itemId)` — only accessible to poster and admin
   - `getClaimsForUser(userId)` — user's own claims
   - `reviewClaim(claimId, posterId, approved, secretDetailMatch)`
     - Poster approves or rejects the claim
     - If approved: set claim status to APPROVED, update item status to MATCHED
     - If rejected: set claim status to REJECTED
     - Create notification for claimant
   - `confirmHandover(claimId, userId)`
     - If poster confirms: set `posterConfirmedHandover = true`
     - If claimant confirms: set `claimantConfirmedHandover = true`
     - If both confirmed: set claim status to HANDED_OVER, item status to HANDED_OVER
     - Create notifications and update karma scores
   - `cancelClaim(claimId, userId)` — only by claimant, only if PENDING

3. **Claim Controller** (`/api/claims`)
   - `POST /api/claims` — submit a claim
   - `GET /api/claims/{id}` — get claim detail
   - `GET /api/claims/item/{itemId}` — all claims for an item (poster/admin only)
   - `GET /api/claims/my` — current user's submitted claims
   - `PUT /api/claims/{id}/review` — approve or reject (poster only)
   - `PUT /api/claims/{id}/confirm-handover` — confirm handover (poster or claimant)
   - `DELETE /api/claims/{id}` — cancel claim (claimant only)

4. **Notification Service** (foundation)
   - Create `NotificationEntity` and `NotificationRepository`
   - `createNotification(recipientId, type, title, message, relatedItemId, relatedClaimId)`
   - `getNotifications(userId, unreadOnly)` — paginated
   - `markAsRead(notificationId)`
   - `markAllAsRead(userId)`
   - `getUnreadCount(userId)`

5. **Notification Controller** (`/api/notifications`)
   - `GET /api/notifications` — current user's notifications
   - `GET /api/notifications/unread-count` — badge count
   - `PUT /api/notifications/{id}/read` — mark single as read
   - `PUT /api/notifications/read-all` — mark all as read

### Frontend Tasks

1. **Claim Submission Modal/Page**
   - Triggered from item detail page
   - For FOUND items: user enters "Secret Detail" answer + optional message
   - For LOST items: user writes message describing their found item match
   - Confirmation dialog before submitting

2. **Claims Management Page**
   - Page: `/my-claims` — claims the user has submitted
   - Cards with: item thumbnail, title, status badge, date submitted
   - Click → claim detail

3. **Incoming Claims View (for posters)**
   - On item detail page or `/my-items/{id}/claims`
   - List of claims with: claimant name, their secret detail attempt, message
   - Poster sees both the secret detail they set and what the claimant guessed
   - Approve / Reject buttons per claim
   - Only one claim can be approved per item

4. **Handover Confirmation UI**
   - After claim is approved, both parties see "Confirm Handover" button
   - Progress indicator: "Waiting for both parties to confirm"
   - Status updates:
     - "You confirmed. Waiting for the other party."
     - "Both confirmed! Item marked as handed over."

5. **Notification System UI**
   - Notification bell in header now fetches real unread count
   - Dropdown shows recent notifications
   - Dedicated `/notifications` page with full history
   - Click notification → navigate to related item/claim

### Mobile Tasks

1. **Claim Submission**
   - Bottom sheet or new screen for claim form
   - Secret detail input, message field

2. **My Claims Screen**
   - List of submitted claims with status
   - Pull-to-refresh

3. **Incoming Claims**
   - Section in item detail showing pending claims (if user is poster)
   - Approve/Reject actions

4. **Handover Confirmation**
   - Confirm button on approved claim detail screen
   - Visual status tracking

5. **Notifications**
   - Badge on bottom nav or toolbar
   - Notification list screen
   - Pull data from notifications endpoint

### Security Considerations
- A claimant must never see the secret detail set by the poster
- The poster must see both the original secret and the claimant's attempt side-by-side for comparison
- Only the item poster can approve/reject claims
- Both parties must independently confirm handover — no single-party completion
- Claims on user's own items must be blocked server-side
- Rate-limit claim submissions to prevent spam (e.g., max 5 active claims per user)

---

## Phase 4: Admin Moderation & Campus Office Handover

### Objectives
- Campus security admins can moderate items and users within their university
- Admin can override handover in disputes
- Admin action audit trail
- Admin dashboard with campus-scoped data

### Backend Tasks

1. **Admin Access Control**
   - Add `@PreAuthorize("hasRole('ADMIN')")` to admin endpoints
   - Create `AdminService` that scopes all queries to the admin's school
   - SUPER_ADMIN bypasses school scoping

2. **Admin Endpoints** (`/api/admin`)
   - `GET /api/admin/items` — all items for admin's campus
   - `GET /api/admin/items?status=ACTIVE` — filter by status
   - `DELETE /api/admin/items/{id}` — remove inappropriate items (with reason)
   - `GET /api/admin/users` — all users at admin's campus
   - `PUT /api/admin/users/{id}/ban` — ban user (with reason)
   - `PUT /api/admin/users/{id}/unban` — unban user
   - `GET /api/admin/claims/{id}` — view claim details
   - `PUT /api/admin/claims/{id}/override-handover` — force-complete a handover in dispute
   - `GET /api/admin/stats` — dashboard statistics (active items, pending claims, resolved this month, etc.)

3. **Admin Action Audit Log**
   - Create `AdminActionEntity` and `AdminActionRepository`
   - Log every admin action with: admin, action type, target, reason, timestamp
   - `GET /api/admin/actions` — audit trail (admin and super-admin only)

4. **Admin User Creation**
   - `POST /api/admin/create-admin` — SUPER_ADMIN only, creates admin account for a campus
   - Or: seed initial admin accounts via data initializer

5. **Campus Office Handover Location**
   - Add `handoverLocations` field to SchoolEntity: array of `{ name, building, room, hours }`
   - `GET /api/schools/{id}/handover-locations` — public endpoint
   - Admin can suggest specific handover location when overriding

### Frontend Tasks

1. **Admin Dashboard**
   - Page: `/admin` (only visible to ADMIN/SUPER_ADMIN)
   - Stats cards: active found items, pending claims, items recovered this month, flagged users
   - Recent activity feed (admin actions)

2. **Admin Item Management**
   - Page: `/admin/items`
   - Table/list of all items at admin's campus
   - Filter by status, type, date range
   - Actions: view detail, remove item (with reason modal)

3. **Admin User Management**
   - Page: `/admin/users`
   - Table of all users at admin's campus
   - Search by name, email, student ID
   - Actions: view profile, ban/unban (with reason modal)

4. **Admin Claims Oversight**
   - Page: `/admin/claims`
   - List of all claims at admin's campus
   - Filter by status
   - Override handover button for disputed claims (with reason)

5. **SUPER_ADMIN Panel**
   - Page: `/superadmin`
   - Cross-campus statistics
   - School management (add/edit/deactivate schools)
   - Create admin accounts

### Mobile Tasks

1. **Admin Mobile View (Simplified)**
   - If user is ADMIN: show "Admin" tab in bottom navigation
   - Campus item list with quick-remove action
   - User list with quick-ban action
   - Override handover capability
   - Keep mobile admin view lightweight — full admin work is expected on web

### Security Considerations
- Admin actions must be strictly scoped to their own campus
- SUPER_ADMIN should be a separate, manually-provisioned role (not self-registrable)
- All moderation actions must have a recorded reason
- Admin audit log must be immutable (append-only)
- Banned users should have their JWT invalidated (add token blacklist or short expiry)

---

## Phase 5: Maps, Analytics & Karma System

### Objectives
- Interactive campus maps showing item locations
- Karma/trust system to reward good community behavior
- Dashboard analytics for both users and admins

### Backend Tasks

1. **GeoJSON & Map Data**
   - Add MongoDB 2dsphere index on `items.locationCoordinates`
   - `GET /api/items/nearby?lng=X&lat=Y&radius=500` — geo-query for items near a point
   - `GET /api/schools/{id}/boundary` — return campus GeoJSON boundary
   - Serve campus boundary polygons as GeoJSON (can be stored in school entity or separate collection)
   - `GET /api/map/items?school={id}` — all active items at a campus with coordinates

2. **Karma System**
   - Karma rules:
     - +10 points: successful handover of a FOUND item you posted
     - +5 points: successful claim that leads to recovery
     - +2 points: posting a LOST item (community awareness)
     - -20 points: admin removes your item for policy violation
     - -50 points: user gets banned
   - Add `karmaScore`, `totalItemsReturned`, `totalItemsClaimed` to UserEntity
   - Update karma in `ClaimService.confirmHandover()` and `AdminService` actions
   - Create `KarmaService` with `updateKarma(userId, points, reason)`
   - Create `karma_history` collection for audit trail
   - `GET /api/users/{id}/karma` — user's karma score and history

3. **Analytics Endpoints**
   - `GET /api/admin/analytics`
     - Items posted over time (daily/weekly/monthly)
     - Recovery rate (handed_over / total items)
     - Average time to recovery
     - Most common categories
     - Active users count
     - Campus comparison (SUPER_ADMIN only)
   - `GET /api/users/me/stats` — personal stats (items posted, claims made, karma history)

### Frontend Tasks

1. **Campus Map Page**
   - Page: `/map`
   - Embed Leaflet.js or Mapbox GL JS map
   - Show campus boundary polygon from GeoJSON
   - Plot item markers (different colors for LOST vs FOUND)
   - Click marker → popup with item summary → link to detail page
   - Campus selector to switch between universities
   - Filter markers by type and category

2. **Karma Profile Section**
   - On profile page: karma score badge, rank title (e.g., "Campus Helper", "Lost & Found Champion")
   - Karma history timeline
   - Leaderboard page: `/leaderboard` — top karma earners at the user's campus

3. **Analytics Dashboard (Admin)**
   - Add charts to admin dashboard using a lightweight chart library (e.g., Recharts)
   - Line chart: items posted over time
   - Pie chart: items by category
   - Bar chart: recovery rate by campus
   - Summary cards: total recovered, average recovery time

4. **User Stats Section**
   - On profile page: personal statistics
   - Items posted, items recovered, success rate

### Mobile Tasks

1. **Map Screen**
   - Use Google Maps SDK for Android
   - Display campus boundary and item markers
   - Marker tap → item summary card → navigate to detail
   - Filter by type

2. **Karma & Stats**
   - Karma score display on profile screen
   - Simple stats view (items posted, recovered)

3. **Leaderboard**
   - Simple list of top karma earners at user's campus

### Security Considerations
- Karma manipulation must be impossible from the client side — all calculations server-side only
- Analytics endpoints must respect campus scoping for ADMIN
- Map data should not expose exact coordinates of active LOST items (could enable theft); use approximate locations
- GeoJSON boundaries should be validated to prevent injection

---

## Phase 6: Security Hardening, Edge Cases & Production Readiness

### Objectives
- Harden all security layers
- Handle edge cases in all workflows
- Prepare for production deployment
- Performance optimization

### Backend Tasks

1. **Authentication Hardening**
   - Implement rate limiting on auth endpoints (e.g., 5 failed logins per 15 minutes per IP)
   - Add account lockout after N failed attempts
   - Implement token blacklist for logout (store invalidated tokens in Redis or MongoDB TTL collection)
   - Add refresh token rotation (new refresh token on each use, old one invalidated)
   - Add CSRF protection for cookie-based sessions (if applicable)
   - Validate JWT audience/issuer claims

2. **Input Validation & Sanitization**
   - Add `@Valid` annotations with proper constraints on all DTOs
   - Sanitize HTML/script content in text fields
   - Validate image file types (only JPEG, PNG, WEBP)
   - Validate file sizes server-side
   - Add request body size limit

3. **Error Handling**
   - Create global `@ControllerAdvice` exception handler
   - Standardize error responses: `{ "error": "code", "message": "human-readable", "timestamp": "..." }`
   - Map all exceptions to appropriate HTTP status codes
   - Never expose stack traces or internal details in error responses

4. **Edge Cases**
   - What if a user deletes their account while they have active claims? → Auto-cancel their pending claims, mark their items as CANCELLED
   - What if an admin is deactivated? → Reassign or flag their campus as unmoderated
   - What if both LOST and FOUND items for the same object are posted? → No auto-matching (per scope); let humans discover via search
   - What if a claim is approved but handover never happens? → Add 7-day handover timeout, then revert to ACTIVE
   - What if a school is deactivated? → Prevent new registrations, existing users remain but can't post

5. **Performance**
   - Add MongoDB indexes on frequently queried fields:
     - `items`: status, type, school, postedBy, createdAt, locationCoordinates (2dsphere)
     - `claims`: item, claimant, status
     - `notifications`: recipient, isRead, createdAt
   - Implement pagination on all list endpoints
   - Add caching headers for static resources (blurred images)
   - Consider MongoDB aggregation pipelines for analytics instead of in-memory calculation

6. **API Documentation**
   - Add SpringDoc/OpenAPI (Swagger) for auto-generated API docs
   - Document all endpoints, request/response schemas, and error codes

7. **Logging & Monitoring**
   - Add structured logging (SLF4J + Logback)
   - Log all authentication events (login, register, failed attempts)
   - Log all admin actions
   - Add health check endpoint: `GET /api/health`

### Frontend Tasks

1. **Error Handling**
   - Global error boundary component
   - User-friendly error pages (404, 500, network error)
   - Toast notifications for operation success/failure
   - Form validation with clear, specific error messages

2. **Loading States**
   - Skeleton loaders on all list pages
   - Button loading states during API calls
   - Optimistic UI updates where appropriate

3. **Accessibility**
   - ARIA labels on interactive elements
   - Keyboard navigation support
   - Color contrast compliance
   - Screen reader-compatible notifications

4. **Responsive Design**
   - Ensure all pages work on mobile browsers
   - Test on common screen sizes (360px - 1920px)
   - Mobile-first CSS adjustments

5. **Build & Deploy**
   - Environment-based API URL configuration (dev/staging/prod)
   - Production build optimization (code splitting, minification)
   - Static asset caching with content hashes

### Mobile Tasks

1. **Offline Handling**
   - Show cached data when offline
   - Queue actions (post, claim) for retry when connection restored
   - Clear "No internet connection" indicator

2. **Error States**
   - Retry mechanisms for failed API calls
   - User-friendly error messages
   - Empty state illustrations

3. **Performance**
   - Image caching with Glide
   - Lazy loading for lists
   - ProGuard / R8 optimization for release builds

4. **Build & Release**
   - Signing configuration for release APK
   - Version code management
   - Test on multiple Android versions (API 26+)

### Security Considerations
- Conduct a full OWASP Top 10 review against all endpoints
- Ensure no sensitive data in logs (passwords, tokens, secret details)
- Implement Content Security Policy headers on frontend
- Add HTTPS enforcement (redirect HTTP to HTTPS)
- Validate all redirect URLs to prevent open redirect
- Set secure cookie flags if using cookies
- Review MongoDB injection attack vectors (use parameterized queries, which Spring Data does by default)

---

## Phase Summary & Dependencies

```
Phase 1: Rebrand + Auth + Roles
   ↓
Phase 2: Item Lifecycle (depends on Phase 1 for authenticated users)
   ↓
Phase 3: Claims & Verification (depends on Phase 2 for items)
   ↓
Phase 4: Admin Moderation (depends on Phase 1 roles + Phase 2-3 for items/claims)
   ↓
Phase 5: Maps, Analytics, Karma (depends on Phase 2-3 for data + Phase 4 for admin views)
   ↓
Phase 6: Hardening & Production (runs partially in parallel with Phase 4-5)
```

### Critical Path
Phases 1 → 2 → 3 are strictly sequential. Phase 4 can begin after Phase 2 is complete. Phase 5 can overlap with Phase 4. Phase 6 should be an ongoing concern starting from Phase 3.

---

## Constraints & Decisions Log

| Decision | Rationale |
|----------|-----------|
| Manual verification only, no AI | Scope constraint — human judgment for secret detail matching |
| No QR codes | Explicitly excluded from scope |
| No payment integration | Explicitly excluded from scope |
| Images blurred server-side | Found items must protect against false claimants recognizing items |
| Dual-confirmation handover | Both parties must confirm to prevent disputes |
| Campus-scoped admin | Each university manages its own data; no cross-campus admin access |
| MongoDB for all data | Already chosen and wired; fits document-based item/claim structures |
| JWT stateless auth | Already implemented; maintain but harden |
| 30-day item expiry | Prevents stale data accumulation |
| Karma is server-calculated only | Prevents manipulation; all score changes happen in backend services |
| Local file storage for images | Sufficient for capstone scope; cloud storage is optional upgrade |

---

## File / Folder Additions Expected

```
backend/src/main/java/com/hulampay/backend/
├── entity/
│   ├── ItemEntity.java          (new)
│   ├── ClaimEntity.java         (new)
│   ├── NotificationEntity.java  (new)
│   └── AdminActionEntity.java   (new)
├── dto/
│   ├── ItemDTO.java             (new)
│   ├── CreateItemDTO.java       (new)
│   ├── ClaimDTO.java            (new)
│   ├── CreateClaimDTO.java      (new)
│   └── NotificationDTO.java     (new)
├── repository/
│   ├── ItemRepository.java      (new)
│   ├── ClaimRepository.java     (new)
│   ├── NotificationRepository.java (new)
│   └── AdminActionRepository.java  (new)
├── service/
│   ├── ItemService.java         (new)
│   ├── ClaimService.java        (new)
│   ├── NotificationService.java (new)
│   ├── AdminService.java        (new)
│   ├── KarmaService.java        (new)
│   └── ImageService.java        (new)
├── controller/
│   ├── ItemController.java      (new)
│   ├── ClaimController.java     (new)
│   ├── NotificationController.java (new)
│   └── AdminController.java     (new)
├── config/
│   └── ScheduledTasks.java      (new — item expiration cron)
├── exception/
│   ├── GlobalExceptionHandler.java (new)
│   ├── ResourceNotFoundException.java (new)
│   └── UnauthorizedException.java (new)
└── util/
    └── ImageBlurUtil.java       (new)

website/src/
├── pages/
│   ├── PostItem.jsx             (new)
│   ├── ItemFeed.jsx             (new)
│   ├── ItemDetail.jsx           (new)
│   ├── MyItems.jsx              (new)
│   ├── MyClaims.jsx             (new)
│   ├── Notifications.jsx        (new)
│   ├── CampusMap.jsx            (new)
│   ├── Leaderboard.jsx          (new)
│   ├── AdminDashboard.jsx       (new)
│   ├── AdminItems.jsx           (new)
│   ├── AdminUsers.jsx           (new)
│   ├── AdminClaims.jsx          (new)
│   └── SuperAdminPanel.jsx      (new)
├── components/
│   ├── ItemCard.jsx             (new)
│   ├── ClaimModal.jsx           (new)
│   ├── NotificationDropdown.jsx (new)
│   ├── MapView.jsx              (new)
│   ├── KarmaBadge.jsx           (new)
│   └── AdminRoute.jsx           (new)
├── services/
│   ├── itemService.js           (new)
│   ├── claimService.js          (new)
│   ├── notificationService.js   (new)
│   ├── adminService.js          (new)
│   └── mapService.js            (new)
└── context/
    └── NotificationContext.jsx   (new)

mobile/.../
├── data/model/
│   ├── Item.kt                  (new)
│   ├── Claim.kt                 (new)
│   └── Notification.kt          (new)
├── data/api/
│   ├── ItemApiService.kt        (new)
│   ├── ClaimApiService.kt       (new)
│   └── NotificationApiService.kt (new)
├── data/repository/
│   ├── ItemRepository.kt        (new)
│   ├── ClaimRepository.kt       (new)
│   └── NotificationRepository.kt (new)
├── ui/
│   ├── items/ItemFeedScreen.kt  (new)
│   ├── items/ItemDetailScreen.kt (new)
│   ├── items/PostItemScreen.kt  (new)
│   ├── items/MyItemsScreen.kt   (new)
│   ├── claims/MyClaimsScreen.kt (new)
│   ├── claims/ClaimFormScreen.kt (new)
│   ├── notifications/NotificationsScreen.kt (new)
│   ├── map/MapScreen.kt         (new)
│   └── admin/AdminScreen.kt     (new)
└── navigation/
    └── Screen.kt                (modify — add new routes)
```

---

## Pre-Implementation Checklist

Before starting Phase 1, confirm:

- [ ] MongoDB Atlas cluster is accessible and has sufficient quota
- [ ] `.env` file exists with `MONGODB_URI`, `MONGODB_DATABASE`, and `JWT_SECRET`
- [ ] Cebu university email domains are confirmed (cit.edu, usc.edu.ph, usjr.edu.ph, uc.edu.ph, etc.)
- [ ] Android emulator or test device is available for mobile testing
- [ ] Node.js and npm are installed for React development
- [ ] Java 21 and Maven are installed for backend development

---

*This plan is designed for a capstone-level system. Each phase builds on the previous, and the scope is locked to manual, human-verified workflows with no AI/ML, QR codes, or payment integrations.*
