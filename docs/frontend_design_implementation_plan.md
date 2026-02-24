# UniLost - Frontend Design Implementation Plan

> **Approach**: Build all UI screens with mock/hardcoded data first across both website and mobile. Once the frontend design is complete and validated, wire up the real backend APIs.

---

## What Already Exists (Phase 1 + Phase A - Completed)

### Website (React)
| Page | Route | Status |
|------|-------|--------|
| Login | `/login` | Done |
| Register | `/register` | Done (email domain auto-detect) |
| Dashboard | `/dashboard` | Placeholder (mock stats) |
| **Item Feed** | **`/items`** | **Done (Phase A) — search, filters, responsive grid** |
| **Item Detail** | **`/items/:id`** | **Done (Phase A) — two-col layout, blur, actions** |
| **Post Item** | **`/post-item`** | **Done (Phase A) — type toggle, form, image upload, preview** |
| **My Items** | **`/my-items`** | **Done (Phase A) — stats, tabs, actions, claim badges** |
| **My Claims** | **`/my-claims`** | **Done (Phase B) — tabs, claim cards, handover progress** |
| **Incoming Claims** | **`/my-items/:itemId/claims`** | **Done (Phase B) — secret detail comparison, approve/reject** |
| **Claim Detail** | **`/claims/:claimId`** | **Done (Phase B) — handover stepper, dual-confirmation** |
| **Notifications** | **`/notifications`** | **Done (Phase C) — filter tabs, type icons, mark-as-read** |
| Profile | `/profile` | Basic structure (mock data) |
| Settings | `/settings` | Working (edit profile + theme toggle) |
| Header | Component | Done (nav links: Feed, My Items, My Claims, Post Item + active states) |
| **ItemCard** | Component | **Done (Phase A)** |
| **StatusBadge** | Component | **Done (Phase A)** |
| **FilterBar** | Component | **Done (Phase A)** |
| **EmptyState** | Component | **Done (Phase A)** |
| **ClaimModal** | Component | **Done (Phase B) — claim submission overlay** |
| **NotificationDropdown** | Component | **Done (Phase C) — header bell dropdown** |
| **AdminRoute** | Component | **Done (Phase D) — admin + super admin route guards** |
| **ConfirmDialog** | Component | **Done (Phase E) — reusable confirmation modal with variants** |
| **ImageUpload** | Component | **Done (Phase E) — reusable drag & drop image upload** |
| ProtectedRoute | Component | Done |
| **Admin Dashboard** | **`/admin`** | **Done (Phase D) — stats, actions log, quick links** |
| **Admin Items** | **`/admin/items`** | **Done (Phase D) — table, filters, remove modal, bulk actions** |
| **Admin Users** | **`/admin/users`** | **Done (Phase D) — table, search, ban/unban modal** |
| **Admin Claims** | **`/admin/claims`** | **Done (Phase D) — tabs, table, override modal** |
| **Super Admin Panel** | **`/superadmin`** | **Done (Phase D) — campus stats, school mgmt, create admin** |

### Mobile (Kotlin)
| Screen | Route | Status |
|--------|-------|--------|
| LoginScreen | `login_screen` | Done (navigates to ItemFeed) |
| RegisterScreen | `register_screen` | Done (email domain auto-detect) |
| DashboardScreen | `dashboard_screen` | Updated (Phase E) — Admin button removed (moved to ProfileScreen) |
| **ItemFeedScreen** | **`item_feed_screen`** | **Done (Phase A) — search, filter chips, LazyColumn, FAB** |
| **ItemDetailScreen** | **`item_detail_screen/{itemId}`** | **Done (Phase A) — blur, chips, meta, actions** |
| **PostItemScreen** | **`post_item_screen`** | **Done (Phase A) — type selector, form, secret detail** |
| **MyItemsScreen** | **`my_items_screen`** | **Done (Phase A) — stats, tabs, MyItemRow** |
| **MyClaimsScreen** | **`my_claims_screen`** | **Done (Phase B) — filter chips, claim cards, handover progress** |
| **ClaimDetailScreen** | **`claim_detail_screen/{claimId}`** | **Done (Phase B) — summary, parties, handover stepper** |
| **NotificationsScreen** | **`notifications_screen`** | **Done (Phase C) — filter chips, type cards, unread indicators** |
| **AdminScreen** | **`admin_screen`** | **Done (Phase D) — stats, items/users tabs, remove/ban actions** |
| **ProfileScreen** | **`profile_screen`** | **Done (Phase E) — avatar, info card, stats, admin button, logout** |
| **SettingsScreen** | **`settings_screen`** | **Done (Phase E) — edit profile form, theme selection, placeholders** |
| **BottomNavBar** | Component | **Done (Phase A, updated Phase E) — Feed, My Items, Post, Alerts, Profile (→ profile_screen)** |
| MainScreen | `main_screen` | Navigation placeholder |
| DetailScreen | `detail_screen` | Parameter display only |
| AuthComponents | Reusable | AuthInput + PrimaryButton |

