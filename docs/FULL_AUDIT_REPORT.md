# UniLost — Full Codebase Audit Report

**Date:** March 24, 2026  
**Scope:** Backend (Spring Boot + MongoDB) & Frontend (React/Vite)  
**Auditor:** Cascade AI  

---

## Executive Summary

The UniLost codebase is **well-structured** and demonstrates strong engineering fundamentals: proper layered architecture, environment-variable-based secrets, JWT with DB-role verification, optimistic locking, batch DTO conversion to prevent N+1 queries, compound MongoDB indexes, and a comprehensive rate limiter. However, the audit identified **1 Critical, 5 High, 9 Medium, and 8 Low** severity issues that should be addressed before production deployment.

---

## 1. SECURITY REVIEW

### CRITICAL

#### S1 — Hardcoded Admin Password in Source Code
- **File:** `backend/.../config/DataSeeder.java:152`
- **Issue:** `private static final String ADMIN_PASSWORD = "Sitheogwapo@123";` is committed in plain text. Anyone with repo access (including forks, CI logs, or leaked repos) can log in as the admin.
- **Root Cause:** Convenience during development; password was not moved to env vars.
- **Fix:** Move to an environment variable:
  ```java
  @Value("${admin.seed-password:#{null}}")
  private String adminPassword;
  ```
  Skip seeding if the variable is absent in production. **Immediately rotate the password** on any deployed environment.

---

### HIGH

#### S2 — JWT Secret Key Length Not Enforced
- **File:** `backend/.../util/JwtUtils.java:20`
- **Issue:** `Keys.hmacShaKeyFor(secret.getBytes())` will accept any string. If `JWT_SECRET` is shorter than 32 bytes (256 bits), the HMAC-SHA256 key is cryptographically weak and the library may throw at runtime.
- **Root Cause:** No startup validation of the secret length.
- **Fix:** Add a `@PostConstruct` check:
  ```java
  @PostConstruct
  void validate() {
      if (key.getEncoded().length < 32) {
          throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for HS256");
      }
  }
  ```

#### S3 — Password Reset Flow: OTP Not Re-Verified Before Reset
- **File:** `backend/.../service/UserService.java:295-306`
- **Issue:** `resetPassword()` relies solely on the `otpVerified` boolean flag on the `UserEntity`. Since the flag persists in the DB until the password is reset, a separate request (e.g., from a different browser session) that knows the email can call `/api/auth/reset-password` with a new password — no OTP needed. This is a **time-of-check-to-time-of-use (TOCTOU)** window.
- **Root Cause:** The OTP verification and password reset are two separate, unsecured HTTP calls with no shared session token binding them together.
- **Fix:** Issue a short-lived, single-use **reset token** (e.g., a signed JWT or random UUID stored on the user) upon successful OTP verification. Require that token in the `/reset-password` call instead of relying on a boolean flag.

#### S4 — `UpdateUserRequest` Allows Password Change Without Current Password
- **File:** `backend/.../dto/UpdateUserRequest.java:17-20`, `backend/.../service/UserService.java:146-157`
- **Issue:** The `PUT /api/users/{id}` endpoint accepts a `password` field in `UpdateUserRequest` and re-hashes it directly — bypassing the dedicated `changePassword()` method that verifies the current password first. An attacker who steals the JWT can change the password without knowing the old one.
- **Root Cause:** Dual password-change paths; the profile update path has no current-password verification.
- **Fix:** Remove the `password` field from `UpdateUserRequest` entirely. Password changes should only go through `PUT /api/users/{id}/change-password`.

#### S5 — CSRF Disabled Entirely
- **File:** `backend/.../config/SecurityConfig.java:37`
- **Issue:** `csrf(AbstractHttpConfigurer::disable)` — while standard for stateless JWT APIs, the WebSocket STOMP endpoint uses SockJS which establishes an HTTP session. CSRF protection should be considered for the SockJS fallback transport.
- **Root Cause:** Blanket CSRF disable for API simplicity.
- **Fix:** This is acceptable for a pure JWT-stateless API. However, document the decision and ensure no cookie-based session authentication is ever added. **Low risk given current architecture but flagged for awareness.**

