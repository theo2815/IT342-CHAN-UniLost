# UniLost — Post-Phase 4 System-Wide Audit Report

**Date:** March 20, 2026  
**Scope:** Full codebase audit after Phase 4 (Claim & Verification) implementation  
**Auditor:** GitHub Copilot (Claude Opus 4.6)

---

## Executive Summary

Phase 4 introduced the Claim & Verification system (ClaimService, ClaimController, frontend claim pages). The audit identified **6 Critical**, **9 High**, **12 Medium**, and **6 Low** severity issues across security, performance, correctness, and consistency.

**Most urgent:** The backend has **compilation errors** that prevent building — the project cannot compile in its current state due to type mismatches between ClaimController↔ClaimService signatures, ClaimEntity String status vs ClaimStatus enum usage, and a missing Jackson dependency in RateLimitFilter.

---

## Issue Severity Legend

| Severity | Definition |
|----------|------------|
| **CRITICAL** | Blocks compilation, causes data corruption, or creates exploitable security holes. Fix immediately. |
| **HIGH** | Functional bugs, authorization gaps, or performance issues under load. Fix before next phase. |
| **MEDIUM** | Code quality, minor security hardening, UX issues. Fix within 2 sprints. |
| **LOW** | Best-practice improvements, future-proofing. Address when convenient. |

---

## CRITICAL Issues (6)

### C1. Backend Does Not Compile — Multiple Type Mismatches
**Severity:** CRITICAL | **Category:** Bug / Phase 4 Regression  
**Files:** ClaimController.java, ClaimService.java, ClaimEntity.java, ClaimRepository.java, RateLimitFilter.java

The backend has **three independent compilation failures**:

#### C1a. RateLimitFilter Missing Jackson Dependency
`RateLimitFilter.java` imports `com.fasterxml.jackson.databind.ObjectMapper` but `spring-boot-starter-webmvc` (Spring Boot 4.0.2) does not include Jackson by default.

**Verified:** Maven compile fails with `package com.fasterxml.jackson.databind does not exist`.