---

## Frontend Design Phases

### Design Phase A: Core Item Screens (Website + Mobile) -- COMPLETED

> **Status**: Completed
> **Navigation updates (from Phase E) also done**: Routes, Header nav links, NavGraph, BottomNavBar all wired in Phase A.

#### Changelog

**Mock Data Created**:
- `website/src/mockData/items.js` — 15 mock items, 9 categories, 8 schools, `timeAgo()` + `daysUntilExpiry()` helpers
- `mobile/.../data/mock/MockItems.kt` — `MockItem` data class, 12 items, categories, schools, `timeAgo()` helper

**Reusable Components (Website)**:
- `components/ItemCard.jsx` + `ItemCard.css` — Card with image (blur for FOUND), type badge, tags, meta
- `components/StatusBadge.jsx` + `StatusBadge.css` — Colored badges for all statuses, dark mode support
- `components/FilterBar.jsx` + `FilterBar.css` — Search input, type chips, category/school dropdowns
- `components/EmptyState.jsx` + `EmptyState.css` — Icon, title, message, optional CTA button

**Reusable Components (Mobile)**:
- `ui/components/BottomNavBar.kt` — NavigationBar with Feed, My Items, Post, Profile tabs
- `ItemCard` composable (inside `ItemFeedScreen.kt`)
- `StatusChip`, `MetaRow` composables (inside `ItemDetailScreen.kt`)
- `TypeOption` composable (inside `PostItemScreen.kt`)
- `StatMiniCard`, `MyItemRow` composables (inside `MyItemsScreen.kt`)

**A1. Item Feed Page (Website)** — `/items`
- `pages/ItemFeed/ItemFeed.jsx` + `ItemFeed.css`
- Responsive 3-col grid (2 on tablet, 1 on mobile), client-side filtering, Load More, empty state
- FilterBar integration with search, type chips, category & school dropdowns

**A2. Item Detail Page (Website)** — `/items/:id`
- `pages/ItemDetail/ItemDetail.jsx` + `ItemDetail.css`
- Two-column layout (stacks on mobile), image blur + overlay for FOUND, metadata card, poster card
- Action buttons: claim/found (non-owner) or edit/recover/cancel (owner)
- Related items section (same category/school, max 3), 404 state

**A3. Post Item Form (Website)** — `/post-item`
- `pages/PostItem/PostItem.jsx` + `PostItem.css`
- Type toggle (Lost=red, Found=green), all form fields, image drag & drop with thumbnails
- Secret Detail field (FOUND only) in dashed card, live preview toggle, success redirect

**A4. My Items Page (Website)** — `/my-items`
- `pages/MyItems/MyItems.jsx` + `MyItems.css`
- Quick stats row (5 cards), 6 status tabs, list rows with actions and claim badges, empty state per tab

**A5. Item Feed Screen (Mobile)** — `item_feed_screen`
- `ui/items/ItemFeedScreen.kt`
- TopAppBar, search field, filter chips (type + category), LazyColumn of ItemCards, FAB

**A6. Item Detail Screen (Mobile)** — `item_detail_screen/{itemId}`
- `ui/items/ItemDetailScreen.kt`
- Image with blur, status chips, meta card, poster card, bottom action bar, related items

**A7. Post Item Screen (Mobile)** — `post_item_screen`
- `ui/items/PostItemScreen.kt`
- Type selector, form fields, category ExposedDropdownMenu, image placeholder, secret detail card

**A8. My Items Screen (Mobile)** — `my_items_screen`
- `ui/items/MyItemsScreen.kt`
- Stats row, ScrollableTabRow, filtered LazyColumn with MyItemRow

**Routing & Navigation Updates**:
- `App.jsx` — 4 new protected routes: `/items`, `/items/:id`, `/post-item`, `/my-items`
- `Header.jsx` + `Header.css` — Nav links (Feed, My Items, Post Item) with active state, responsive hide on mobile
- Logo now links to `/items` instead of `/dashboard`
- `navigation/Screen.kt` — Added ItemFeed, ItemDetail, PostItem, MyItems routes
- `navigation/NavGraph.kt` — 4 new composable destinations with proper argument handling
- `LoginScreen.kt` — Login now navigates to `ItemFeed` instead of `Dashboard`

