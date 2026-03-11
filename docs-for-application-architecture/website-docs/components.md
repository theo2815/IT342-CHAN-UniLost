# Website Components

## Layout Components

### Header.jsx (~200 lines)
- **Nav links**: Home, Item Feed, Map View, Leaderboard
- **User menu dropdown**: Profile, Settings, Logout, role badge — uses click-outside detection via refs
- **Notification bell**: Badge with unread count, opens `NotificationDropdown` with 5 recent
- **Post Item CTA**: Floating button with PlusCircle icon
- **Messages icon**: Links to /messages
- **Auth state**: Shows Login/Register links if not authenticated
- **Active route**: Highlights current nav link via `useLocation()`

### Footer.jsx
- Brand logo, tagline: "Connecting Cebu's Academic Community"
- Quick links: Privacy, Terms, Support
- Copyright 2026

## Route Guards

### ProtectedRoute.jsx
- Checks `authService.isAuthenticated()` → redirect to `/login` if false
- Returns `<Outlet />` for nested route rendering

### AdminRoute.jsx
- Checks authenticated AND `authService.isAdmin()`
- Redirects to `/items` if unauthorized

## Item Display

### ItemCard.jsx
**Props**: `item` (object), `onClick` (handler), `variant` ('default' | 'compact' | 'snapshot')

- Image with blur effect if item type is FOUND
- Type badge (LOST/FOUND) with color coding
- "Image protected" label for FOUND items
- Title, category tag, school tag
- Location pin + relative time (`timeAgo()`)
- Snapshot variant: special image reveal for Leaderboard

### StatusBadge.jsx
**Props**: `status` (string)

Maps status to styled label: ACTIVE, CLAIMED, HANDED_OVER, EXPIRED, CANCELLED, LOST, FOUND, PENDING, APPROVED, REJECTED

### EmptyState.jsx
**Props**: `icon` (optional, defaults to PackageX), `title`, `message`, `action` (optional button)

## Modals & Dialogs

### ClaimModal.jsx (~150 lines)
**Props**: `isOpen`, `onClose`, `item`

- Item summary: thumbnail, type badge, title, location
- Secret detail answer field (FOUND items only)
- Message textarea: 500 char limit with counter
- Email visibility checkbox
- Success confirmation state with "View My Claims" button
- Backdrop click to dismiss

### ConfirmDialog.jsx
**Props**: `isOpen`, `onClose`, `onConfirm`, `title`, `message` (string or JSX), `confirmLabel`, `cancelLabel`, `variant` ('danger' | 'warning' | 'success')

- Escape key to close
- Variant-based icon
- ARIA `role="alert"` for accessibility

## Form Components

### FilterBar.jsx
**Props**: `searchQuery`, `onSearchChange`, `activeType`, `onTypeChange`, `activeCategory`, `onCategoryChange`, `activeSchool`, `onSchoolChange`

- Real-time search input
- Type chips: All / Lost / Found
- Category dropdown: 9 categories
- School dropdown: 8 schools
- Controlled component — parent manages all state

### ImageUpload.jsx
**Props**: `images`, `onImagesChange`, `maxImages` (default: 5)

- Drag-and-drop OR click to browse
- Filters to image/* MIME types
- Preview thumbnails with remove buttons
- Drag-over visual state

## Notifications

### NotificationDropdown.jsx
- Fetches 5 recent via `getRecentNotifications()`
- 7 notification types with color-coded circular icons:
  CLAIM_RECEIVED, CLAIM_APPROVED, CLAIM_REJECTED, HANDOVER_CONFIRMED, HANDOVER_REMINDER, ITEM_EXPIRED, ITEM_MATCH
- Mark All Read button
- Unread dot indicator per item
- Click navigation to detail pages
- "View All" link to /notifications

---

## Design System (10 UI Components)

All located in `components/ui/` — each has its own directory with `.jsx` and `.css` files.

### 1. Button (`ui/Button/Button.jsx`)
**Props**: `variant` ('primary' | 'secondary' | 'outline' | 'ghost'), `size` ('sm' | 'md' | 'lg'), `loading`, `disabled`, `fullWidth`, `icon` (left), `iconRight`, `type` ('button' | 'submit' | 'reset')

- Spinner when loading
- Icon sizing matches button size
- ARIA attributes for accessibility

### 2. Input (`ui/Input/Input.jsx`)
**Props**: `label`, `error`, `helper`, `required`, `icon` (left), `iconRight`, `rightAction` (interactive element, e.g., show/hide password), `maxLength`, `disabled`, `textarea` (boolean → renders `<textarea>`), `size` ('sm' | 'md' | 'lg')

- Auto-generated IDs via `useId()`
- Character count display with maxLength
- Error message with warning icon
- Helper text
- ARIA descriptions
- `forwardRef` for external ref access

### 3. Alert (`ui/Alert/Alert.jsx`)
**Props**: `type` ('success' | 'error' | 'warning' | 'info'), `title` (optional), `children` (message), `dismissible`, `autoDismiss` (ms), `onDismiss`, `showIcon`, `icon` (custom)

- Icon auto-maps to type
- Exit animation on dismiss
- Screen reader `role="alert"`

### 4. Modal (`ui/Modal/Modal.jsx`)
**Props**: `isOpen`, `onClose`, `title`, `size` ('sm' | 'md' | 'lg' | 'xl'), `closeOnBackdrop`, `closeOnEscape`, `footer` (JSX slot)

- Focus trap (Tab loops inside modal)
- Escape key handler
- Body scroll prevention
- Focus restoration on close
- ARIA modal attributes

### 5. Spinner (`ui/Spinner/Spinner.jsx`)
**Props**: `size` ('sm' | 'md' | 'lg'), `label` (sr-only text), `fullPage` (overlay variant)

### 6. Badge (`ui/Badge/Badge.jsx`)
**Props**: `variant`, `size` ('sm' | 'md' | 'lg'), `solid`, `outline`, `dot` (indicator), `icon`

### 7. Card (`ui/Card/Card.jsx`) — Compound Component
**Props**: `hoverable`, `padded`, `compact`, `elevated`, `glass`, `fullWidth`, `onClick`

**Sub-components**: `Card.Header({ title, children })`, `Card.Body({ children })`, `Card.Footer({ children })`, `Card.Image({ src, alt })`

### 8. FormError (`ui/FormError/FormError.jsx`)
Inline error display with icon

### 9. Skeleton (`ui/Skeleton/Skeleton.jsx`)
Loading placeholder with shimmer animation

### 10. Toast (`ui/Toast/` directory)
**Files**: `Toast.jsx` (includes ToastProvider), `Toast.css`, `toastContext.js` (ToastContext + useToast), `toastApi.js`, `index.js`

- **ToastProvider**: Manages toast state, max 5 visible, renders toast list
- **useToast()**: Consumer hook for components
- **toastApi.js**: Global bridge for non-component code — `showSuccess()`, `showError()`, `showWarning()`, `showInfo()`
- Auto-dismiss (4s default), progress bar, icons per type
