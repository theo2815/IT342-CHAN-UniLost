# Phase 3 - Item Management (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** Phase 2 (User & Campus Management)
> **Completed:** 2026-03-19

---

## Objective

Implement the core Lost & Found item system, enabling users to report lost items, post found items with "Secret Detail" protection and blurred images, search/filter across all Cebu City campuses, and manage their own listings.

---

## Pre-Existing Work

- **Backend:** `ItemEntity.java` and `ItemRepository.java` already existed (no controller/service)
- **Website:** `ItemFeed.jsx`, `ItemDetail.jsx`, `PostItem.jsx`, `FilterBar.jsx`, `ItemCard.jsx` all built with mock data
- **Mock Data:** `mockData/items.js` provided data structure reference

---

## Backend (Spring Boot) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | `ItemService.java` | DONE | Full business logic: CRUD, search, pagination, batch DTO conversion, ownership checks |
| 2 | `ItemController.java` | DONE | REST endpoints with `@Valid` multipart handling, anonymous-safe `resolveEmail()` |
| 3 | `CloudinaryService.java` | DONE | Image upload/delete with MIME whitelist (JPEG, PNG, GIF, WebP), max 3 images |
| 4 | `CloudinaryConfig.java` | DONE | Bean config from env vars (`CLOUDINARY_CLOUD_NAME`, `API_KEY`, `API_SECRET`) |
| 5 | Blurred image logic | DONE | Frontend-side blur via CSS `.blurred` class for FOUND items |
| 6 | Full-text search | DONE | `MongoTemplate` with `Pattern.quote()` regex on title, description, location |
| 7 | Filters (campus, category, status, type) | DONE | Dynamic `Criteria` building in `searchItems()` |
| 8 | Soft delete | DONE | `isDeleted` + `deletedAt` fields; all queries use `isDeletedFalse` |
| 9 | `WALLETS` category added | DONE | `ItemRequest` `@Pattern` includes all 9 categories |
| 10 | `GlobalExceptionHandler.java` | DONE | `@RestControllerAdvice` for 400/403/404/413/500 |
| 11 | `ItemDTO.java` + `ItemRequest.java` | DONE | Response DTO with resolved reporter/campus; request DTO with Jakarta validation |
| 12 | `UpdateUserRequest.java` | DONE | Dedicated DTO for profile updates (replaces reuse of `RegisterRequest`) |

### API Endpoints (Implemented)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/items` | Public | List items with filters (campus, category, status, keyword, type) + pagination |
| `GET` | `/api/items/{id}` | Public | Get single item detail (secretDetailQuestion hidden for non-owners) |
| `POST` | `/api/items` | Auth | Create new Lost or Found item (multipart: JSON + images) |
| `PUT` | `/api/items/{id}` | Auth | Update item (owner or ADMIN only, orphan image cleanup) |
| `DELETE` | `/api/items/{id}` | Auth | Soft delete item (owner or ADMIN only) |
| `GET` | `/api/items/user/{userId}` | Public | Get all items posted by a specific user (paginated) |
| `GET` | `/api/items/campus/{campusId}` | Public | Get all items for a specific campus (paginated) |

## Website (React + Vite) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Wire `ItemFeed` to API | DONE | Debounced search (400ms), AbortController, paginated load-more |
| 2 | Wire `PostItem` form to API | DONE | Multipart upload with `FormData`, Blob JSON part, image drag-drop |
| 3 | Wire `ItemDetail` to API | DONE | Single item fetch + related items, ownership-based edit/delete |
| 4 | Image upload component | DONE | Drag-drop upload zone, preview thumbnails, max 3 validation |
| 5 | Blurred image display | DONE | CSS blur for FOUND items, "Image protected" label, "Click to reveal" on snapshot |
| 6 | Search & filter functionality | DONE | FilterBar with type chips, category/school dropdowns, server-side filtering |
| 7 | Wire Profile page | DONE | User's items from API with pagination, lost/found tab filtering |
| 8 | Wire Settings page | DONE | Profile update via `PUT /api/users/{id}` using `userService` |
| 9 | `itemService.js` created | DONE | All item API endpoints with `{ success, data, error }` pattern |
| 10 | `categories.js` constants | DONE | Shared category list replacing `mockCategories` import from mock data |

---

## Files Created/Modified

### Backend — New Files
- `dto/ItemDTO.java` — Response DTO with resolved reporter + campus
- `dto/ItemRequest.java` — Validated request DTO (9 categories, LOST/FOUND type)
- `dto/UpdateUserRequest.java` — Dedicated profile update DTO with validation
- `service/ItemService.java` — Business logic with batch N+1 fix, ownership/admin checks
- `service/CloudinaryService.java` — Image upload/delete with MIME validation
- `controller/ItemController.java` — REST controller with anonymous-safe `resolveEmail()`
- `config/CloudinaryConfig.java` — Cloudinary bean configuration
- `config/GlobalExceptionHandler.java` — Centralized `@RestControllerAdvice` error handling

### Backend — Modified Files
- `entity/ItemEntity.java` — Added status, location, imageUrls, dateLostFound, `@Indexed` on reporterId/campusId
- `entity/UserEntity.java` — Added `@JsonIgnore` on passwordHash, passwordResetToken, passwordResetExpiry
- `repository/ItemRepository.java` — Added paginated soft-delete queries
- `config/SecurityConfig.java` — Added item endpoint rules (GET=public, POST/PUT/DELETE=authenticated)
- `service/UserService.java` — Updated `updateUser()` to accept `UpdateUserRequest`
- `controller/UserController.java` — Updated to use `UpdateUserRequest` with `@Valid`
- `pom.xml` — Added `cloudinary-http44:1.36.0`
- `application.properties` — Cloudinary config, multipart limits, auto-index