---

### Design Phase B: Claims & Handover UI -- COMPLETED

> **Status**: Completed
> **All website and mobile screens built with mock data. Routes and navigation updated.**

#### Changelog

**Mock Data Created**:
- `website/src/mockData/claims.js` — 7 mock claims covering PENDING, APPROVED, REJECTED, HANDED_OVER statuses. Incoming claims (on u1's items: c1, c2) and outgoing claims (by u1: c3, c4). Exports: `mockClaims`, `getClaimsForItem()`, `getMyOutgoingClaims()`, `getMyIncomingClaims()`, `getClaimById()`, `timeAgo()`
- `mobile/.../data/mock/MockClaims.kt` — `MockClaim` data class with matching 7 claims. `MockClaims` object with `getClaimsForItem()`, `getMyOutgoingClaims()`, `getMyIncomingClaims()`, `getClaimById()` helpers

**B1. Claim Submission Modal (Website)** — triggered from ItemDetail
- `components/ClaimModal.jsx` + `ClaimModal.css`
- Full-screen backdrop overlay, glass modal card (max-width 520px)
- Item summary row, secret detail answer field (FOUND only), message textarea (500 char limit)
- Contact preference checkbox, Submit/Cancel buttons, success state with "View My Claims" navigation
- `ItemDetail.jsx` modified — `showClaimModal` state, claim button wired to open modal

**B2. My Claims Page (Website)** — `/my-claims`
- `pages/MyClaims/MyClaims.jsx` + `MyClaims.css`
- Filter tabs (All/Pending/Approved/Rejected/Handed Over), claim card rows with status badges
- Handover progress indicator for APPROVED claims, click navigates to `/claims/:claimId`
- Empty state with "Browse Items" CTA

**B3. Incoming Claims View (Website)** — `/my-items/:itemId/claims`
- `pages/IncomingClaims/IncomingClaims.jsx` + `IncomingClaims.css`
- Item summary card, "Only one claim can be approved" warning banner
- Claim cards with claimant avatar, secret detail side-by-side comparison
- Approve/Reject buttons for PENDING claims, empty state

**B4. Claim Detail + Handover UI (Website)** — `/claims/:claimId`
- `pages/ClaimDetail/ClaimDetail.jsx` + `ClaimDetail.css`
- Claim summary, parties section (poster + claimant cards), claim content
- 4-step handover stepper (Claim Approved → Poster Confirms → Claimant Confirms → Handed Over)
- Confirm Handover button, status text, suggested meetup location
- Status-specific content for PENDING/REJECTED/APPROVED/HANDED_OVER

**B5. Mobile Claim Flow Screens**:
- `ui/items/MyClaimsScreen.kt` — TopAppBar, filter chips, LazyColumn of ClaimCard composables, handover progress for APPROVED, empty state
- `ui/items/ClaimDetailScreen.kt` — Claim summary card, parties row, claim content, HandoverStepper composable (4-step vertical stepper), confirm button, location hint
- `ui/items/ItemDetailScreen.kt` modified — ModalBottomSheet claim form (item summary, secret detail field, message, submit, success state), incoming claims section for poster (claimant info, secret detail comparison, approve/reject buttons)

**Routing & Navigation Updates**:
- `App.jsx` — 3 new protected routes: `/my-claims`, `/my-items/:itemId/claims`, `/claims/:claimId`
- `Header.jsx` — Added "My Claims" nav link with FileText icon, updated active states for child routes
- `MyItems.jsx` — Wired Claims button onClick to navigate to incoming claims
- `navigation/Screen.kt` — Added MyClaims, ClaimDetail routes
- `navigation/NavGraph.kt` — 2 new composable destinations with proper argument handling

---

### Design Phase C: Notifications -- COMPLETED

> **Status**: Completed
> **Notification dropdown, full notifications page (website), and notifications screen (mobile) built with mock data. Bell wired up with dynamic badge count.**

#### Changelog

**Mock Data Created**:
- `website/src/mockData/notifications.js` — 11 mock notifications covering 7 types: CLAIM_RECEIVED, CLAIM_APPROVED, CLAIM_REJECTED, HANDOVER_CONFIRMED, HANDOVER_REMINDER, ITEM_EXPIRED, ITEM_MATCH. Mix of read/unread. Exports: `mockNotifications`, `getUnreadCount()`, `getRecentNotifications()`, `markAsRead()`, `markAllAsRead()`, `timeAgo()`
- `mobile/.../data/mock/MockNotifications.kt` — `MockNotification` data class with matching 11 notifications. `MockNotifications` object with `getUnreadCount()`, `getAll()`, `timeAgo()` helpers

**C1. Notification Dropdown (Website)** — triggered from Header bell icon
- `components/NotificationDropdown.jsx` + `NotificationDropdown.css`
- Dropdown panel (380px wide, absolute positioned, glass style)
- Header row with "Notifications" title + "Mark all as read" button
- 5 most recent notifications with color-coded type icons, title, message preview, time ago, unread dot
- Click notification → navigates to linked route + marks as read
- Footer with "View all notifications" → `/notifications`
- `Header.jsx` modified — dynamic unread badge count, `isNotificationOpen` state, click-outside handler, `notification-wrapper` ref
- `Header.css` modified — added `.notification-wrapper` positioning

**C2. Notifications Page (Website)** — `/notifications`
- `pages/Notifications/Notifications.jsx` + `Notifications.css`
- Page header with unread count badge + "Mark all as read" button
- Filter tabs: All / Unread / Claims / Items
- Notification cards with: unread left border accent, color-coded type icon, title, full message, type label, time ago, unread dot
- Click notification → navigates to linked route + marks as read
- EmptyState for empty filters

**C3. Notifications Screen (Mobile)** — `notifications_screen`
- `ui/items/NotificationsScreen.kt`
- TopAppBar with unread badge + "Mark all read" action
- Filter chips (All/Unread/Claims/Items)
- LazyColumn of NotificationCard composables with: unread dot indicator, color-coded type icon circle, title, message, time ago, type label
- Empty state composable

**Routing & Navigation Updates**:
- `App.jsx` — Added `/notifications` protected route
- `navigation/Screen.kt` — Added Notifications route
- `navigation/NavGraph.kt` — Added notifications composable destination
- `ui/components/BottomNavBar.kt` — Added "Alerts" tab with Notifications icon (5 tabs: Feed, My Items, Post, Alerts, Profile)

---

### Design Phase D: Admin Panels -- COMPLETED

> **Status**: Completed
> **All website admin pages (dashboard, items, users, claims, super admin) and mobile admin screen built with mock data. Routes guarded by AdminRoute/SuperAdminRoute. Navigation updated.**

#### Changelog

**Mock Data Created**:
- `website/src/mockData/adminData.js` — Dashboard stats, 8 admin actions (5 types), 12 mock users (roles: 9 STUDENT + 2 ADMIN + 1 SUPER_ADMIN, 2 banned), 15 admin items, 10 admin claims, 8 schools with campus stats. Exports: `mockAdminStats`, `mockAdminActions`, `mockAdminUsers`, `mockAdminItems`, `mockAdminClaims`, `mockSchools`, `mockCampusStats`, `timeAgo()`
- `mobile/.../data/mock/MockAdminData.kt` — `MockAdminStats`, `MockAdminItem`, `MockAdminUser` data classes. `MockAdminData` object with stats, 10 items, 12 users

**Route Guards**:
- `website/src/components/AdminRoute.jsx` — `AdminRoute` (ADMIN + SUPER_ADMIN → `<Outlet />`, else → `/items`) and `SuperAdminRoute` (SUPER_ADMIN only → `<Outlet />`, else → `/admin`)

**D1. Admin Dashboard (Website)** — `/admin`
- `pages/Admin/AdminDashboard.jsx` + `AdminDashboard.css`
- Shield icon header with role badge (Campus Admin / Super Admin)
- 5 stat cards grid (Total Users, Active Items, Pending Claims, Recovered This Month, Banned Users) with colored icons
- Quick Links section: 3 cards → Items/Users/Claims management + Super Admin Panel link (SUPER_ADMIN only)
- Recent Admin Actions log: 8 action rows with type-colored icons, admin name, timeAgo

**D2. Admin Item Management (Website)** — `/admin/items`
- `pages/Admin/AdminItems.jsx` + `AdminItems.css`
- Breadcrumb (Admin > Items), filter bar (search + type + status + school dropdowns)
- Data table: checkbox, image thumbnail, title, type badge, status badge, school, posted by, date, actions (View/Remove)
- Bulk action bar: "X items selected" + Remove Selected
- Remove Modal: item summary + reason textarea + confirm
- Pagination

**D3. Admin User Management (Website)** — `/admin/users`
- `pages/Admin/AdminUsers.jsx` + `AdminUsers.css`
- Breadcrumb, search bar, role/status dropdowns
- Data table: avatar+name, email, school, role badge (Student=blue, Admin=purple, Super Admin=gold), karma, status dot (Active=green, Banned=red), actions
- Ban/Unban Modal: user summary + reason textarea + confirm (red for ban, green for unban)
- Only STUDENT users can be banned

**D4. Admin Claims Management (Website)** — `/admin/claims`
- `pages/Admin/AdminClaims.jsx` + `AdminClaims.css`
- Breadcrumb, filter tabs (All/Pending/Approved/Rejected/Handed Over) with counts
- Data table: item (thumbnail+title+type), claimant, poster, status badge, date, actions (View/Override)
- Override Handover Modal (APPROVED claims only): claim summary + warning message + reason + "Force Complete Handover"

**D5. Super Admin Panel (Website)** — `/superadmin`
- `pages/Admin/SuperAdminPanel.jsx` + `SuperAdminPanel.css`
- Cross-campus Statistics: 8 school cards with shortName, users count, items count, recovery rate %
- School Management Table: name, shortName, email domain, students, items, active/inactive toggle, edit action
- Edit School Modal: name, shortName, email domain fields
- Create Admin Account form: first/last name, email, school dropdown, temporary password, success state

**D6. Admin Mobile Screen** — `admin_screen`
- `ui/items/AdminScreen.kt`
- TopAppBar with back arrow, stats row (3 mini cards: Active Items, Pending Claims, Banned Users)
- Two tabs: Items / Users
- Items tab: LazyColumn of item rows with type chip, school, status, Remove button
- Users tab: LazyColumn of user rows with avatar, name, email, role label, Ban/Unban button
- AlertDialog confirmation with reason TextField

**Routing & Navigation Updates**:
- `App.jsx` — 5 new admin routes: `/admin`, `/admin/items`, `/admin/users`, `/admin/claims` (guarded by AdminRoute), `/superadmin` (guarded by SuperAdminRoute)
- `navigation/Screen.kt` — Added `object Admin : Screen("admin_screen")`
- `navigation/NavGraph.kt` — Added AdminScreen import + composable destination
- `DashboardScreen.kt` — Added "Admin Panel" OutlinedButton (visible when isAdmin = true) navigating to admin_screen

---

### Design Phase E: Navigation & Layout Updates -- COMPLETED

> **Status**: Completed
> **Mobile ProfileScreen + SettingsScreen built. Website ConfirmDialog + ImageUpload components created. BottomNavBar updated to point to profile_screen. Navigation routes added.**

#### Changelog

**E1. Website Navigation Updates** — Already completed during Phases A-D
- All routes wired in App.jsx, Header nav links with active states

**E2. Mobile Navigation Updates** — Completed
- `Screen.kt` — Added `Profile` and `Settings` routes
- `NavGraph.kt` — Added ProfileScreen + SettingsScreen composable destinations
- `BottomNavBar.kt` — Profile tab now routes to `profile_screen` instead of `dashboard_screen`
- `DashboardScreen.kt` — Removed Admin Panel button (moved to ProfileScreen)

**E3. Reusable Components (Website)** — Completed
- Previously done: ItemCard, StatusBadge, FilterBar, EmptyState, ClaimModal, NotificationDropdown, AdminRoute
- New: `components/ConfirmDialog.jsx` + `ConfirmDialog.css` — Reusable confirmation modal with variant colors (danger/warning/success), backdrop blur, escape key, responsive
- New: `components/ImageUpload.jsx` + `ImageUpload.css` — Reusable drag & drop image upload with thumbnails, max limit, drag-over state, remove buttons

**E4. Mobile Screens + Components** — Completed
- `ui/profile/ProfileScreen.kt` — Profile header with avatar + initials, verified badge, role badge, student info card, quick stats row (3 cards), action buttons (Edit Profile, Admin Panel, Logout)
- `ui/profile/ProfileViewModel.kt` — HiltViewModel with logout via AuthRepository
- `ui/settings/SettingsScreen.kt` — Edit profile form (first/last name, phone, address), read-only fields (email, student ID, school), theme selection (light/dark preview cards), placeholder sections (Change Password, Notification Preferences)
- BottomNavBar, ItemCard, StatusChip, etc. — already created in earlier phases

---

## Implementation Order

```
Phase A (Core Item Screens) -- COMPLETED
  A1. Item Feed (Web)              -- Done
  A5. Item Feed (Mobile)           -- Done
  A3. Post Item Form (Web)         -- Done
  A7. Post Item Screen (Mobile)    -- Done
  A2. Item Detail (Web)            -- Done
  A6. Item Detail (Mobile)         -- Done
  A4. My Items (Web)               -- Done
  A8. My Items (Mobile)            -- Done
      ↓
Phase E (Navigation) -- COMPLETED
  E1. Website routes + Header nav  -- Done (routes, nav links, active states — completed during Phases A-D)
  E2. Mobile NavGraph + Bottom Nav -- Done (NavGraph updated, BottomNavBar → profile_screen, Profile + Settings routes)
  E3. Shared web components        -- Done (ItemCard, StatusBadge, FilterBar, EmptyState, ConfirmDialog, ImageUpload)
  E4. Shared mobile components     -- Done (BottomNavBar, ItemCard, StatusChip, ProfileScreen, SettingsScreen)
      ↓
Phase B (Claims & Handover) -- COMPLETED
  B1. Claim Modal (Web)           -- Done
  B5. Claim Flow (Mobile)         -- Done
  B2. My Claims (Web)             -- Done
  B3. Incoming Claims (Web)       -- Done
  B4. Handover UI (Web)           -- Done
      ↓
Phase C (Notifications) -- COMPLETED
  C1. Notification Dropdown (Web)  -- Done
  C2. Notifications Page (Web)     -- Done
  C3. Notifications Screen (Mobile) -- Done
      ↓
Phase D (Admin Panels) -- COMPLETED
  D1. Admin Dashboard (Web)              -- Done
  D2. Admin Items Management (Web)       -- Done
  D3. Admin Users Management (Web)       -- Done
  D4. Admin Claims Management (Web)      -- Done
  D5. Super Admin Panel (Web)            -- Done
  D6. Admin Mobile Screen                -- Done
```

---

## Mock Data Files

`website/src/mockData/` directory:

```
website/src/mockData/
├── items.js          — 15 mock items, categories, schools, helpers (DONE)
├── claims.js         — 7 mock claims, status helpers, timeAgo (DONE - Phase B)
├── notifications.js  — 11 mock notifications, 7 types, mark-as-read helpers (DONE - Phase C)
└── adminData.js      — admin stats, actions, users, items, claims, schools, campus stats (DONE - Phase D)
```

`mobile/.../data/mock/` directory:

```
mobile/.../data/mock/
├── MockItems.kt        — 12 mock items, categories, schools, timeAgo helper (DONE)
├── MockClaims.kt       — 7 mock claims, MockClaim data class, helpers (DONE - Phase B)
├── MockNotifications.kt — 11 mock notifications, MockNotification data class, helpers (DONE - Phase C)
└── MockAdminData.kt    — admin stats, items, users data classes (DONE - Phase D)
```

---

## Design Tokens & Consistency Rules

- **LOST items** → Red accent (`#ef4444`), alert icon
- **FOUND items** → Green accent (`#10b981`), checkmark icon
- **Status colors**: ACTIVE=blue, CLAIMED=purple, HANDED_OVER=green, EXPIRED=gray, CANCELLED=gray
- **Card style**: Use existing `.glass` class for website cards
- **Icons**: Continue using lucide-react (web) and Material Icons (mobile)
- **Spacing**: Follow existing design tokens in `index.css`
- **Font**: Inter (body) + Outfit (headings) — already set up
- **Images**: Use placeholder images from picsum.photos or local SVG illustrations for empty states

---

## After Frontend Design Is Complete

Once all screens are built with mock data:
1. Replace mock data imports with real API service calls
2. Implement the backend endpoints from the original `implementation_plan.md` (Phases 2-6)
3. Wire up services: `itemService.js`, `claimService.js`, `notificationService.js`, `adminService.js`
4. Connect mobile repositories to real API endpoints
5. Remove mock data files

---

## Estimated Scope

| Phase | Website Pages | Mobile Screens | New Components |
|-------|--------------|----------------|----------------|
| A (Items) | 4 pages | 4 screens | ~6 |
| B (Claims) | 2 pages + 1 modal | 3 screens | ~4 |
| C (Notifications) | 1 page + 1 dropdown | 1 screen | ~2 |
| D (Admin) | 5 pages | 1 screen | ~3 |
| E (Navigation) | 2 components | 2 screens + nav updates | ~4 |
| **Total** | **12 pages + 2 components** | **11 screens** | **~19 components** |
