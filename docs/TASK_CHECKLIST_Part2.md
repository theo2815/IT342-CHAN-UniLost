# Task Checklist - Part 2 (Mobile & Backend Integration)

## 1. Mobile Application - Android Kotlin
- [x] **Register Screen**
    - Implemented UI matching web design.
    - Integrated with backend `/api/auth/register`.
    - Added client-side validation (empty fields, email regex, password match).
    - Added dynamic "Select University" dropdown fetching from backend.
- [x] **Login Screen**
    - Implemented UI matching web design.
    - Integrated with backend `/api/auth/login`.
    - Secure token storage using DataStore.
- [ ] **Dashboard/Profile Screen (Protected)**
    - [x] Basic UI structure implemented.
    - [x] Protected route logic (redirects to Login if no token).
    - [ ] **Ongoing**: Fetching and displaying real user data in Dashboard and Profile screens.
- [x] **Logout Functionality**
    - Clears local token storage.
    - Redirects to Login screen.
- [x] **Backend Connection**
    - Retrofit configured with `BASE_URL = "http://10.0.2.2:8080/api/"` for emulator access.
    - Handles network transmission securely.

## 2. Backend Finalization
- [x] **Logout Handling**
    - Implemented secure logout mechanism (token clearing on client).
- [x] **Improved Validation & Error Handling**
    - Backend returns structured error messages.
    - Mobile app parses and displays these errors (e.g., "Email already exists").
- [x] **Consistent API Responses**
    - Standardized `AuthResponse` with `Token` and `UserDTO`.
    - Updated `AuthApiService` and `User` model in Android to match backend response structure.

## 3. Web Application Fixes (Bonus)
- [x] **Login Data Issue**
    - Refactored `authService.js` to store `token` and `user` separately.
    - Fixed "undefined" user data in Header and Profile components.
    - Updated `api.js` to retrieve token from correct storage key.

---
**Status:** Mobile Authentication (Register/Login) is fully functional. Dashboard/Profile UI is in place but data integration is ongoing.
