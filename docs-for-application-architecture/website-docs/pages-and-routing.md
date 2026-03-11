# Website Pages & Routing

## Complete Route Table

| Path | Component | Guard | Role |
|------|-----------|-------|------|
| `/` | Landing | — | Public |
| `/login` | Login | — | Public |
| `/register` | Register | — | Public |
| `/forgot-password` | ForgotPassword | — | Public |
| `/verify-otp` | VerifyOTP | — | Public |
| `/reset-password` | ResetPassword | — | Public |
| `/dashboard` | Dashboard | ProtectedRoute | STUDENT |
| `/items` | ItemFeed | ProtectedRoute | STUDENT |
| `/items/:id` | ItemDetail | ProtectedRoute | STUDENT |
| `/post-item` | PostItem | ProtectedRoute | STUDENT |
| `/my-items/:itemId/claims` | IncomingClaims | ProtectedRoute | STUDENT |
| `/claims/:claimId` | ClaimDetail | ProtectedRoute | STUDENT |
| `/map` | MapView | ProtectedRoute | STUDENT |
| `/leaderboard` | Leaderboard | ProtectedRoute | STUDENT |
| `/messages` | Messages | ProtectedRoute | STUDENT |
| `/notifications` | Notifications | ProtectedRoute | STUDENT |
| `/profile` | Profile | ProtectedRoute | STUDENT |
| `/settings` | Settings | ProtectedRoute | STUDENT |
| `/admin` | AdminDashboard | AdminRoute | ADMIN |
| `/admin/items` | AdminItems | AdminRoute | ADMIN |
| `/admin/users` | AdminUsers | AdminRoute | ADMIN |
| `/admin/claims` | AdminClaims | AdminRoute | ADMIN |
| `/superadmin` | SuperAdminPanel | FacultyRoute | FACULTY |

## Public Pages

### Landing
- Hero banner with background image overlay
- CTA buttons: "Report Lost Item" + "I Found Something"
- How-it-works: 3 feature cards (Report → Verify → Reclaim)
- Live feed of recent found items
- Campus coverage text

### Login
- Email + password inputs (global `<Input>` component)
- Show/hide password toggle via `rightAction` prop
- Remember me checkbox
- Forgot password link → `/forgot-password`
- Error handling: Only shows errors on auth endpoints, not 401 redirects
- Toast on success/failure
- Redirects to `/dashboard` after 600ms

### Register
- Full name, email, password, confirm password (global `<Input>`)
- Auto campus detection: fetches campuses on mount, matches email domain
- Password strength meter (Weak/Medium/Strong)
- Terms acceptance checkbox (required)
- Campus validation: must match recognized university domain

### ForgotPassword
- Email input
- Calls `authService.forgotPassword(email)`
- On success: redirects to `/verify-otp` with email in state

### VerifyOTP
- 6-digit OTP input
- Calls `authService.verifyOtp(email, otp)`
- On success: redirects to `/reset-password` with email + OTP in state

### ResetPassword
- New password + confirm password inputs
- Password strength meter
- Calls `authService.resetPassword(email, otp, newPassword)`
- On success: redirects to `/login`

## Protected User Pages

### Dashboard
- Hero banner: "Lost something in Cebu?"
- Hot zones widget: simulated map with campus markers + ping animations
- Recent items carousel
- User greeting: "Hello, {firstName}"
- Karma/stats cards (mock data)

### ItemFeed (~99 lines)
- **FilterBar** component:
  - Search across title/description/location
  - Type chips: All / Lost / Found
  - Category dropdown (9 categories)
  - School dropdown (8 schools)
- Pagination: 9 items per page, "Load More" button
- CSS grid with staggered animations
- Empty state with "Post an Item" CTA
- Shows "Showing X of Y items"
- Filters computed via `useMemo`

### ItemDetail (~120 lines)
- Full-width image (blurred if FOUND)
- Status badges (type + item status)
- Full description, category tag, school info
- Location with map pin icon
- Posted date + days until expiry countdown
- Poster card: avatar (initials), name, school, karma score
- Actions: Share, Report, Claim (opens ClaimModal — only if not owner)
- Related items: 3 suggestions (same category/school)
- 404 state with back button

### PostItem (~150 lines)
- Type toggle: LOST / FOUND (card-style segmented control)
- Fields: title, description, category dropdown, location, date picker
- Secret detail field (FOUND only) for verification
- Image upload: drag-drop, max 3 images
- Live preview: `ItemCard` showing what will be posted
- Success state → redirects to /my-items after 2s
- Validation: requires title, description, category, location

### IncomingClaims
- Claims received on the user's posted items
- Status filtering and claim cards with claimant info

### ClaimDetail
- Full claim information with item summary
- Claim status with action buttons (for item owner)
- Handover tracking (dual confirmation)

### MapView
- Campus map with item markers (planned integration)

### Leaderboard
- Top contributors ranked by karma score

### Messages
- Chat interface between finder and verified claimant

### Notifications
- Full notification list (all types)
- Mark read functionality
- Links to related content

### Profile (~200 lines)
- Hero card: background banner, editable avatar, name + role badge
- Stats row: items posted, claims made, karma score, items recovered
- Tab navigation: "Lost Items" vs "Found Items"
- Lost Items tab: user's lost reports + incoming claims with claimant info
- Found Items tab: user's found postings
- Click-through to detail pages

### Settings (~200 lines)
- **4 tabs**:
  1. **Edit Profile**: Avatar, first name, last name, email, phone, address, student ID, school — Save/Cancel buttons
  2. **Change Password**: Current + new + confirm password
  3. **Notifications**: Notification preferences toggles
  4. **Theme**: Light/dark mode toggle

## Admin Pages

### AdminDashboard
- 5 stat cards: totalUsers, activeItems, pendingClaims, recoveredThisMonth, bannedUsers
- Quick links: Items Management, Users Management, Claims Management, (FACULTY only) Faculty Panel
- Recent admin actions log with type-colored icons

### AdminItems
- Items management table with filter/search
- Actions: View, Edit, Remove
- Status filtering

### AdminUsers
- Users table with search
- Actions: Ban, Unban, View profile, Edit role
- Karma display per user

### AdminClaims
- Claims table with status filtering
- Statuses: PENDING, APPROVED, REJECTED, HANDED_OVER
- Actions: Approve, Reject, Override handover

### SuperAdminPanel (FACULTY)
- Cross-campus statistics
- School management: Add, edit, delete schools
- Campus-wide analytics

## Mock Data (website/src/mockData/)

### items.js
- **Categories** (9): Electronics, Documents, Clothing, Accessories, Books, Bags, Keys, Wallets, Other
- **Schools** (8): CIT-U, USC, UP Cebu, USJ-R, UC, SWU, CNU, CTU (with email domains)
- **Items array**: 9+ items with full metadata
- **Helpers**: `timeAgo(dateString)`, `daysUntilExpiry(expiresAt)`

### claims.js
- 5+ claims with statuses: PENDING, APPROVED, REJECTED, HANDED_OVER
- Fields: item reference, claimant info, secret answer, message, handover tracking

### notifications.js
- 11 notifications across 7 types
- **Helpers**: `getUnreadCount()`, `getRecentNotifications(limit)`, `markAsRead(id)`, `markAllAsRead()`

### adminData.js
- `mockAdminStats` — dashboard numbers
- `mockAdminActions` — 8+ recent action log entries
- `mockAdminUsers` — 12+ user profiles with roles/karma
- `mockAdminItems` — 15+ items (admin view)
- `mockAdminClaims` — 10+ claims (admin view)
- `mockSchools` — 8 schools with student/item counts