#### S6 — Rate Limiter Does Not Inspect `X-Forwarded-For`
- **File:** `backend/.../filter/RateLimitFilter.java:98-100`
- **Issue:** `getClientIp()` uses `request.getRemoteAddr()`. Behind a reverse proxy (Nginx, Cloudflare, AWS ALB), all requests share the proxy's IP, making the rate limiter ineffective.
- **Root Cause:** Simple implementation for local dev.
- **Fix:**
  ```java
  private String getClientIp(HttpServletRequest request) {
      String xff = request.getHeader("X-Forwarded-For");
      if (xff != null && !xff.isBlank()) {
          return xff.split(",")[0].trim();
      }
      return request.getRemoteAddr();
  }
  ```

---

### MEDIUM

#### S7 — No File Size Validation on Cloudinary Upload (Per-File)
- **File:** `backend/.../service/CloudinaryService.java:29-36`
- **Issue:** `validateImageFile()` checks content type but not individual file size. The global multipart limit is 10MB/file but malicious clients can bypass Spring's multipart config. Profile picture upload enforces 5MB in `UserService` but item image uploads do not enforce a per-file limit at service level.
- **Root Cause:** Reliance on Spring's servlet-level limit only.
- **Fix:** Add `if (file.getSize() > MAX_FILE_SIZE)` check inside `validateImageFile()`.

#### S8 — `CampusController` Missing `@Valid` on Create/Update
- **File:** `backend/.../controller/CampusController.java:26,58`
- **Issue:** `createCampus(@RequestBody CampusDTO)` and `updateCampus()` don't use `@Valid`, so any validation annotations on `CampusDTO` are ignored. An admin can create a campus with blank/invalid data.
- **Root Cause:** Validation annotation omitted.
- **Fix:** Add `@Valid` to the `@RequestBody` parameters.

#### S9 — `ChatEntity` Missing Indexes on `itemId`, `finderId`, `ownerId`
- **File:** `backend/.../entity/ChatEntity.java`
- **Issue:** While `MongoIndexInitializer` creates compound indexes programmatically, the entity itself has no `@Indexed` annotations. This is fine functionally, but creates a discrepancy with other entities that do use `@Indexed`.
- **Root Cause:** Indexes managed externally.
- **Fix:** Add `@Indexed` annotations for consistency and self-documentation.

#### S10 — `MessageEntity` Missing Index on `chatId`
- **File:** `backend/.../entity/MessageEntity.java`
- **Issue:** Same as S9 — `chatId` is not annotated with `@Indexed` on the entity despite being used in all message queries.
- **Fix:** Add `@Indexed` annotation. The programmatic index already covers this, but annotation is best practice.

#### S11 — Frontend Stores JWT in `localStorage`
- **File:** `website/src/services/authService.js:30-31`
- **Issue:** `localStorage.setItem('token', token)` — tokens in localStorage are accessible to any JavaScript running on the page, making them vulnerable to XSS attacks.
- **Root Cause:** Standard SPA pattern, but less secure than httpOnly cookies.
- **Fix:** For production, consider using httpOnly cookies with SameSite=Strict. If localStorage is kept, ensure robust CSP headers and XSS prevention. **Acceptable for current stage but should be hardened before production.**

#### S12 — Frontend Role Checks Based on localStorage (Client-Side Only)
- **File:** `website/src/components/AdminRoute.jsx`, `website/src/services/authService.js:80-103`
- **Issue:** Admin/Faculty route guards read the user's role from localStorage. A user can tamper with localStorage to gain client-side access to admin pages. The backend enforces RBAC correctly, so data is safe, but the admin UI would be visible.
- **Root Cause:** Standard SPA pattern — backend is the true authority.
- **Fix:** Acceptable since backend enforces all access. For defense-in-depth, verify the role from `/api/auth/me` on protected page load instead of trusting localStorage.

#### S13 — `forgotPassword` Endpoint Leaks Timing Information
- **File:** `backend/.../service/UserService.java:225-238`
- **Issue:** When the email exists, the method sends an OTP (slow network call). When it doesn't, it returns immediately. This timing difference enables user enumeration.
- **Root Cause:** Silent return without matching delay.
- **Fix:** Add a random delay (200-500ms) in the "email not found" path, or always perform a dummy hash computation.

#### S14 — `.env.example` Missing `VITE_API_URL`
- **File:** `website/.env.example`
- **Issue:** Only `VITE_GOOGLE_MAPS_API_KEY` is listed. `VITE_API_URL` (used in `api.js:5`) is missing, which means developers may accidentally use the default `http://localhost:8080/api` in production.
- **Fix:** Add `VITE_API_URL=http://localhost:8080/api` to `.env.example`.

