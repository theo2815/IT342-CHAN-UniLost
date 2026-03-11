# Website Architecture

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| React | 19.2.0 | UI framework |
| Vite | 7.3.1 | Build tool & dev server |
| React Router | 7.13.0 | Client-side routing |
| Axios | 1.13.4 | HTTP client |
| Lucide React | 0.563.0 | Icon library |
| ESLint | ^9.39.1 | Linting |

## Project Structure

```
website/src/
├── main.jsx                     # Entry point — imports global.css, renders App
├── App.jsx                      # Router + providers (Theme, Toast)
├── App.css                      # App-level overrides
├── index.css                    # Additional base styles
├── assets/                      # Static assets
├── context/
│   └── ThemeContext.jsx         # Dark/light mode with localStorage persistence
├── services/
│   ├── api.js                   # Axios instance — base URL, interceptors
│   ├── authService.js           # Login, register, logout, token management
│   ├── campusService.js         # Campus CRUD via API
│   └── schoolService.js         # School CRUD via API
├── components/
│   ├── Header.jsx               # Top nav with links, user menu, notifications
│   ├── Footer.jsx               # Brand footer with links
│   ├── ProtectedRoute.jsx       # Auth guard → redirects to /login
│   ├── AdminRoute.jsx           # Admin guard → redirects to /items
│   ├── ItemCard.jsx             # Item display card (default/compact/snapshot)
│   ├── FilterBar.jsx            # Search + type/category/school filters
│   ├── ClaimModal.jsx           # Claim submission form
│   ├── ConfirmDialog.jsx        # Confirmation dialog (danger/warning/success)
│   ├── EmptyState.jsx           # Empty list placeholder
│   ├── ImageUpload.jsx          # Drag-drop image upload (max 5)
│   ├── NotificationDropdown.jsx # Recent notifications dropdown
│   ├── StatusBadge.jsx          # Status label with color coding
│   └── Toast.jsx                # Toast notification system
├── components/ui/               # 10-component design system (see components.md)
│   ├── Alert/
│   ├── Badge/
│   ├── Button/
│   ├── Card/
│   ├── FormError/
│   ├── Input/
│   ├── Modal/
│   ├── Skeleton/
│   ├── Spinner/
│   └── Toast/
├── pages/                       # Route-mapped page components
│   ├── Landing/
│   ├── Login/, Register/, ForgotPassword/, VerifyOTP/, ResetPassword/
│   ├── Dashboard/
│   ├── ItemFeed/, ItemDetail/, PostItem/
│   ├── IncomingClaims/, ClaimDetail/
│   ├── MapView/, Leaderboard/, Messages/
│   ├── Notifications/
│   ├── Profile/, Settings/
│   └── Admin/ (AdminDashboard, AdminItems, AdminUsers, AdminClaims, SuperAdminPanel)
├── mockData/
│   ├── items.js                 # Mock items, categories, schools, helpers
│   ├── claims.js                # Mock claims data
│   ├── notifications.js         # Mock notifications + helpers
│   └── adminData.js             # Admin stats, actions, users, items, claims, schools
├── hooks/                       # Custom hooks (empty, planned)
└── styles/
    ├── global.css               # CSS reset, tokens, themes, utilities, animations
    ├── tokens.css               # Design tokens (spacing, radius, typography, z-index)
    └── themes.css               # Light/dark theme color variables
```

## Provider Hierarchy

```jsx
<StrictMode>
  <ThemeProvider>        {/* Dark/light mode context */}
    <ToastProvider>      {/* Toast notification context */}
      <BrowserRouter>    {/* React Router */}
        <Routes>...</Routes>
      </BrowserRouter>
    </ToastProvider>
  </ThemeProvider>
</StrictMode>
```

## API Client (services/api.js)

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

// Request interceptor: attaches Bearer token from localStorage
// Response interceptor: 401 → clears auth, redirects to /login
//   Exception: skips redirect for /auth/login and /auth/register paths
```

## Auth Service (services/authService.js)

**Storage**: `localStorage` keys: `token` (JWT string), `user` (JSON object)

```javascript
login(email, password)             → POST /auth/login → saves token + user
register(userData)                 → POST /auth/register
logout()                          → clears localStorage
isAuthenticated()                 → !!localStorage.token
getCurrentUser()                  → JSON.parse(localStorage.user)
getUserRole()                     → user.role || 'STUDENT'
isAdmin()                         → role === 'ADMIN'
isFaculty()                       → role === 'FACULTY'
forgotPassword(email)             → POST /auth/forgot-password
verifyOtp(email, otp)             → POST /auth/verify-otp
resetPassword(email, otp, pwd)    → POST /auth/reset-password
```

**Return pattern**: All methods return `{ success: boolean, data?, error?: string }`

## Theme System (context/ThemeContext.jsx)

```javascript
const { theme, toggleTheme, setTheme } = useTheme();
// theme: 'light' | 'dark'
// Persisted in localStorage
// Sets data-theme attribute on document.documentElement
// CSS uses [data-theme="dark"] selector for theme overrides
```

## Route Guards

**ProtectedRoute**: Checks `authService.isAuthenticated()` → redirects to `/login` → renders `<Outlet />`  
**AdminRoute**: Checks authenticated + `authService.isAdmin()` → redirects to `/items`  
**FacultyRoute** (exported from AdminRoute.jsx): Checks authenticated + `authService.isFaculty()` → redirects to `/admin`

## State Management

- **No global store** (no Redux/Zustand)
- **Theme**: React Context (`ThemeContext`)
- **Toast**: React Context (`ToastProvider`)
- **Auth**: `localStorage` (no context — read directly via `authService`)
- **Component state**: `useState` hooks
- **Derived state**: `useMemo` for filtered/computed data
- **Side effects**: `useEffect` for data fetching on mount

## Build & Run

```bash
cd website
npm install
npm run dev        # Vite dev server (localhost:5173)
npm run build      # Production build
npm run preview    # Preview production build
```

## CSS Methodology

See [design-system.md](design-system.md) for the complete design token system and theming approach.

All styles use CSS Custom Properties (`var(--token-name)`) — no CSS-in-JS, no Tailwind, no Sass.  
Each component has a co-located `.css` file using BEM-like class naming.