**Fix:**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```
OR replace ObjectMapper with manual JSON string construction.

#### C1b. ClaimController↔ClaimService Method Signature Mismatch
The controller calls service methods with wrong argument counts:

| Controller Call | Service Signature |
|---|---|
| `claimService.getMyClaims(email)` | `getMyClaims(String, Pageable)` — 2 params |
| `claimService.getIncomingClaims(email)` | `getIncomingClaims(String, Pageable)` — 2 params |
| `claimService.getClaimsForItem(itemId, email)` | `getClaimsForItem(String, String, Pageable)` — 3 params |

**Fix:** Add pagination parameters to ClaimController:
```java
@GetMapping("/my")
public ResponseEntity<Page<ClaimDTO>> getMyClaims(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    String email = (String) authentication.getPrincipal();
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ResponseEntity.ok(claimService.getMyClaims(email, pageable));
}
```
Apply same pattern to `/incoming` and `/item/{itemId}`.

#### C1c. ClaimEntity Status String vs ClaimStatus Enum
`ClaimEntity.status` is declared as `String` but `ClaimService` uses `ClaimStatus` enum:

- `claim.setStatus(ClaimStatus.ACCEPTED)` → compile error (String setter, enum argument)
- `claim.getStatus() != ClaimStatus.PENDING` → always `true` (reference inequality between String and enum)
- `claimRepository.findByItemIdAndStatus(id, ClaimStatus.PENDING)` → compile error (String param, enum arg)

**Fix (Option A — Recommended):** Change ClaimEntity to use enum:
```java
private ClaimStatus status = ClaimStatus.PENDING;
```
Then update ClaimRepository methods to accept `ClaimStatus` instead of `String`.

**Fix (Option B):** Change ClaimService to use strings:
```java
claim.setStatus(ClaimStatus.ACCEPTED.name());
if (!ClaimStatus.PENDING.name().equals(claim.getStatus())) { ... }
```

#### C1d. ClaimRepository Missing Pageable Overloads
The service calls `findByClaimantId(id, pageable)`, `findByFinderId(id, pageable)`, `findByItemId(id, pageable)` but the repository only defines non-pageable versions.

**Fix:** Add to ClaimRepository:
```java
Page<ClaimEntity> findByClaimantId(String claimantId, Pageable pageable);
Page<ClaimEntity> findByFinderId(String finderId, Pageable pageable);
Page<ClaimEntity> findByItemId(String itemId, Pageable pageable);
```

---

### C2. Race Condition on Claim Acceptance — No Transaction Boundary
**Severity:** CRITICAL | **Category:** Data Integrity  
**File:** ClaimService.java — `acceptClaim()` method

The accept flow performs 3 separate database operations with no atomicity:
1. Save accepted claim
2. Auto-reject all other PENDING claims on the same item
3. Update item status to CLAIMED

If two concurrent acceptances happen on the same item:
- Both read the same PENDING claims list
- Both try to reject each other's claim
- Both set the item to CLAIMED
- Result: **Two accepted claims** → data corruption

**Fix:** Wrap in `@Transactional` or use MongoDB multi-document transactions:
```java
@Transactional
public ClaimDTO acceptClaim(String claimId, String finderEmail) { ... }
```
Requires MongoDB replica set (Atlas supports this). Add `spring-boot-starter-data-mongodb` transaction manager.

---

### C3. Secret Detail Answer Not Required — Verification System Bypassable
**Severity:** CRITICAL | **Category:** Business Logic / Security  
**File:** ClaimRequest.java

`providedAnswer` has no `@NotBlank` validation:
```java
@Size(max = 500, message = "Answer must be 500 characters or less")
private String providedAnswer;  // Can be null or empty!
```

Users can submit claims with **empty secret answers**, completely bypassing the verification system that is the core purpose of Phase 4.

**Fix:**
```java
@NotBlank(message = "Secret detail answer is required")
@Size(max = 500, message = "Answer must be 500 characters or less")
private String providedAnswer;
```

---

### C4. JWT Token Storage in localStorage — XSS Token Theft
**Severity:** CRITICAL | **Category:** Security  
**File:** website/src/services/authService.js

```javascript
localStorage.setItem('token', token);    // Accessible to any JS on the page
localStorage.setItem('user', JSON.stringify(user));
```

Any XSS vulnerability (even from a third-party dependency) enables full account takeover by stealing the JWT token. localStorage has no `HttpOnly` or `Secure` flags.

**Fix (Short-term):** Keep localStorage but implement:
1. Short token lifetime (15 min) + refresh token rotation
2. Add Content-Security-Policy headers to prevent inline scripts

**Fix (Long-term):** Move to HttpOnly cookie-based auth:
- Backend sets JWT in HttpOnly, Secure, SameSite=Strict cookie
- Frontend removes token management entirely
- Add CSRF token for state-changing requests

---

### C5. No JWT Token Revocation or Expiry Handling
**Severity:** CRITICAL | **Category:** Security  
**Files:** JwtUtils.java, authService.js

- **Backend:** No token blacklist. Logout only clears localStorage — the token remains valid until natural expiry (1 hour default).
- **Frontend:** No expiry detection. Expired tokens cause silent 401s redirecting to login without explanation.
- **Impact:** A stolen token remains usable for the full expiry window even after the user "logs out."

**Fix:**
1. Implement server-side token blacklist (Redis or MongoDB collection) checked in `JwtAuthenticationFilter`
2. Add `jti` (JWT ID) claim for tracking
3. Frontend: decode token to check expiry before requests

---

### C6. Rate Limiter Bypassable via X-Forwarded-For Spoofing
**Severity:** CRITICAL | **Category:** Security  
**File:** RateLimitFilter.java

```java
private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();  // Trusts any value!
    }
    return request.getRemoteAddr();
}
```

Any client can send a fake `X-Forwarded-For` header with a random IP to bypass the 10-request/minute rate limit on auth endpoints, enabling brute-force attacks against login and OTP.

**Fix:** Only trust X-Forwarded-For behind a known reverse proxy. In development, use `getRemoteAddr()` only:
```java
private String getClientIp(HttpServletRequest request) {
    // Only use X-Forwarded-For in production behind trusted proxy
    return request.getRemoteAddr();
}
```

---

## HIGH Issues (9)

### H1. CORS Allows Wildcard Headers with Credentials
**Severity:** HIGH | **Category:** Security  
**File:** SecurityConfig.java

Previously used `List.of("*")` for allowed headers. **Current code correctly restricts headers** to `"Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"`. ✅ This is already fixed.

However, `configuration.setAllowCredentials(true)` combined with configurable origins from properties could be risky if origins are not validated at deployment.

**Fix:** Ensure production CORS origins are explicitly set (not wildcard).

---

### H2. Password Validation Too Weak on Backend
**Severity:** HIGH | **Category:** Security  
**File:** AuthController.java

```java
if (newPassword.length() < 6) {
    return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
}
```

No complexity requirements (uppercase, numbers, special characters). The frontend has a slightly stronger regex but it's bypassed easily.

**Fix:** Add server-side password policy:
```java
private static final Pattern PASSWORD_PATTERN = 
    Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$");