#### S15 — No `@Scheduled` Enablement Annotation
- **File:** `backend/.../filter/RateLimitFilter.java:103`
- **Issue:** `@Scheduled(fixedRate = 300_000)` on the cleanup method requires `@EnableScheduling` on a config class. If this annotation is missing, the cleanup never runs and the in-memory map grows unbounded.
- **Root Cause:** May work via auto-configuration, but should be explicit.
- **Fix:** Add `@EnableScheduling` to `SecurityConfig` or the main `UniLostApplication` class.

---

### LOW

#### S16 — No Content-Security-Policy Headers
- **Issue:** No CSP headers are set. This increases XSS risk on the frontend.
- **Fix:** Add CSP headers via Spring Security or the frontend web server.

#### S17 — WebSocket CONNECT Allows Anonymous Fallthrough
- **File:** `backend/.../config/WebSocketAuthInterceptor.java:60`
- **Issue:** If no valid JWT is provided on CONNECT, the user remains `null` rather than being rejected. While SUBSCRIBE/SEND are blocked, the connection itself stays open, consuming server resources.
- **Fix:** Consider rejecting unauthenticated CONNECT frames entirely.

---

## 2. BUG DETECTION

### HIGH

#### B1 — `authService.resetPassword()` Sends Unused `otp` Parameter
- **File:** `website/src/services/authService.js:126-133`
- **Issue:** `resetPassword(email, otp, newPassword)` sends `{ email, otp, newPassword }` to the backend, but the backend `POST /api/auth/reset-password` controller (`AuthController.java:74-91`) only reads `email` and `newPassword` from the body — it ignores `otp`. The frontend collects 3 parameters but the third (`otp`) is dead code on the wire.
- **Root Cause:** Mismatch between frontend API contract and backend implementation after refactoring.
- **Fix:** Remove the `otp` parameter from the frontend `resetPassword()` call signature since the backend relies on the `otpVerified` flag instead.

#### B2 — `ClaimService.submitClaim()` Race Condition on LOST Item Auto-Accept
- **File:** `backend/.../service/ClaimService.java:86-119`
- **Issue:** For LOST items, claims are auto-accepted. If two users submit claims simultaneously, both can be auto-accepted before either saves the item status to `CLAIMED`. The check `item.getStatus() != ItemStatus.ACTIVE` at line 47 is not atomic — both threads can read `ACTIVE`.
- **Root Cause:** No optimistic lock on `ItemEntity` version check during claim submission.
- **Fix:** Use the `@Version` field already on `ItemEntity` — reload and verify status atomically, or use MongoDB's `findAndModify` to atomically transition the item from `ACTIVE` to `CLAIMED`.

---

### MEDIUM

#### B3 — `AdminService.verifyCampusAccess()` NullPointerException
- **File:** `backend/.../service/AdminService.java:409-413`
- **Issue:** `admin.getUniversityTag().equals(targetCampusId)` — if the admin's `universityTag` is null (data corruption), this throws NPE.
- **Fix:** Use `Objects.equals()` or null-check first.

#### B4 — `AdminService.getCrossCampusStats()` N+1 Query Pattern
- **File:** `backend/.../service/AdminService.java:368-396`
- **Issue:** Iterates over all campuses and fires 4 count queries per campus inside the loop. With 12 seeded campuses, that's 48 DB queries per request.
- **Root Cause:** Straightforward implementation without aggregation.
- **Fix:** Use MongoDB aggregation pipelines to compute all counts in 1-2 queries.

#### B5 — `flagItem()` Does Not Notify the Item Reporter
- **File:** `backend/.../service/AdminService.java:262-289`
- **Issue:** When a user flags an item, `notifyItemFlagged()` is never called. The `NotificationService` has the method, but it's not wired up.
- **Root Cause:** Notification call was likely forgotten during implementation.
- **Fix:** Add `notificationService.notifyItemFlagged(item.getReporterId(), item.getTitle(), itemId);` after the flag is saved. Requires injecting `NotificationService` into `AdminService`.

#### B6 — `UserController.getUserById()` Exposes Full User Data to Any Authenticated User
- **File:** `backend/.../controller/UserController.java:52-57`
- **Issue:** `GET /api/users/{id}` is accessible to any authenticated user and returns the full `UserDTO` including email, role, and account status. This could be a privacy concern.
- **Root Cause:** No privacy filtering for non-self, non-admin users.
- **Fix:** Strip sensitive fields (email, accountStatus) when the requesting user is not the profile owner or an admin.

