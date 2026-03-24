# UniLost Mobile Implementation Plan

> **Purpose:** Define a clear, structured strategy for building the Android mobile app before writing any code.
> **Tech Stack:** Kotlin + Jetpack Compose + Material 3 | Retrofit + OkHttp | Hilt DI | Room DB | STOMP WebSocket
> **Backend:** Shared Spring Boot 4 + MongoDB Atlas API (already built, same API as web)

---

## Table of Contents

1. [Current State Assessment](#1-current-state-assessment)
2. [Layout & Navigation Structure](#2-layout--navigation-structure)
3. [Architecture & Project Structure](#3-architecture--project-structure)
4. [Navigation & Screen Flow](#4-navigation--screen-flow)
5. [UI/UX Strategy (Mobile-First)](#5-uiux-strategy-mobile-first)
6. [API Integration Plan](#6-api-integration-plan)
7. [Data Layer & Offline Strategy](#7-data-layer--offline-strategy)
8. [Real-Time Features (WebSocket & Push)](#8-real-time-features-websocket--push)
9. [Phase-by-Phase Implementation](#9-phase-by-phase-implementation)
10. [Mobile-Specific Features](#10-mobile-specific-features)
11. [Testing Strategy](#11-testing-strategy)
12. [Performance & Scalability](#12-performance--scalability)
13. [Risk Assessment & Mitigations](#13-risk-assessment--mitigations)

---

## 2. Layout & Navigation Structure

> This section defines the **global shell** of the app — the persistent top bar and bottom navigation — for both authenticated and guest users. All screens live inside this shell.

---

### Authenticated User Layout

#### Header (Top App Bar — Fixed)

```
┌─────────────────────────────────────────────────────┐
│  🔵 UniLost          [🔔 Bell]  [💬 Message]        │
│  (logo + name → Home)  (badge)    (badge)            │
└─────────────────────────────────────────────────────┘
```

| Element | Position | Behavior |
|---------|----------|----------|
| **Logo + App Name** | Left | Tapping navigates to Home (Dashboard) |
| **Notification Bell** | Right (1st icon) | Shows unread badge count; tapping navigates to Notifications screen |
| **Message Icon** | Right (2nd icon) | Shows unread message badge; tapping navigates to Chat List screen |

**Implementation notes:**
- Badge counts come from `UnreadContext` / WebSocket (same logic as web)
- Both badges hide when count is 0
- The app bar uses `TopAppBar` from Material 3 with `CenterAlignedTopAppBar` or `SmallTopAppBar`
- App bar is present on all authenticated screens **except** PostItem (which uses its own back-navigation toolbar)

---

#### Body

- **Compact, scrollable, touch-friendly** layout for all screens
- All existing web pages adapted to mobile:
  - Single-column card lists (no multi-column grids)
  - Bottom sheets replace dropdowns and modals
  - Larger touch targets (min 48dp)
  - Adequate padding and spacing for thumbs
- Content fills the space between the top bar and bottom nav

---

#### Bottom Navigation (Fixed — 5 Tabs)

```
┌──────────┬──────────┬──────────┬──────────┬──────────┐
│  Explore │   Map    │    ➕    │Leaderboard│ Profile  │
│  📋 Feed │  🗺️ View │ Post Item│   🏆    │   👤    │
└──────────┴──────────┴──────────┴──────────┴──────────┘
```

| Tab | Icon | Destination | Notes |
|-----|------|-------------|-------|
| **Explore** | Feed/list icon | Item Feed screen | Default/home tab for browsing items |
| **Map View** | Map pin icon | Map View screen | Interactive campus map with item markers |
| **➕ Post Item** | Plus icon (elevated/filled) | Post Item screen | Primary CTA — visually distinct (filled circle FAB style) |
| **Leaderboard** | Trophy icon | Leaderboard screen | Top karma scorers |
| **Profile** | Person icon | Profile screen (with menu/drawer for Settings, My Items, My Claims, Logout) | Tapping opens profile; long-press or chevron opens sub-menu |

**Implementation notes:**
- The `➕ Post Item` tab is the **primary CTA** — it should be visually elevated (larger icon, filled background, or FAB-style center button)
- `Profile` tab includes a sub-navigation drawer or inline section links for: My Items, My Claims, Settings, Logout
- Active tab is highlighted with the primary color (`Slate700`)
- The bottom nav persists across all authenticated screens

---

### Guest (Unauthenticated) Layout

#### Header (Top App Bar — Fixed)

```
┌──────────────────────────────────────────────────────┐
│  🔵 UniLost              [Login]  [Register]         │
│  (logo + name → Home)   (text btn) (outlined btn)    │
└──────────────────────────────────────────────────────┘
```

| Element | Position | Behavior |
|---------|----------|----------|
| **Logo + App Name** | Left | Tapping navigates to Home (Landing screen) |
| **Login Button** | Right (1st) | Text button style; navigates to Login screen |
| **Register Button** | Right (2nd) | Outlined button style; navigates to Register screen |

**Implementation notes:**
- No notification or message icons for guests
- If screen width is very narrow, "Register" may collapse to just icon or single "Sign In" button
- Login/Register buttons use compact styling to avoid crowding the header

---

#### Body

- Same **compact, scrollable, touch-friendly** layout
- Guests can browse: Home (Landing), Item Feed, Map View, Leaderboard
- Item actions that require auth (claim, post, chat) show a login prompt or redirect to Login

---

#### Bottom Navigation (Fixed — 3 Tabs, Guest View)

```
┌──────────────────┬──────────────────┬──────────────────┐
│     Explore      │     Map View     │   Leaderboard    │
│     📋 Feed      │     🗺️ View      │      🏆         │
└──────────────────┴──────────────────┴──────────────────┘
```

| Tab | Destination | Notes |
|-----|-------------|-------|
| **Explore** | Item Feed | Public browsing, view-only |
| **Map View** | Map View | Public map, no claim actions |
| **Leaderboard** | Leaderboard | Public leaderboard |

**Implementation notes:**
- `➕ Post Item` tab is **hidden** for guests (posting requires auth)
- `Profile` tab is **hidden** for guests (no profile without account)
- Tapping a guest-restricted action (e.g., claim button on item detail) shows a bottom sheet prompt: _"Sign in to claim this item"_ with Login/Register buttons
- The app detects auth state from `TokenManager`; switching state (login/logout) triggers immediate nav bar rebuild

---

### Auth State Transition

```
Guest State                    Authenticated State
─────────────────              ─────────────────────────────
Header: Logo | Login Register  Header: Logo | 🔔 Bell | 💬 Chat
BottomNav: Feed | Map | Board  BottomNav: Feed | Map | ➕ | Board | Profile
```

- On **Login success** → Rebuild nav shell to authenticated layout, navigate to Home
- On **Logout** → Clear token, rebuild nav shell to guest layout, navigate to Landing/Feed
- On **Token expiry** (401 response) → Automatically rebuild to guest layout + show snackbar "Session expired, please log in"

---

## 1. Current State Assessment

### What Is Already Built (Mobile)

| Layer | Status | Details |
|-------|--------|---------|
| **Project Setup** | DONE | Gradle, Hilt DI, Retrofit, Room, Compose, Navigation |
| **Auth (Login)** | REAL API | `AuthApiService.login()` → JWT stored in DataStore |
| **Auth (Register)** | REAL API | School detection from email domain, form validation |
| **Token Management** | DONE | `TokenManager` with DataStore, `AuthInterceptor` for all requests |
| **Navigation** | DONE | `NavGraph.kt` with 18 screen routes, `BottomNavBar.kt` |
| **Item Feed** | MOCK | 25+ mock items, search & category filtering UI complete |
| **Post Item** | MOCK | Form with type toggle, category, location, secret detail |
| **Item Detail** | MOCK | Full layout with claim submission UI |
| **My Items** | MOCK | Status tabs, statistics cards |
| **My Claims** | MOCK | Status filtering, handover progress |
| **Claim Detail** | MOCK | Handover workflow UI |
| **Notifications** | MOCK | 11 mock notifications, type filtering, unread badges |
| **Profile** | MOCK | Basic display with hardcoded "Theo Chan" |
| **Settings** | PARTIAL | Screen shell exists |
| **Room DB** | PARTIAL | Setup done, only `ExampleEntity` exists |

### What the Backend Provides (Already Built)

The mobile app connects to the **same Spring Boot API** as the web frontend:
- **72+ REST endpoints** across Auth, Users, Items, Claims, Chats, Notifications, Campus (Admin endpoints are desktop-only, out of scope for mobile)
- **WebSocket STOMP** for real-time chat messages and notifications (`/ws` endpoint)
- **JWT authentication** (HS256, 1-hour expiry, email-based)
- **Rate limiting**: Auth 10/min, Write 30/min, Read 120/min per IP
- **Cloudinary** image hosting (up to 3 images per item, 10MB each)
- **MongoDB Atlas** as the database (no mobile-side schema changes needed)

### Key Gaps to Fill

1. **API Services** — Only `AuthApiService` exists. Need services for Items, Claims, Chats, Notifications, Users, Campuses
2. **ViewModels** — Only Login/Register have proper ViewModels. All other screens use inline state
3. **Repository Pattern** — Only `AuthRepository` exists. Need repositories for each domain
4. **Models** — Only `User`, `School`, `AuthResponse` exist. Need Item, Claim, Chat, Message, Notification, Campus DTOs
5. **WebSocket Client** — No STOMP client implemented yet
6. **Image Upload** — No Cloudinary/multipart upload support
7. **Map Integration** — No Google Maps dependency or implementation
8. **Push Notifications** — Firebase dependencies commented out
9. **Offline Caching** — Room entities not defined for core data

---

## 3. Architecture & Project Structure

### Architectural Pattern: MVVM + Repository + Clean Architecture Layers

```
┌─────────────────────────────────────────────────┐
│                  UI Layer                        │
│  Screens (Compose) ← ViewModels (StateFlow)     │
├─────────────────────────────────────────────────┤
│                Domain Layer                      │
│  Repositories (interface + impl)                 │
├─────────────────────────────────────────────────┤
│                Data Layer                        │
│  Remote (Retrofit API)  |  Local (Room DB)       │
│  TokenManager (DataStore)                        │
└─────────────────────────────────────────────────┘
```

### Proposed Package Structure

```
com.hulampay.mobile/
├── di/                          # Hilt modules (existing)
│   ├── NetworkModule.kt         # Retrofit, OkHttp (existing)
│   ├── DatabaseModule.kt        # Room (existing)
│   └── AppModule.kt             # App-level bindings (existing)
│
├── data/
│   ├── api/                     # Retrofit service interfaces
│   │   ├── AuthApiService.kt    # (existing)
│   │   ├── ItemApiService.kt    # NEW
│   │   ├── ClaimApiService.kt   # NEW
│   │   ├── ChatApiService.kt    # NEW
│   │   ├── NotificationApiService.kt  # NEW
│   │   ├── UserApiService.kt    # NEW
│   │   └── CampusApiService.kt  # NEW
│   │
│   ├── model/                   # Data models / DTOs
│   │   ├── User.kt              # (existing — needs field alignment)
│   │   ├── AuthResponse.kt      # (existing)
│   │   ├── School.kt            # (existing)
│   │   ├── ItemDTO.kt           # NEW
│   │   ├── ItemRequest.kt       # NEW
│   │   ├── ClaimDTO.kt          # NEW
│   │   ├── ClaimRequest.kt      # NEW
│   │   ├── ChatDTO.kt           # NEW
│   │   ├── MessageDTO.kt        # NEW
│   │   ├── NotificationDTO.kt   # NEW
│   │   ├── CampusDTO.kt         # NEW
│   │   └── PageResponse.kt      # NEW (generic paginated response wrapper)
│   │
│   ├── local/                   # Room entities & DAOs
│   │   ├── entity/
│   │   │   ├── CachedItem.kt    # NEW
│   │   │   ├── CachedChat.kt    # NEW
│   │   │   └── CachedNotification.kt  # NEW
│   │   └── dao/
│   │       ├── ItemDao.kt       # NEW
│   │       ├── ChatDao.kt       # NEW
│   │       └── NotificationDao.kt  # NEW
│   │
│   ├── repository/              # Repository implementations
│   │   ├── AuthRepository.kt    # (existing)
│   │   ├── ItemRepository.kt    # NEW
│   │   ├── ClaimRepository.kt   # NEW
│   │   ├── ChatRepository.kt    # NEW
│   │   ├── NotificationRepository.kt  # NEW
│   │   ├── UserRepository.kt    # NEW
│   │   └── CampusRepository.kt  # NEW
│   │
│   ├── remote/                  # (existing — keep AuthInterceptor, TokenManager)
│   └── mock/                    # (existing — remove progressively as API integration completes)
│
├── navigation/
│   ├── Screen.kt                # (existing — extend as needed)
│   └── NavGraph.kt              # (existing — extend as needed)
│
├── ui/
│   ├── theme/                   # (existing — Color.kt, Theme.kt, Type.kt)
│   ├── components/              # Shared UI components
│   │   ├── AuthComponents.kt    # (existing)
│   │   ├── BottomNavBar.kt      # (existing — enhance)
│   │   ├── ItemCard.kt          # NEW (extract from ItemFeedScreen)
│   │   ├── ClaimStatusChip.kt   # NEW
│   │   ├── ImageCarousel.kt     # NEW
│   │   ├── LoadingState.kt      # NEW
│   │   ├── ErrorState.kt        # NEW
│   │   ├── EmptyState.kt        # NEW
│   │   ├── PullToRefresh.kt     # NEW
│   │   └── SearchBar.kt         # NEW
│   │
│   └── screens/                 # (existing — refactor to use ViewModels)
│       ├── auth/                # Login, Register (existing)
│       ├── feed/                # ItemFeed + ViewModel
│       ├── item/                # ItemDetail, PostItem, MyItems + ViewModels
│       ├── claim/               # MyClaims, ClaimDetail + ViewModels
│       ├── chat/                # Messages (NEW screen) + ViewModel
│       ├── map/                 # MapView (NEW screen) + ViewModel
│       ├── notification/        # Notifications + ViewModel
│       └── profile/             # Profile, Settings + ViewModels
│
└── util/
    ├── DateUtils.kt             # NEW (time ago formatting, ISO parsing)
    ├── ImageUtils.kt            # NEW (multipart file creation)
    └── WebSocketManager.kt      # NEW (STOMP client singleton)
```

### Key Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| State Management | `StateFlow` in ViewModels | Already used in Login/Register; consistent with Compose |
| DI Framework | Hilt (existing) | Already set up, well-integrated with Compose navigation |
| Networking | Retrofit + OkHttp (existing) | Already configured with auth interceptor |
| Image Loading | Glide (existing) | Already in dependencies; handles Cloudinary URLs |
| Local Cache | Room (existing setup) | Already configured, just needs entities |
| WebSocket | Krossbow (STOMP) or raw OkHttp WebSocket | See Section 7 for detailed comparison |
| Maps | Google Maps Compose | Native Android, same provider as web |
| Push Notifications | Firebase Cloud Messaging | Firebase deps already in build.gradle (commented) |

---

## 4. Navigation & Screen Flow

### Bottom Navigation Bar

**Authenticated (5 Tabs):**
```
┌──────────┬──────────┬──────────┬──────────┬──────────┐
│  Explore │   Map    │    ➕    │Leaderboard│ Profile  │
│  📋 Feed │  🗺️ View │ Post Item│   🏆    │   👤    │
└──────────┴──────────┴──────────┴──────────┴──────────┘
```

| Tab | Screen | Description |
|-----|--------|-------------|
| **Explore** | ItemFeed | Search + filter items, scrollable item cards |
| **Map View** | MapView | Interactive campus map with item markers |
| **➕ Post Item** | PostItem | Primary CTA — elevated center button, create lost/found report |
| **Leaderboard** | Leaderboard | Top karma scorers, campus filter |
| **Profile** | Profile → Settings / My Items / My Claims / Logout | User info, karma, sub-navigation menu |

**Guest (3 Tabs):**
```
┌──────────────────┬──────────────────┬──────────────────┐
│     Explore      │     Map View     │   Leaderboard    │
│     📋 Feed      │     🗺️ View      │      🏆         │
└──────────────────┴──────────────────┴──────────────────┘
```

### Screen Navigation Map

```
                        ┌─── Splash/Auth ───┐
                        │                    │
                     Login ←──→ Register
                        │         │
                  ForgotPassword   │
                        │         │
                   VerifyOTP      │
                        │         │
                  ResetPassword   │
                        └────┬────┘
                             │ (authenticated)
                             ▼
                    ┌── Bottom Nav ──┐
                    │                │
     ┌──────────────┼──────┬────────┼──────────────┐
     │              │      │        │              │
  Dashboard    ItemFeed  PostItem  ChatList     Profile
     │              │               │              │
     │         ItemDetail      ChatDetail      Settings
     │           │    │                         │   │
     │     ClaimModal  Edit                  MyItems │
     │                                         │    │
     │                                    MyClaims  ChangePassword
     │                                         │
     │                                    ClaimDetail
     │
     └── MapView (accessible from Dashboard or Feed)

     Notification Bell (top bar) → NotificationsScreen
```

> **Note:** Admin functionality is **out of scope for mobile**. Admin tasks (moderation, user management, campus stats) are desktop-only via the web app.

### Navigation Behavior

| Action | Behavior |
|--------|----------|
| Bottom tab switch | Replace current stack, preserve scroll position |
| Back press on root tab | Exit app (or switch to Home first) |
| Deep link from notification | Navigate directly to Claim/Chat/Item |
| Auth token expired | Clear backstack, navigate to Login |
| Post Item from any screen | Open PostItem as modal/fullscreen over current tab |

---

## 5. UI/UX Strategy (Mobile-First)

### Design Principles

1. **Thumb-friendly** — Primary actions within bottom 60% of screen; swipe gestures for common actions
2. **Information density** — Show just enough on cards; progressive disclosure on detail screens
3. **Visual hierarchy** — Bold titles, muted metadata, color-coded status badges
4. **Fast feedback** — Skeleton loaders for lists, optimistic UI for sends, pull-to-refresh everywhere
5. **Consistency with web** — Same color language (Slate palette), same status colors, same category icons

### Mobile vs Web UX Differences

| Feature | Web | Mobile Adaptation |
|---------|-----|-------------------|
| **Navigation** | Top header with links | Bottom nav bar (5 tabs) |
| **Item Feed** | Grid layout (3 columns) | Single-column card list (scrollable) |
| **Filters** | Horizontal filter bar | Collapsible filter sheet (bottom sheet) |
| **Map View** | Full-page map | Full-screen map with floating filter chips |
| **Chat** | Split panel (list + thread) | Two separate screens (list → detail) |
| **Post Item** | Single long form | Multi-step wizard (type → details → location → images → review) |
| **Image Upload** | Drag & drop | Camera capture + gallery picker |
| **Notifications** | Dropdown + full page | Push notification banners + full page |
| **Item Detail** | Side-by-side layout | Stacked: images → info → claim CTA (sticky bottom) |
| **Settings** | Tab layout | Section-based list (clickable rows) |
| **Admin** | Full web dashboard | **Out of scope — desktop only** |

### Key UI Components to Build

| Component | Description | Used In |
|-----------|-------------|---------|
| `ItemCard` | Compact card: thumbnail, title, category chip, school badge, time ago, type indicator (LOST=red, FOUND=green) | Feed, Dashboard, MyItems |
| `ImageCarousel` | Swipeable image pager with dots indicator | ItemDetail, PostItem preview |
| `StatusBadge` | Color-coded chip: ACTIVE(blue), CLAIMED(orange), RETURNED(green), EXPIRED(gray), etc. | ItemDetail, MyItems, Claims |
| `ClaimActionBar` | Sticky bottom bar with context-aware buttons (Submit Claim / Accept / Reject / Mark Returned / Confirm Received) | ItemDetail, ClaimDetail |
| `ChatBubble` | Left/right aligned message bubble, timestamp, read receipt | ChatDetail |
| `SystemMessage` | Centered gray message for claim events (accepted, rejected, handover) | ChatDetail |
| `NotificationRow` | Icon + title + body + time ago + unread dot | Notifications |
| `EmptyState` | Illustration + message + CTA button | Any empty list |
| `PullToRefresh` | Swipe-down refresh indicator | All list screens |
| `FilterBottomSheet` | Bottom sheet with category, type, campus selectors | ItemFeed, MapView |
| `LocationPickerSheet` | Google Maps bottom sheet for picking item location | PostItem |
| `SkeletonLoader` | Animated shimmer placeholder matching card/list layout | All data screens |

### Color System (Aligned with Web)

```
Primary:       Slate700  #334155  (app bar, buttons, links)
Secondary:     Sage      #84a98c  (karma, success states)
Background:    Slate100  #f1f5f9  (screen background)
Surface:       White     #FFFFFF  (cards, sheets)
Text Primary:  Slate800  #1e293b
Text Muted:    Slate400  #94a3b8
Error/Lost:    Red500    #ef4444  (LOST items, errors, destructive)
Found:         Green500  #22c55e  (FOUND items, success)
Warning:       Amber500  #f59e0b  (pending, flagged)
Info:          Blue500   #3b82f6  (active, info badges)
```

### Typography

- **Headings:** Bold, 20-24sp
- **Card titles:** SemiBold, 16sp
- **Body text:** Regular, 14sp
- **Captions/metadata:** Regular, 12sp, muted color
- **Badges/chips:** Medium, 11sp

---

## 6. API Integration Plan

### Base URL Configuration

```kotlin
// Current: "http://10.0.2.2:8080/api/" (emulator localhost)
// Production: Configure via BuildConfig or gradle flavor
// IMPORTANT: Backend CORS must include mobile origins (or mobile doesn't need CORS since it's not a browser)
```

> **Note:** Mobile apps make direct HTTP requests, so CORS does not apply. The existing JWT auth flow works identically.

### Retrofit Service Interfaces

Below is every API service needed, mapped to the backend endpoints:

#### ItemApiService.kt
```
POST   /items                         → createItem(FormData)          [multipart]
GET    /items                         → searchItems(params)           [paginated]
GET    /items/map                     → getMapItems(campusId, type, limit)
GET    /items/{id}                    → getItemById(id)
PUT    /items/{id}                    → updateItem(id, FormData)      [multipart]
DELETE /items/{id}                    → deleteItem(id)
GET    /items/user/{userId}           → getItemsByUser(userId, page, size)
GET    /items/campus/{campusId}       → getItemsByCampus(campusId)
POST   /items/{id}/flag               → flagItem(id, reason)
```

#### ClaimApiService.kt
```
POST   /claims                        → submitClaim(ClaimRequest)
GET    /claims/my                     → getMyClaims(page, size)
GET    /claims/incoming               → getIncomingClaims(page, size)
GET    /claims/item/{itemId}          → getClaimsForItem(itemId, page, size)
GET    /claims/{id}                   → getClaimById(id)
PUT    /claims/{id}/accept            → acceptClaim(id)
PUT    /claims/{id}/reject            → rejectClaim(id)
PUT    /claims/{id}/cancel            → cancelClaim(id)
PUT    /claims/{id}/mark-returned     → markItemReturned(id)
PUT    /claims/{id}/confirm-received  → confirmItemReceived(id)
PUT    /claims/{id}/dispute-handover  → disputeHandover(id)
```

#### ChatApiService.kt
```
GET    /chats                         → getMyChats()
GET    /chats/paged                   → getMyChatsPagedchats(page, size)
GET    /chats/{chatId}                → getChatById(chatId)
GET    /chats/{chatId}/messages       → getMessages(chatId, page, size)
POST   /chats/{chatId}/messages       → sendMessage(chatId, content)
PUT    /chats/{chatId}/read           → markAsRead(chatId)
GET    /chats/unread-count            → getUnreadCount()
```

#### NotificationApiService.kt
```
GET    /notifications                 → getNotifications(page, size)
GET    /notifications/unread/count    → getUnreadCount()
PUT    /notifications/{id}/read       → markAsRead(id)
PUT    /notifications/read-all        → markAllAsRead()
DELETE /notifications/{id}            → deleteNotification(id)
```

#### UserApiService.kt
```
GET    /users/{id}                    → getUserById(id)
GET    /users/leaderboard             → getLeaderboard(size, campusId)
PUT    /users/{id}                    → updateProfile(id, UpdateUserRequest)
POST   /users/{id}/profile-picture    → uploadProfilePicture(id, file)  [multipart]
PUT    /users/{id}/change-password    → changePassword(id, ChangePasswordRequest)
DELETE /users/{id}                    → deleteAccount(id)
```

#### CampusApiService.kt
```
GET    /campuses                      → getAllCampuses()
GET    /campuses/stats                → getCampusStats()
GET    /campuses/{id}                 → getCampusById(id)
GET    /campuses/domain/{domain}      → getCampusesByDomain(domain)
```

> **Out of scope:** Admin API endpoints (`/api/admin/**`) are not consumed by the mobile app. Admin tasks are desktop-only via the web app.

### Model Alignment

The existing `User.kt` model has fields that don't match the backend's `UserDTO`:

| Current Mobile `User.kt` | Backend `UserDTO` | Action |
|--------------------------|-------------------|--------|
| `userId` | `id` | Rename |
| `firstName`, `lastName` | `fullName` | Merge to single field |
| `phone`, `address` | Not in backend | Remove |
| `studentIdNumber` | Not in backend | Remove |
| `school` (String) | `universityTag` + `campus` (CampusDTO) | Restructure |
| `isVerified`, `isBanned` | `accountStatus` (enum) | Replace |
| Missing | `profilePictureUrl` | Add |
| Missing | `karmaScore` | Add |
| Missing | `role` (enum) | Add |
| Missing | `createdAt` | Add |

**Decision:** Create a new `UserDTO.kt` that exactly mirrors the backend response. Keep the old `User.kt` temporarily for auth backward compatibility, then migrate.

### Multipart Image Upload Strategy

For `POST /items` and `POST /users/{id}/profile-picture`:

```kotlin
// Approach: Use Retrofit @Multipart
@Multipart
@POST("items")
suspend fun createItem(
    @Part("item") item: RequestBody,  // JSON part
    @Part images: List<MultipartBody.Part>  // Image files
): Response<ItemDTO>
```

Image sources on mobile:
1. **Camera capture** → Compress to JPEG, max 1920px width, <5MB
2. **Gallery picker** → Read URI, compress if needed
3. Use Android's `ActivityResultContracts.TakePicture` and `GetMultipleContents`

---

## 7. Data Layer & Offline Strategy

### Caching Strategy (Room DB)

| Data | Cache? | TTL | Rationale |
|------|--------|-----|-----------|
| **Campus list** | Yes | 24 hours | Rarely changes, needed for filters |
| **Item feed** | Yes | 5 minutes | Show stale data while refreshing |
| **Item detail** | Yes | 5 minutes | Allow offline viewing of recently opened items |
| **Chat list** | Yes | 1 minute | Frequently changes; show stale briefly |
| **Messages** | Yes | Permanent (per chat) | Allow reading old messages offline |
| **Notifications** | Yes | 5 minutes | Show recently fetched notifications |
| **User profile** | Yes | 10 minutes | Own profile, occasionally changes |
| **Leaderboard** | No | — | Always fetch fresh |
| **Claims** | Yes | 2 minutes | Show recent state |

### Room Entities (Key Ones)

```
CachedCampus:     id, universityCode, campusName, name, shortLabel, address, domainWhitelist, lat, lng, lastFetched
CachedItem:       id, title, description, type, status, category, location, lat, lng, imageUrls(JSON), reporterId, campusId, createdAt, lastFetched
CachedChat:       id, itemId, itemTitle, otherParticipantName, lastMessagePreview, lastMessageAt, unreadCount, lastFetched
CachedMessage:    id, chatId, senderId, senderName, content, type, isRead, createdAt
CachedNotification: id, type, title, message, linkId, isRead, createdAt, lastFetched
```

### Data Flow Pattern

```
Screen Request
      │
      ▼
  ViewModel
      │
      ▼
  Repository ──── Cache hit? ──→ Return cached + trigger refresh
      │                              │
      │ (cache miss or expired)      │
      ▼                              ▼
  API Service ──→ Save to Room ──→ Emit updated data via Flow
```

**Implementation:** Use `NetworkBoundResource` pattern:
1. Emit cached data immediately (if available)
2. Fetch from network in background
3. Save network response to Room
4. Emit fresh data from Room (single source of truth)

### Pagination Strategy

Backend returns `Page<T>` with `content`, `totalElements`, `totalPages`, `number`, `size`.

Mobile approach:
- Use Jetpack **Paging 3** library with `PagingSource` backed by Retrofit
- Item Feed, My Claims, Incoming Claims, Notifications, Messages all use pagination
- Default page size: 20 items (matches backend default)
- Show shimmer placeholders while loading next page

---

## 8. Real-Time Features (WebSocket & Push)

### WebSocket (In-App Real-Time)

The backend uses **STOMP over SockJS** at `/ws`. Mobile needs a STOMP client.

**Recommended Library:** `org.hildan.krossbow:krossbow-stomp-core` + `krossbow-websocket-okhttp`

**Why Krossbow over raw OkHttp WebSocket:**
- Native STOMP frame support (CONNECT, SUBSCRIBE, SEND, MESSAGE)
- Kotlin coroutines integration
- Automatic reconnection
- Handles STOMP heartbeats

**WebSocket Connection Flow:**
```
1. User authenticates → JWT token available
2. App enters foreground → WebSocketManager.connect(token)
3. STOMP CONNECT with Authorization header
4. Subscribe to:
   - /user/queue/notifications  → Update notification badge
   - /user/queue/messages       → Update message badge + active chat
5. On specific chat screen → Subscribe to /topic/chat/{chatId}
6. App enters background → Disconnect (save battery)
7. Token refresh → Reconnect with new token
```

**WebSocketManager Singleton:**
```
WebSocketManager
├── connect(token: String)
├── disconnect()
├── subscribeNotifications() → Flow<NotificationDTO>
├── subscribeMessages() → Flow<MessageDTO>
├── subscribeChatTopic(chatId: String) → Flow<MessageDTO>
├── sendMessage(chatId: String, content: String)
└── connectionState: StateFlow<ConnectionState>
```

**Fallback:** If WebSocket fails, poll every 30 seconds for:
- `GET /chats/unread-count`
- `GET /notifications/unread/count`

### Push Notifications (Background/Killed App)

**Implementation: Firebase Cloud Messaging (FCM)**

> **Backend Change Required:** The backend currently has no FCM integration. This is a new backend feature needed for mobile.

**Required Backend Changes:**
1. Add `fcmToken` field to `UserEntity`
2. Add `POST /api/users/{id}/fcm-token` endpoint to register device token
3. Integrate Firebase Admin SDK to send push notifications
4. Send FCM push when creating notifications (in `NotificationService`)

**Mobile FCM Flow:**
```
1. App startup → Request notification permission (Android 13+)
2. Get FCM token from FirebaseMessaging
3. POST /api/users/{id}/fcm-token to register with backend
4. On token refresh → Re-register
5. Receive push in FirebaseMessagingService:
   - If app in foreground: Ignore (WebSocket handles it)
   - If app in background/killed: Show system notification
6. User taps notification → Deep link to relevant screen
```

**Push Notification Types:**
| Type | Title | Action on Tap |
|------|-------|---------------|
| CLAIM_RECEIVED | "New claim on your item" | → ClaimDetail |
| CLAIM_ACCEPTED | "Your claim was accepted!" | → ChatDetail |
| CLAIM_REJECTED | "Claim update" | → ClaimDetail |
| NEW_MESSAGE | "{senderName} sent a message" | → ChatDetail |
| HANDOVER_REQUEST | "Confirm item handover" | → ClaimDetail |
| ITEM_FLAGGED | "Your item was flagged" | → ItemDetail |

**Deep Linking:**
```
unilost://claim/{claimId}     → ClaimDetail
unilost://chat/{chatId}       → ChatDetail
unilost://item/{itemId}       → ItemDetail
unilost://notifications       → NotificationsScreen
```

---

## 9. Phase-by-Phase Implementation

### Phase 3: Item Management (Estimated: ~5 days)

**Goal:** Replace all mock item data with real API calls.

**Tasks:**

1. **Models**
   - Create `ItemDTO.kt`, `ItemRequest.kt`, `CampusDTO.kt`, `PageResponse.kt`
   - Align `UserDTO.kt` with backend response

2. **API Layer**
   - Create `ItemApiService.kt` (all 9 endpoints)
   - Create `CampusApiService.kt` (4 public endpoints)
   - Register in Hilt `NetworkModule`

3. **Repository Layer**
   - Create `ItemRepository.kt` (wraps API + Room cache)
   - Create `CampusRepository.kt` (wraps API + Room cache)

4. **Room Cache**
   - Define `CachedItem` and `CachedCampus` entities
   - Create DAOs with insert/query/delete operations
   - Update `AppDatabase` with new entities

5. **ViewModels**
   - `ItemFeedViewModel` — search, filter, paginate items
   - `ItemDetailViewModel` — load item, related items
   - `PostItemViewModel` — form state, validation, image upload, create/update
   - `MyItemsViewModel` — user's items with status filtering

6. **Screen Updates**
   - `ItemFeedScreen` — Remove MockItems, bind to ViewModel, add pull-to-refresh, pagination
   - `ItemDetailScreen` — Remove mock data, load real item, wire claim button
   - `PostItemScreen` — Wire form to API, implement image picker (camera + gallery)
   - `MyItemsScreen` — Load from API, real statistics

7. **Image Upload**
   - Implement camera capture with `ActivityResultContracts.TakePicture`
   - Implement gallery picker with `ActivityResultContracts.GetMultipleContents`
   - Image compression utility (max 1920px, JPEG quality 80%)
   - Multipart upload via Retrofit

8. **Campus Integration**
   - Fetch campuses on app start, cache in Room
   - Use in filter dropdowns across Feed, MapView, PostItem

**Dependencies:** Phase 2 (Auth + Campus setup) already completed.

---

### Phase 4: Claim & Verification (Estimated: ~4 days)

**Goal:** Wire claim submission, review, and handover workflow to real API.

**Tasks:**

1. **Models**
   - Create `ClaimDTO.kt`, `ClaimRequest.kt`

2. **API + Repository**
   - Create `ClaimApiService.kt` (11 endpoints)
   - Create `ClaimRepository.kt`

3. **ViewModels**
   - `ClaimModalViewModel` — submit claim with message + secret answer
   - `MyClaimsViewModel` — paginated list, status filtering
   - `IncomingClaimsViewModel` — claims on user's items
   - `ClaimDetailViewModel` — claim state, handover actions

4. **Screen Updates**
   - `ItemDetailScreen` — Wire "Submit Claim" with real API, show existing claim status
   - `MyClaimsScreen` — Remove MockClaims, real pagination
   - `ClaimDetailScreen` — Real handover workflow (mark-returned → confirm-received → dispute)
   - Add `IncomingClaimsScreen` (accessible from MyItems → "View Claims")

5. **Claim Flow Logic**
   - LOST items: Auto-accept on claim submission, redirect to chat
   - FOUND items: Show secret question, submit answer, wait for finder review
   - Handover: Dual-confirmation (finder marks returned → owner confirms received)

**Dependencies:** Phase 3 (Items must be loaded from real API to claim them).

---

### Phase 5: Messaging (Estimated: ~5 days)

**Goal:** Implement real-time chat with WebSocket + REST fallback.

**Tasks:**

1. **Dependencies**
   - Add Krossbow STOMP library to `build.gradle`

2. **Models**
   - Create `ChatDTO.kt`, `MessageDTO.kt`

3. **API + Repository**
   - Create `ChatApiService.kt` (7 endpoints)
   - Create `ChatRepository.kt` (with Room cache for messages)

4. **WebSocket Layer**
   - Implement `WebSocketManager` singleton (Hilt @Singleton)
   - STOMP CONNECT with JWT auth header
   - Subscribe/unsubscribe lifecycle management
   - Auto-reconnect with exponential backoff
   - Fallback polling for unread counts

5. **Room Cache**
   - `CachedChat` and `CachedMessage` entities
   - Persist messages for offline reading
   - Sync strategy: fetch latest, merge with cached

6. **ViewModels**
   - `ChatListViewModel` — all chats, unread counts, real-time updates
   - `ChatDetailViewModel` — messages, send, mark read, real-time incoming

7. **New Screens**
   - `ChatListScreen` — List of chats with preview, unread badge, other participant name
   - `ChatDetailScreen` — Message bubbles (left/right), system messages, input bar, scroll-to-bottom
   - Both accessible from Messages tab in bottom nav

8. **System Messages**
   - Render CLAIM_SUBMISSION, CLAIM_ACCEPTED, CLAIM_REJECTED, HANDOVER_REQUEST, HANDOVER_CONFIRMED, HANDOVER_DISPUTED as centered system messages with distinct styling

9. **Unread Badge Integration**
   - WebSocket pushes update badge count on BottomNavBar
   - Active chat suppresses badge increment (like web's `setActiveChatForBadge`)

**Dependencies:** Phase 4 (Claims create chats automatically).

---

### Phase 6: Handover & Karma (Estimated: ~2 days)

**Goal:** Complete the handover confirmation flow and karma display.

**Tasks:**

1. **Handover Flow in ClaimDetail**
   - Finder: "Mark as Returned" button → `PUT /claims/{id}/mark-returned`
   - Owner: "Confirm Received" button → `PUT /claims/{id}/confirm-received`
   - Either: "Dispute" button → `PUT /claims/{id}/dispute-handover`
   - Status transitions reflected in real-time (via chat system messages)

2. **Karma Integration**
   - Display karma score on Profile screen (from UserDTO.karmaScore)
   - Leaderboard screen wired to `GET /users/leaderboard`
   - Campus-specific leaderboard filtering
   - Karma animation on score change (e.g., confetti or score-up indicator)

3. **Item Status Updates**
   - After handover completes, item status → RETURNED
   - Reflect in MyItems, ItemDetail, Feed

**Dependencies:** Phase 4 (Claims) and Phase 5 (Chat for handover messages).

---

### Phase 7: Campus Maps (Estimated: ~4 days)

**Goal:** Implement interactive Google Maps view for browsing items by location.

**Tasks:**

1. **Dependencies**
   - Add `com.google.maps.android:maps-compose` library
   - Add Google Maps API key in `AndroidManifest.xml` (same key as web, or separate Android key)

2. **ViewModel**
   - `MapViewModel` — fetch map items, user location, campus filtering

3. **MapView Screen**
   - Full-screen Google Map centered on Cebu (10.3157, 123.8854)
   - Custom markers: Red pin for LOST, Green pin for FOUND
   - Marker clustering for dense areas
   - Tap marker → Info window with item title, category, time ago
   - Tap info window → Navigate to ItemDetail
   - Floating filter chips: Type (All/Lost/Found), Campus dropdown

4. **User Location**
   - Request `ACCESS_FINE_LOCATION` permission
   - Show user's blue dot on map
   - "Center on me" FAB button

5. **Location Picker (for PostItem)**
   - Reuse map component with draggable pin
   - Long-press to drop pin
   - Reverse geocode to show address
   - Return lat/lng to PostItem form

6. **Campus Quick Zoom**
   - Tap campus in filter → Zoom to campus `centerCoordinates` at zoom level 16

**Dependencies:** Phase 3 (Items with lat/lng from API).

---

### Phase 8: Push Notifications (Estimated: ~4 days)

**Goal:** Firebase push notifications + in-app notification center.

**Tasks:**

1. **Firebase Setup**
   - Uncomment Firebase dependencies in `build.gradle`
   - Add `google-services.json` to `/app` directory
   - Initialize Firebase in `Application` class

2. **FCM Service**
   - Implement `UniLostFirebaseMessagingService`
   - Handle token refresh → POST to backend
   - Build notification channels (Claims, Messages, Items)
   - Show system notifications when app is in background

3. **Backend Integration (Coordination Required)**
   - Backend must add FCM token storage + push sending
   - OR: Use a separate push notification service (e.g., Firebase Functions triggered by backend webhook)

4. **In-App Notifications**
   - `NotificationApiService.kt` (5 endpoints)
   - `NotificationRepository.kt` with Room cache
   - `NotificationsViewModel` — paginated, filtered, mark read
   - Update `NotificationsScreen` — remove mock data, real pagination
   - Notification bell badge in top app bar (count from WebSocket or polling)

5. **Deep Linking**
   - Configure intent filters in `AndroidManifest.xml`
   - Handle `unilost://` scheme in NavGraph
   - Map notification tap → appropriate screen

6. **Permission Handling**
   - Android 13+ (`POST_NOTIFICATIONS` permission)
   - Show rationale dialog explaining why notifications matter
   - Graceful fallback if denied (in-app badge still works)

**Dependencies:** Phase 6 (WebSocket for in-app real-time) + Backend FCM integration.

---

### Phase 9: Testing & Deployment (Estimated: ~5 days)

**Goal:** Quality assurance, performance optimization, and release.

**Tasks:**

1. **Unit Tests**
   - ViewModel tests with fake repositories
   - Repository tests with mock API responses
   - Utility function tests (date formatting, image compression)

2. **Integration Tests**
   - API service tests against real backend (staging)
   - Room DAO tests with in-memory database
   - WebSocket connection/subscription tests

3. **UI Tests**
   - Compose UI tests for critical flows:
     - Login → Dashboard
     - Browse Feed → Item Detail → Submit Claim
     - Post Item with images
     - Chat send/receive messages
   - Screenshot tests for key screens

4. **Performance Optimization**
   - Image loading: Glide disk cache, thumbnail placeholders
   - List performance: LazyColumn keys, stable items
   - Memory: Leak detection with LeakCanary
   - Network: Response caching headers, minimize redundant calls
   - Startup: Lazy initialization of non-critical services

5. **Security Hardening**
   - ProGuard/R8 code shrinking and obfuscation
   - Certificate pinning for API calls (optional, high security)
   - Secure DataStore for tokens (encrypted shared preferences)
   - Input sanitization on all user inputs

6. **Build & Release**
   - Configure release signing keystore
   - Set up build variants (debug, staging, release)
   - Version code/name management
   - Generate signed APK and AAB for Play Store
   - Create Play Store listing (screenshots, description)

---

## 10. Mobile-Specific Features

### Camera Integration (PostItem)

```
User taps "Add Photo"
      │
      ▼
  Bottom Sheet: "Take Photo" | "Choose from Gallery"
      │                │
      ▼                ▼
  Camera Intent    Gallery Picker
      │                │
      ▼                ▼
  Compress Image (max 1920px, JPEG 80%)
      │
      ▼
  Add to image list (max 3)
      │
      ▼
  Show preview with remove button
```

### Location Services

- **Permissions:** `ACCESS_FINE_LOCATION` (precise) with `ACCESS_COARSE_LOCATION` fallback
- **Use cases:**
  1. Map View: Show user's location blue dot
  2. Post Item: Auto-fill location coordinates
  3. Dashboard: Sort "nearby" items (future feature)
- **Battery consideration:** Use `FusedLocationProviderClient` with balanced accuracy

### Biometric Authentication (Future Enhancement)

- Optional fingerprint/face unlock to skip login
- Store encrypted JWT in Android Keystore
- Use `BiometricPrompt` API

### Share Item

- Share button on ItemDetail → Android share sheet
- Share text: "Found a [category] at [location] on UniLost! [deep-link-url]"

### App Widget (Future Enhancement)

- Glance widget showing "X items near you" with quick-post button

---

## 11. Testing Strategy

### Test Pyramid

```
        ╱ UI Tests (10%) ╲
       ╱  Compose + Espresso   ╲
      ╱─────────────────────────╲
     ╱ Integration Tests (30%)   ╲
    ╱  Repository + API + Room    ╲
   ╱───────────────────────────────╲
  ╱    Unit Tests (60%)             ╲
 ╱  ViewModels + Utilities + Repos   ╲
╱─────────────────────────────────────╲
```

### Key Test Scenarios

| Feature | Test Type | What to Test |
|---------|-----------|--------------|
| Login | Unit + UI | Valid/invalid credentials, token storage, navigation |
| Item Feed | Unit + Integration | Pagination, filtering, search debounce, empty state |
| Post Item | Unit + UI | Form validation, image compression, multipart upload |
| Submit Claim | Unit + Integration | LOST auto-accept, FOUND secret answer, duplicate prevention |
| Chat | Integration | Message send/receive, WebSocket reconnection, unread count |
| Notifications | Unit | Badge count, mark read, deep link routing |
| Offline | Integration | Room cache hit, stale data display, refresh on reconnect |

### Testing Tools

- **JUnit 5** — Unit tests
- **MockK** — Kotlin mocking
- **Turbine** — Flow testing
- **Compose Testing** — `createComposeRule()` for UI tests
- **Hilt Testing** — `@HiltAndroidTest` for DI in tests
- **OkHttp MockWebServer** — API response mocking

---

## 12. Performance & Scalability

### Performance Targets

| Metric | Target |
|--------|--------|
| Cold start | < 2 seconds |
| Screen transition | < 300ms |
| Item feed scroll | 60 FPS |
| Image load (cached) | < 100ms |
| Image load (network) | < 2 seconds |
| Message send → receive | < 500ms (WebSocket) |
| Memory usage | < 150MB baseline |

### Optimization Techniques

1. **Image Optimization**
   - Glide with disk + memory cache
   - Request appropriately sized images (Cloudinary URL transformations: `w_400,h_400,c_fill`)
   - Thumbnail placeholders (blurred low-res)
   - Skip decoding for off-screen images

2. **List Performance**
   - `LazyColumn` with stable `key` per item
   - Avoid recomposition: use `remember`, `derivedStateOf`
   - Prefetch next page when 5 items from end

3. **Network Efficiency**
   - Request only needed fields (if backend supports projection)
   - Batch related requests where possible
   - ETag/If-Modified-Since caching headers
   - Cancel in-flight requests on screen exit (ViewModel scope)

4. **Memory Management**
   - Avoid holding large bitmaps in memory
   - Clear Room cache periodically (items older than 7 days)
   - Use `rememberSaveable` for surviving config changes

5. **Battery**
   - WebSocket only when app is in foreground
   - Location updates with balanced accuracy
   - Minimize background work (defer to FCM push)

---

## 13. Risk Assessment & Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| **Backend FCM not ready** | No push notifications when app killed | Medium | Implement aggressive in-app polling as fallback; WebSocket covers foreground |
| **WebSocket instability on mobile networks** | Missed real-time messages | Medium | Auto-reconnect with backoff; polling fallback every 30s; persist messages in Room |
| **Google Maps API quota/cost** | Map view breaks or costs money | Low | Set daily quota limits; cache map tiles; limit marker count to 100 |
| **Image upload fails on slow network** | User loses post progress | Medium | Save draft locally in Room; retry with exponential backoff; show progress bar |
| **JWT expires during long session** | 401 errors mid-use | Medium | Check token expiry before requests; proactive refresh flow (or re-login prompt) |
| **Large campus with 1000+ items** | Slow feed, heavy memory | Low | Paging 3 with 20-item pages; lazy image loading; server-side search |
| **User denies location permission** | Map and location features degrade | Medium | Map still works (centered on Cebu); manual location entry for PostItem |
| **User denies notification permission** | No push notifications | Medium | In-app badge + periodic polling still work; show re-prompt after key action |
| **Concurrent claim race condition** | Multiple users claim same item | Low | Backend has optimistic locking; show friendly "already claimed" message |
| **App package name mismatch** | Namespace says `com.hulampay.mobile` | Low | Rename to `com.unilost.mobile` during Phase 10 cleanup |

---

## Summary: Implementation Order & Dependencies

```
Phase 3: Item Management ──────────┐
    (Models, API, Feed, Post,      │
     Images, Campus)               │
         │                         │
         ▼                         │
Phase 4: Claim & Verification      │
    (Claim flow, handover UI)      │
         │                         │
         ▼                         │
Phase 5: Messaging                 │
    (WebSocket, chat screens)      │
         │                         │
         ▼                         ▼
Phase 6: Handover & Karma  Phase 7: Campus Maps
    (dual-confirm,              (Google Maps,
     karma leaderboard)          location picker)
         │                         │
         └──────────┬──────────────┘
                    ▼
          Phase 8: Push Notifications
              (FCM, deep links,
               notification center)
                    │
                    ▼
          Phase 9: Testing & Deploy
              (QA, performance,
               Play Store release)
```

> **Note:** Admin is fully removed from mobile scope. All admin/moderation work is done via the web app on desktop.

**Total estimated effort: ~29 days** (single developer, sequential phases)

**Parallel opportunities:**
- Phase 6 (Handover & Karma) can run in parallel with Phase 7 (Maps) once Phase 5 is done
- This could compress timeline to ~22 days with parallelization

---

> **Next Step:** Begin Phase 3 — Item Management. Start with models and API service definitions, then wire the Item Feed screen.