if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
    return ResponseEntity.badRequest().body(Map.of("error", 
        "Password must be at least 8 characters with uppercase, number, and special character"));
}
```

---

### H3. Frontend Auth State Not Context-Based — Race Conditions
**Severity:** HIGH | **Category:** Bug / UX  
**Files:** ProtectedRoute.jsx, AdminRoute.jsx, authService.js

Auth state relies on synchronous `localStorage.getItem('token')` checks without React state management:
- No cross-tab logout sync (logging out in tab A leaves tab B authenticated)
- Token expiry not detected until a 401 occurs
- Component re-renders don't trigger auth checks

**Fix:** Implement `AuthContext` provider wrapping the app, with `storage` event listener for cross-tab sync and token expiry checking.

---

### H4. MyClaims/IncomingClaims — No Error Handling on API Failure
**Severity:** HIGH | **Category:** Bug / UX  
**Files:** MyClaims.jsx, IncomingClaims.jsx

```javascript
// MyClaims.jsx
const result = await claimService.getMyClaims();
if (result.success) {
    setClaims(result.data);
}
setLoading(false);  // No error state set on failure!
```

If the API fails, users see an empty page with no indication of what went wrong and no retry option.

**Fix:** Add error state and display:
```javascript
if (!result.success) {
    setError(result.error);
}
```

---

### H5. IncomingClaims — Race Condition on Approve/Reject
**Severity:** HIGH | **Category:** Bug  
**File:** IncomingClaims.jsx

```javascript
const handleApprove = async (claimId) => {
    const result = await claimService.acceptClaim(claimId);
    if (result.success) {
        const claimsResult = await claimService.getClaimsForItem(itemId);
        if (claimsResult.success) setClaims(claimsResult.data);
    }
};
```

No loading state during submit → user can click approve/reject multiple times rapidly, causing duplicate API calls. If `itemId` changes during the async operation, stale data gets set.

**Fix:**
1. Add `actionLoading` state to disable buttons during API call
2. Use AbortController pattern (like ItemFeed.jsx already does)

---

### H6. ClaimModal — Secret Answer Optional for FOUND Items
**Severity:** HIGH | **Category:** Business Logic  
**File:** ClaimModal.jsx

The modal allows submitting claims on FOUND items without answering the secret detail question, as the backend `ClaimRequest.providedAnswer` lacks `@NotBlank`. The frontend checks are also insufficient.

**Fix:** Frontend should require the answer:
```javascript
const isFormValid = message.trim().length > 0 && 
    (item.type !== 'FOUND' || secretAnswer.trim().length > 0);
```
Plus the backend fix in C3.

---

### H7. UserService — No Email Normalization on Registration
**Severity:** HIGH | **Category:** Data Integrity  
**File:** UserService.java

Email is not normalized (lowercased/trimmed) before storage:
```java
user.setEmail(registrationDTO.getEmail());  // Raw email stored
```

But login and password reset normalize: `email.trim().toLowerCase()`. This could cause:
- Registration with `John@CIT.edu`, login with `john@cit.edu` → "Invalid email or password"
- Duplicate accounts with different case

**Fix:**
```java
user.setEmail(registrationDTO.getEmail().trim().toLowerCase());
```

---

### H8. No Pagination from Frontend Claim Pages
**Severity:** HIGH | **Category:** Performance  
**Files:** MyClaims.jsx, IncomingClaims.jsx, claimService.js

Frontend fetches ALL claims without pagination parameters. Even after the backend is fixed (C1b), the frontend needs to pass `page` and `size`:

```javascript
// claimService.js — needs pagination support
async getMyClaims(page = 0, size = 20) {
    const response = await api.get(`/claims/my?page=${page}&size=${size}`);
    return { success: true, data: response.data };
}
```

Without this, a user with 1000+ claims loads them all simultaneously.

---

### H9. Missing MongoDB Indexes for Claim Queries
**Severity:** HIGH | **Category:** Performance  
**File:** MongoConfig.java

Current indexes cover `(itemId, claimantId)`, `(itemId, status)`, `(itemId, claimantId, status)`.

**Missing indexes:**
- `(claimantId)` alone — used by `findByClaimantId` for MyClaims
- `(finderId)` alone — used by `findByFinderId` for IncomingClaims
- `(claimantId, createdAt)` — for paginated sorted queries

**Fix:** Add to MongoConfig.java:
```java
claimsCollection.createIndex(Indexes.ascending("claimantId"), 
    new IndexOptions().name("idx_claimant"));
claimsCollection.createIndex(Indexes.ascending("finderId"), 
    new IndexOptions().name("idx_finder"));