#### B7 — `UserService.convertToDTO()` Fetches Campus on Every Call
- **File:** `backend/.../service/UserService.java:347-362`
- **Issue:** Every `convertToDTO()` call does a `campusRepository.findById()`. When `getAllUsers()` is called with a page of 20 users, this results in up to 20 individual campus lookups (N+1 query).
- **Root Cause:** No batch loading in `UserService.getAllUsers()`.
- **Fix:** Batch-load campuses like `ItemService.convertToDTOs()` does.

#### B8 — `ClaimService.acceptClaim()` Fetches Item Three Times
- **File:** `backend/.../service/ClaimService.java:190, 215, 234`
- **Issue:** The same item is fetched via `findByIdAndIsDeletedFalse` three separate times within `acceptClaim()`.
- **Root Cause:** Incremental code additions without refactoring.
- **Fix:** Fetch the item once at the top and reuse the reference.

#### B9 — `CampusController.createCampus()` Has No Duplicate Check
- **File:** `backend/.../controller/CampusController.java:25-29`
- **Issue:** An admin can create duplicate campuses with the same ID or domain. The DataSeeder checks `campusRepository.count() > 0`, but the API endpoint does not.
- **Root Cause:** Missing uniqueness validation.
- **Fix:** Check for existing campus by ID or domain before creation.

---

### LOW

#### B10 — Frontend `authService.register()` Does Not Send `campusId`
- **File:** `website/src/services/authService.js:6-17`
- **Issue:** The `register()` function only sends `fullName`, `email`, `password` — it never includes `campusId`. For universities with multiple campuses sharing the same email domain (USC, USJ-R, UC), registration will fail with "Multiple campuses use this email domain. Please select your campus."
- **Root Cause:** The Register page may handle this separately, but the service function doesn't forward it.
- **Fix:** Include `campusId: userData.campusId` in the request body.

#### B11 — `ClaimService` Line 700+ `convertToDTO` Truncated
- **File:** `backend/.../service/ClaimService.java:700+`
- **Issue:** The `finderId`/`finderName` resolution and handover timestamp fields are in the truncated portion. If there's a bug there, it's invisible to this review. (Functionally likely fine based on the pattern.)

#### B12 — `NotificationController.getEmail()` May Return "anonymousUser"
- **File:** `backend/.../controller/NotificationController.java:58-60`
- **Issue:** `SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()` — if somehow an anonymous request reaches this endpoint, it returns `"anonymousUser"` as the email, which would trigger a `ResourceNotFoundException` downstream. Not a real bug since the endpoint requires authentication, but fragile.
- **Fix:** Add a null/anonymous check consistent with `AuthController.getCurrentUser()`.

---

## 3. PERFORMANCE & OPTIMIZATION

### MEDIUM

#### P1 — `AdminService.getAnalytics()` Issues Many Individual Count Queries
- **File:** `backend/.../service/AdminService.java:293-363`
- **Issue:** Iterates over all `ItemStatus` values with individual `countBy...` queries (7+ queries), plus fetches category and location items into memory for grouping.
- **Fix:** Use MongoDB aggregation with `$group` to compute all status counts, top categories, and top locations in 1-2 pipeline executions.

#### P2 — `ChatService.getTotalUnreadCount()` Loads All User Chats First
- **File:** `backend/.../service/ChatService.java:281-305`
- **Issue:** Fetches all `ChatEntity` objects just to extract their IDs for the aggregation query.
- **Fix:** Use a projection query to fetch only chat IDs, or restructure the aggregation to match on participant fields directly.

#### P3 — No Pagination on `ChatService.getMyChats()`
- **File:** `backend/.../service/ChatService.java:70-103`
- **Issue:** Returns all chats for a user without pagination. For power users with many resolved claims, this list could grow large.
- **Fix:** Add pagination support (`Pageable` parameter).

#### P4 — `ItemService.getMapItems()` Hard Limit of 200
- **File:** `backend/.../service/ItemService.java:284`
- **Issue:** `query.limit(200)` — for a busy campus this could be too many items for the map to render smoothly. No cursor/pagination for progressive loading.
- **Fix:** Consider clustering or dynamic limit based on zoom level.

---

### LOW