### Website — New Files
- `services/itemService.js` — Item API service (CRUD + search + user/campus endpoints)
- `services/userService.js` — User profile update service
- `services/campusService.js` — Campus CRUD service
- `constants/categories.js` — Canonical category list matching backend validation
- `utils/timeAgo.js` — Extracted time-ago utility

### Website — Modified Files
- `components/ItemCard.jsx` — Supports both API and mock data shapes
- `components/FilterBar.jsx` — Fetches campuses from API, uses shared category constants
- `pages/PostItem/PostItem.jsx` — Wired to `itemService.createItem()` with multipart upload
- `pages/ItemFeed/ItemFeed.jsx` — Paginated API with debounce, AbortController, server-side filtering
- `pages/ItemDetail/ItemDetail.jsx` — Fetches single + related items from API
- `pages/Dashboard/Dashboard.jsx` — Fetches recent items from API
- `pages/Profile/Profile.jsx` — Fetches user's items from API
- `pages/Settings/Settings.jsx` — Profile update via `userService.updateProfile()`
- `services/api.js` — Base URL from `import.meta.env.VITE_API_URL`
- `services/authService.js` — Safe JSON.parse with try/catch in `getCurrentUser()`

---

## Technical Details

### Item Entity Fields (Actual Implementation)
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `title` | String | Item name/title (max 100 chars) |
| `description` | String | Detailed description (max 1000 chars) |
| `category` | String | `ELECTRONICS`, `WALLETS`, `CLOTHING`, `DOCUMENTS`, `ACCESSORIES`, `BOOKS`, `KEYS`, `BAGS`, `OTHER` |
| `type` | String | `LOST` or `FOUND` |
| `status` | String | `ACTIVE`, `RESOLVED`, `EXPIRED` |
| `campusId` | String | Reference to campus where item was lost/found (`@Indexed`) |
| `reporterId` | String | Reference to the user who posted (`@Indexed`) |
| `location` | String | Specific location description (max 200 chars) |
| `imageUrls` | List\<String\> | Cloudinary URLs for uploaded photos (max 3) |
| `secretDetailQuestion` | String | Verification question (Found items only, max 500 chars) |
| `dateLostFound` | LocalDateTime | When the item was lost or found |
| `isDeleted` | boolean | Soft delete flag (default: false) |
| `deletedAt` | LocalDateTime | Soft delete timestamp |
| `createdAt` | LocalDateTime | Post creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

### Search & Filter Parameters
```
GET /api/items?keyword=laptop&campusId=abc123&category=ELECTRONICS&type=LOST&status=ACTIVE&page=0&size=20
```

### Image Handling
- **Upload:** Multipart form data -> Cloudinary cloud storage
- **Validation:** Server-side MIME whitelist (JPEG, PNG, GIF, WebP), max 3 images, 10MB/file
- **Found Items:** CSS-blurred on frontend for all users except poster
- **Orphan Cleanup:** Old images deleted from Cloudinary when replaced during update

---

## Security Measures

- **Input Validation:** Jakarta `@NotBlank`, `@Size`, `@Pattern` on all `ItemRequest` fields
- **RBAC:** `verifyOwnershipOrAdmin()` on update/delete; ADMIN bypass
- **Secret Detail Protection:** `secretDetailQuestion` stripped from DTOs for non-owner/non-admin
- **Image Validation:** Server-side MIME whitelist, max 3 images, 10MB/file limit
- **Orphan Cleanup:** Old images deleted from Cloudinary before upload replacements
- **XSS Prevention:** `Pattern.quote()` on search keywords
- **Sensitive Field Protection:** `@JsonIgnore` on `passwordHash`, `passwordResetToken`, `passwordResetExpiry`
- **Anonymous Request Safety:** `resolveEmail()` handles null auth and `anonymousUser` principal

---

## Acceptance Criteria

- [x] Users can post Lost and Found items with images
- [x] Found items show blurred images to non-owners
- [x] Items can be searched by keyword across all campuses
- [x] Items can be filtered by campus, category, type, and status
- [x] Users can view, edit, and soft-delete their own items
- [x] All website pages use real API data instead of mock data
- [x] Image upload works end-to-end (frontend -> Cloudinary -> displayed)
- [x] Public browsing of items without authentication
- [x] Proper error handling with GlobalExceptionHandler

---

## Post-Implementation Audit Summary

Two full audit cycles were performed after initial implementation:

**Audit Cycle 1 — 32 fixes implemented:**
All CRITICAL through LOW severity items addressed (validation annotations, N+1 batch fix, error states, AbortController, debounce, orphan cleanup, CORS externalization, env vars, etc.)

**Audit Cycle 2 — Validation rescan (5 additional fixes):**

| ID | Severity | Issue | Resolution |
|----|----------|-------|------------|
| M1 | Medium | `mockCategories` imported from mock data | Created `constants/categories.js` shared constant |
| M2 | Medium | `GET /api/items` required authentication | Changed to `permitAll()` for GET endpoints |
| M3 | Medium | `anonymousUser` principal not handled | Added `resolveEmail()` helper in ItemController |
| L1 | Low | `RegisterRequest` reused for profile updates | Created dedicated `UpdateUserRequest` DTO |
| L4 | Low | `passwordHash` not protected at serialization level | Added `@JsonIgnore` on sensitive entity fields |

**Final Verdict:** PASS — 0 regressions, 0 Critical, 0 High, 0 Medium, 0 Low remaining.