```

---

## MEDIUM Issues (12)

### M1. Status Fields Use String Instead of Enum Types
**Severity:** MEDIUM | **Category:** Code Quality / Data Integrity  
**Files:** ClaimEntity (status), ItemEntity (status, type), UserEntity (role, accountStatus)

All status/type fields are raw Strings, allowing invalid values at the database level. Only compile-time type safety prevents misuse — and as C1c shows, that safety is already broken.

**Fix:** Convert all to their respective enums: `ClaimStatus`, `ItemStatus`, `ItemType`, `Role`, `AccountStatus`.

---

### M2. GlobalExceptionHandler May Leak Internal Details
**Severity:** MEDIUM | **Category:** Security  
**File:** GlobalExceptionHandler.java

`IllegalArgumentException` and other handlers return `e.getMessage()` directly, which could contain internal paths, query details, or stack information from MongoDB/Spring.

**Fix:** Sanitize messages for non-custom exceptions:
```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
    log.error("Unexpected error", e);
    return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred"));
}
```

---

### M3. Frontend Uses `alert()` for Error Display
**Severity:** MEDIUM | **Category:** UX  
**Files:** IncomingClaims.jsx

```javascript
alert(result.error);  // Blocks main thread, poor UX
```

The app has a Toast notification system but doesn't use it consistently.

**Fix:** Replace all `alert()` calls with toast notifications:
```javascript
import { showError } from '../../components/ui/Toast/toastApi';
showError(result.error);
```

---

### M4. No AbortController in Claim Pages
**Severity:** MEDIUM | **Category:** Bug / Performance  
**Files:** MyClaims.jsx, IncomingClaims.jsx, ClaimDetail.jsx

If the user navigates away while API calls are in flight, `setState` is called on unmounted components. ItemFeed.jsx correctly implements AbortController, but claim pages don't.

**Fix:** Apply the same AbortController pattern from ItemFeed.jsx to all claim pages.

---

### M5. IncomingClaims Shows Secret Question as Plain Text
**Severity:** MEDIUM | **Category:** Security  
**File:** IncomingClaims.jsx

The secret detail question and answer are displayed in plain text:
```jsx
<div className="ic-secret-value yours">{claim.secretDetailQuestion || 'Not set'}</div>
<div className="ic-secret-value theirs">{claim.providedAnswer}</div>
```

While React's JSX auto-escapes HTML, this user-generated content should still be validated server-side to prevent stored XSS if the rendering context ever changes (e.g., `dangerouslySetInnerHTML` added later).

---

### M6. ClaimDetail.jsx Has No Cancel Functionality
**Severity:** MEDIUM | **Category:** Missing Feature  
**File:** ClaimDetail.jsx

The page displays claim details with status messages for PENDING, REJECTED, CANCELLED, and ACCEPTED states. However, there is **no cancel button** for the claimant to cancel their own pending claim, despite `claimService.cancelClaim()` existing.

**Fix:** Add cancel button for pending claims when `isClaimant`:
```jsx
{claim.status === 'PENDING' && isClaimant && (
    <button onClick={handleCancel}>Cancel Claim</button>
)}
```

---

### M7. CloudinaryService Uploads Images Sequentially
**Severity:** MEDIUM | **Category:** Performance  
**File:** CloudinaryService.java

When uploading 3 images, each upload is sequential. With Cloudinary's typical 1-3 second upload time, this adds 3-9 seconds to item creation.

**Fix:** Use parallel upload with CompletableFuture:
```java
List<CompletableFuture<String>> futures = images.stream()
    .map(img -> CompletableFuture.supplyAsync(() -> uploadImage(img)))
    .toList();
return futures.stream()
    .map(CompletableFuture::join)
    .toList();