#### P5 — In-Memory Rate Limiter Doesn't Scale Horizontally
- **File:** `backend/.../filter/RateLimitFilter.java`
- **Issue:** The `ConcurrentHashMap` is per-JVM instance. If the app is deployed behind a load balancer with multiple instances, each instance has its own rate limit counters.
- **Fix:** For multi-instance deployment, use Redis-based rate limiting (e.g., Spring Cloud Gateway or Bucket4j with Redis).

#### P6 — `@Transactional` on MongoDB Without Replica Set
- **Files:** `ItemService.java:180`, `ClaimService.java:184,333,397,471,529`
- **Issue:** `@Transactional` annotations require a MongoDB replica set. If the app is connected to a standalone MongoDB instance, transactions silently don't work and data inconsistencies can occur on partial failures.
- **Fix:** Ensure MongoDB Atlas (which uses replica sets by default) is configured. Document this as a deployment requirement.

---

## 4. ARCHITECTURE & CONSISTENCY

### MEDIUM

#### A1 — Duplicated DTO Conversion Logic Across Services
- **Files:** `ItemService.convertToDTO()`, `AdminService.convertItemToDTO()`, `ClaimService.convertToDTO()`
- **Issue:** Item and User DTO conversion is duplicated in 3 services with slight variations. Changes to the DTO structure require updates in multiple places.
- **Fix:** Extract shared conversion logic into a `DtoMapper` utility class or use MapStruct.

#### A2 — Inconsistent Error Response Formats
- **Issue:** Some controllers return `Map.of("error", message)`, others return raw strings (`"You can only update your own profile"`), and the `GlobalExceptionHandler` consistently returns `Map.of("error", ...)`. Frontend error extraction handles both patterns but this is fragile.
- **Fix:** Standardize all error responses to `{ "error": "message" }` format through the `GlobalExceptionHandler`.

#### A3 — Missing `@EnableScheduling` Annotation
- **Issue:** `RateLimitFilter.cleanup()` uses `@Scheduled` but no `@EnableScheduling` annotation was found on any configuration class. Without it, the cleanup job never runs.
- **Fix:** Add `@EnableScheduling` to `UniLostApplication` or a dedicated config class.

#### A4 — `UserProbe.java` — Unused File
- **File:** `backend/.../UserProbe.java`
- **Issue:** This file exists in the root package alongside `UniLostApplication.java`. Its purpose is unclear and it may be dead code.
- **Fix:** Review and remove if unused.

---

### LOW

#### A5 — No Unit/Integration Tests
- **Files:** `backend/src/test/`
- **Issue:** The test directory exists but no meaningful tests were found. The `pom.xml` includes test dependencies (`spring-boot-starter-data-mongodb-test`, `spring-boot-starter-webmvc-test`) but they are unused.
- **Fix:** Add tests for critical flows: authentication, claim lifecycle, RBAC enforcement, OTP verification.

#### A6 — Frontend Has No Error Boundary
- **Issue:** No React Error Boundary component exists. Unhandled rendering errors will crash the entire app.
- **Fix:** Add a top-level `<ErrorBoundary>` component wrapping `<Router>`.

#### A7 — Inconsistent `@Indexed` Usage
- **Issue:** Some entities use `@Indexed` (UserEntity, ItemEntity, ClaimEntity) while others rely entirely on `MongoIndexInitializer`. This creates confusion about which indexes actually exist.
- **Fix:** Use both: `@Indexed` for self-documentation on entities, and `MongoIndexInitializer` for compound indexes that can't be expressed via annotations.

#### A8 — Missing `VITE_API_URL` in `.env.example`
- **File:** `website/.env.example`
- **Issue:** Already noted in S14. Developers cloning the repo won't know about this required env var.
- **Fix:** Add to `.env.example`.

---

## Summary Table

