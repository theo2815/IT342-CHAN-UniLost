# Mobile Screens & Navigation

## Navigation Graph

**Start Destination**: `Screen.Login.route`

### Screen Routes (Sealed Class)

```kotlin
sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Detail : Screen("detail_screen")              // IntType arg: {id}
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Dashboard : Screen("dashboard_screen")
    object ItemFeed : Screen("item_feed_screen")
    object ItemDetail : Screen("item_detail_screen")      // StringType arg: {itemId}
    object PostItem : Screen("post_item_screen")
    object MyItems : Screen("my_items_screen")
    object MyClaims : Screen("my_claims_screen")
    object ClaimDetail : Screen("claim_detail_screen")    // StringType arg: {claimId}
    object Notifications : Screen("notifications_screen")
    object Admin : Screen("admin_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")
}
```

### Navigation Phases

| Phase | Screens |
|-------|---------|
| **Auth** | Login, Register |
| **Core** | Dashboard, Main, Detail |
| **Phase A** | ItemFeed, ItemDetail, PostItem, MyItems |
| **Phase B** | MyClaims, ClaimDetail |
| **Phase C** | Notifications |
| **Phase D** | Admin |
| **Phase E** | Profile, Settings |

## Screen Details

### Authentication Screens

**LoginScreen**
- Email + password fields (AuthInput components)
- Login button (PrimaryButton with loading state)
- "Don't have an account?" → Register link
- On success: saves token to DataStore, navigates to ItemFeed
- Error: Toast message

**RegisterScreen**
- Fields: First Name, Last Name, Email, Password, Confirm Password
- Optional: Address, Phone Number, Student ID Number
- Auto-detects school from email domain (@cit.edu, @usc.edu.ph, etc.)
- Fetches schools list from API on mount
- Validation: email pattern, password length 6+, confirm match
- On success: navigates to Login; on error: Toast

### Core Screens

**DashboardScreen**
- Hero section with campus colors
- Quick stats cards (mock data)
- Recent items grid
- Logout functionality

**ItemFeedScreen**
- Filter chips: All / Lost / Found
- Category dropdown filter
- Search bar
- Grid of item cards using mock data
- Pull-to-refresh pattern
- Each card navigates to ItemDetail

**ItemDetailScreen**
- Full item image (blurred for FOUND type)
- Type badge, status badge
- Description, category, location
- Reporter info card
- "Claim This Item" button
- "Share" and "Report" actions

**PostItemScreen**
- Type selector: LOST / FOUND
- Title, description, category fields
- Location input
- Date picker
- Secret detail question (FOUND items only)
- Image picker (mock implementation)
- Submit button

### Claims Screens

**MyItemsScreen**
- User's posted items list
- Each shows claim count badge
- Navigate to item details

**MyClaimsScreen**
- Claims submitted by the user
- Status indicators: PENDING, APPROVED, REJECTED, HANDED_OVER
- Navigate to ClaimDetail

**ClaimDetailScreen**
- Item summary card
- Claim status with timeline
- Secret detail answer (if applicable)
- Message history
- Handover confirmation buttons

### Other Screens

**NotificationsScreen**
- List of notifications with type-based icons
- 7 notification types with color coding
- Mark as read on tap
- Navigate to related content via linkTo

**AdminScreen**
- Stats dashboard: active items, pending claims, banned users
- Items management tab
- Users management tab
- Action buttons: Remove, Ban/Unban

**ProfileScreen**
- User info card (name, email, school, role)
- Karma score display
- Posted items tab
- Logout button

**SettingsScreen**
- Edit profile fields
- Change password
- Notification preferences
- Theme toggle (placeholder)
- About section

## Bottom Navigation Bar

| Tab | Icon | Route | Label |
|-----|------|-------|-------|
| 1 | Search | item_feed | Feed |
| 2 | Package | my_items | My Items |
| 3 | Plus | post_item | Post |
| 4 | Bell | notifications | Alerts |
| 5 | Person | profile | Profile |

## Mock Data Summary

### MockItem
```kotlin
data class MockItem(
    id, type (LOST|FOUND), title, description, category,
    status (ACTIVE|CLAIMED|HANDED_OVER|EXPIRED|CANCELLED),
    imageUrl, location, school, claimCount, postedBy, createdAt
)
```
10+ items: phones, umbrellas, laptops, IDs, backpacks, keys, wallets, calculators, glasses, textbooks, USB drives

### MockClaim
```kotlin
data class MockClaim(
    id, itemId, itemTitle, itemType, status (PENDING|APPROVED|REJECTED|HANDED_OVER),
    secretDetailAnswer, message, createdAt
)
```
7 claims with various statuses

### MockNotification
```kotlin
data class MockNotification(
    id, type, title, message, isRead, createdAt
)
```
11 notifications across 7 types

### MockAdminUser
```kotlin
data class MockAdminUser(
    id, name, email, role (STUDENT|ADMIN|SUPER_ADMIN),
    school, karmaScore, isBanned
)
```
10 items, 12 users for admin panel

### Categories (9)
Electronics, Documents, Clothing, Accessories, Books, Bags, Keys, Wallets, Other

### Schools (8)
CIT-U, USC, UP Cebu, USJ-R, UC, SWU, CNU, CTU