```

---

### M8. Campus Data Not Cached
**Severity:** MEDIUM | **Category:** Performance  
**Files:** UserService.java, ClaimService.java

Every `convertToDTO()` call queries `campusRepository.findById()`. Campus data rarely changes (8 universities). Should be cached.

**Fix:** Add `@Cacheable` with Spring Cache:
```java
@Cacheable(value = "campuses", key = "#id")
public Optional<CampusEntity> findCampusById(String id) { ... }
```

---

### M9. Frontend Header Uses Wrong Field for User Initials
**Severity:** MEDIUM | **Category:** Bug  
**File:** Header.jsx

The Header component may reference `user.firstName`/`user.lastName` but `authService.getCurrentUser()` returns a user object with `fullName` (not split). This causes initials to display incorrectly.

**Fix:** Use the `fullName` field:
```javascript
const getInitials = (fullName) => {
    if (!fullName) return '?';
    return fullName.split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase();
};
```

---

### M10. No Loading Indicators During Claim Actions
**Severity:** MEDIUM | **Category:** UX  
**Files:** IncomingClaims.jsx, ClaimModal.jsx

No spinner or disabled state on approve/reject/submit buttons during API calls. Users can double-click, causing duplicate requests.

**Fix:** Track `actionLoading` state per claim and disable buttons during API calls.

---

### M11. CampusController Missing @Valid on Create/Update
**Severity:** MEDIUM | **Category:** Input Validation  
**File:** CampusController.java

`@RequestBody CampusDTO` is accepted without `@Valid`, allowing creation of campuses with null/empty names.

**Fix:** Add `@Valid` annotation and validation constraints to CampusDTO.

---

### M12. OTP Verification Without Transaction
**Severity:** MEDIUM | **Category:** Data Integrity  
**File:** UserService.java

OTP verification performs multiple field updates (`passwordResetToken`, `passwordResetExpiry`, `otpAttempts`, `otpVerified`) without transaction wrapping. Concurrent OTP verification attempts could bypass the lockout mechanism.

---

## LOW Issues (6)

### L1. No `@Version` on ClaimEntity for Optimistic Locking
ItemEntity has `@Version` but ClaimEntity does not. Concurrent claim modifications can silently overwrite each other.

### L2. Email Leakage in Password Reset Error Messages
`verifyResetOtp` throws "No account found with that email address" — confirms whether an email is registered. The `requestPasswordReset` method silently returns (good), but `verifyResetOtp` and `resetPassword` leak this information.

### L3. No `@JsonInclude(NON_NULL)` on DTOs
Null fields are serialized as `"field": null` in API responses, increasing payload size unnecessarily.

### L4. No Component Memoization in Item/Claim Lists
`ItemCard` and claim cards re-render on every parent state change. `React.memo` and `useCallback` would reduce renders.

### L5. Hardcoded Fallback Image URL
Multiple components use `'https://picsum.photos/seed/placeholder/400/300'` as fallback. Should be a constant or local placeholder asset.

### L6. EmailService Template Hardcoded
The OTP email template is a hardcoded string in EmailService.java. Should use Thymeleaf or external template for maintainability.

---

## Phase 4 Consistency Check

| Requirement | Status | Notes |
|---|---|---|
| Submit claim with secret detail answer | **BLOCKED** | Code doesn't compile (C1) |
| Finders view incoming claims | **BLOCKED** | Controller→Service signature mismatch (C1b) |
| Accept/Reject claims | **BLOCKED** | String/enum mismatch (C1c) |
| Cancel own pending claim | **PARTIAL** | Backend logic exists, frontend button missing (M6) |
| Duplicate claim prevention | **OK** | Partial unique index in MongoDB + service check |
| Status naming consistency | **BROKEN** | Entity uses String, Service uses Enum (C1c) |
| One active claim per user per item | **OK** | Logic correct, index enforces at DB level |

---

## Recommended Fix Priority

### Immediate (Before any testing)
1. **C1a** — Add Jackson dependency to pom.xml
2. **C1b** — Fix ClaimController method signatures with pagination
3. **C1c** — Convert ClaimEntity.status to ClaimStatus enum
4. **C1d** — Add Pageable overloads to ClaimRepository
5. **C3** — Add @NotBlank to ClaimRequest.providedAnswer

### Before Phase 5
6. **C2** — Add @Transactional to acceptClaim
7. **C6** — Fix rate limiter IP resolution
8. **H7** — Normalize email on registration
9. **H4** — Add error handling to MyClaims/IncomingClaims
10. **H5** — Add loading states to prevent double-click
11. **H6** — Require secret answer in ClaimModal for FOUND items
12. **M6** — Add cancel button to ClaimDetail

### Before Phase 6
13. **C4** — Plan migration to HttpOnly cookie auth OR implement short-lived tokens
14. **C5** — Implement token blacklist
15. **H2** — Strengthen password validation
16. **H3** — Migrate to AuthContext provider
17. **H8** — Add pagination to frontend claim services
18. **H9** — Add missing MongoDB indexes

---

## Summary

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 6 | Must fix before any testing |
| HIGH | 9 | Must fix before Phase 5 |
| MEDIUM | 12 | Fix within next 2 sprints |
| LOW | 6 | Address when convenient |
| **TOTAL** | **33** | |

The most critical finding is that **the backend does not compile** due to multiple type mismatches introduced in Phase 4. The ClaimController, ClaimService, ClaimEntity, and ClaimRepository are all out of sync with each other. Fixing C1 (all sub-items) is the absolute first priority before any further development.