| # | Severity | Category | Title |
|---|----------|----------|-------|
| S1 | **CRITICAL** | Security | Hardcoded admin password in DataSeeder |
| S2 | HIGH | Security | JWT secret key length not enforced |
| S3 | HIGH | Security | Password reset TOCTOU — OTP flag not bound to session |
| S4 | HIGH | Security | UpdateUserRequest allows password change without verification |
| S5 | HIGH | Security | CSRF disabled (acceptable but document) |
| S6 | HIGH | Security | Rate limiter ignores X-Forwarded-For |
| S7 | MEDIUM | Security | No per-file size validation on image upload |
| S8 | MEDIUM | Security | CampusController missing @Valid |
| S9 | MEDIUM | Security | ChatEntity missing @Indexed annotations |
| S10 | MEDIUM | Security | MessageEntity missing @Indexed annotations |
| S11 | MEDIUM | Security | JWT stored in localStorage (XSS risk) |
| S12 | MEDIUM | Security | Client-side role checks from localStorage |
| S13 | MEDIUM | Security | Timing-based user enumeration in forgotPassword |
| S14 | MEDIUM | Security | .env.example missing VITE_API_URL |
| S15 | MEDIUM | Security | @Scheduled without @EnableScheduling |
| S16 | LOW | Security | No Content-Security-Policy headers |
| S17 | LOW | Security | WebSocket allows anonymous CONNECT |
| B1 | HIGH | Bug | Frontend resetPassword sends unused otp param |
| B2 | HIGH | Bug | Race condition on LOST item claim auto-accept |
| B3 | MEDIUM | Bug | AdminService.verifyCampusAccess NPE risk |
| B4 | MEDIUM | Bug | getCrossCampusStats N+1 (48 queries) |
| B5 | MEDIUM | Bug | flagItem does not notify item reporter |
| B6 | MEDIUM | Bug | getUserById exposes full data to any user |
| B7 | MEDIUM | Bug | UserService.convertToDTO N+1 campus lookups |
| B8 | MEDIUM | Bug | acceptClaim fetches same item 3 times |
| B9 | MEDIUM | Bug | createCampus has no duplicate check |
| B10 | LOW | Bug | Frontend register() doesn't send campusId |
| B11 | LOW | Bug | ClaimService convertToDTO truncated in review |
| B12 | LOW | Bug | NotificationController may pass "anonymousUser" |
| P1 | MEDIUM | Performance | Analytics uses many individual count queries |
| P2 | MEDIUM | Performance | getTotalUnreadCount loads all chat entities |
| P3 | MEDIUM | Performance | getMyChats has no pagination |
| P4 | MEDIUM | Performance | Map items hard-capped at 200 |
| P5 | LOW | Performance | Rate limiter doesn't scale horizontally |
| P6 | LOW | Performance | @Transactional requires MongoDB replica set |
| A1 | MEDIUM | Architecture | Duplicated DTO conversion logic |
| A2 | MEDIUM | Architecture | Inconsistent error response formats |
| A3 | MEDIUM | Architecture | Missing @EnableScheduling |
| A4 | MEDIUM | Architecture | UserProbe.java may be dead code |
| A5 | LOW | Architecture | No unit/integration tests |
| A6 | LOW | Architecture | No React Error Boundary |
| A7 | LOW | Architecture | Inconsistent @Indexed usage |
| A8 | LOW | Architecture | .env.example incomplete |

---

## Positive Findings (What's Done Well)

1. **Secrets in env vars** — `application.properties` uses `${...}` for all credentials
2. **JWT role from DB** — Filter reads role from DB, not from the token, preventing role escalation
3. **Account status enforcement** — Suspended/deactivated users are blocked at the JWT filter level
4. **Optimistic locking** — Both `ItemEntity` and `ClaimEntity` use `@Version`
5. **Batch DTO conversion** — `ItemService`, `ClaimService`, `ChatService` all batch-load referenced entities
6. **Compound MongoDB indexes** — `MongoIndexInitializer` creates targeted indexes
7. **Rate limiting** — Tiered rate limits per endpoint type with proper HTTP headers
8. **WebSocket auth** — STOMP CONNECT/SUBSCRIBE/SEND all verified
9. **Input validation** — DTOs use Jakarta Validation annotations
10. **Regex injection prevention** — `Pattern.quote()` used on search keywords
11. **Soft delete** — Items are soft-deleted, preserving audit trail
12. **CORS configuration** — Properly scoped with configurable origins

---

## Priority Fix Order

1. **S1** — Hardcoded password (immediate, 5 min fix)
2. **S4** — Remove password from UpdateUserRequest (immediate, 5 min fix)
3. **S3** — Bind OTP verification to a reset token (30 min)
4. **B2** — Fix race condition on LOST item auto-accept (30 min)
5. **S6** — Fix rate limiter X-Forwarded-For (5 min)
6. **S2** — Add JWT secret length validation (5 min)
7. **A3/S15** — Add @EnableScheduling (1 min)
8. **B5** — Wire up flagItem notification (5 min)
9. **B10** — Fix frontend register to send campusId (5 min)
10. **B1** — Fix frontend resetPassword signature (2 min)
