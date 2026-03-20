# Phase 1 - Authentication System (Backend + Website)

> **Status:** COMPLETED
> **Priority:** MUST HAVE
> **Depends On:** None

---

## Objective

Implement a secure, multi-university email domain authentication system that verifies users belong to participating Cebu City academic institutions.

---

## Backend (Spring Boot)

| Task | Endpoint / Component | Status |
|------|---------------------|--------|
| User registration with email domain validation | `POST /api/auth/register` | DONE |
| User login with JWT token generation | `POST /api/auth/login` | DONE |
| Forgot password (OTP via email) | `POST /api/auth/forgot-password` | DONE |
| OTP verification | `POST /api/auth/verify-otp` | DONE |
| Password reset | `POST /api/auth/reset-password` | DONE |
| Get current authenticated user | `GET /api/auth/me` | DONE |
| JWT Authentication Filter | `JwtAuthenticationFilter.java` | DONE |
| JWT Utility (HS256, 24h expiry) | `JwtUtils.java` | DONE |
| Spring Security Configuration | `SecurityConfig.java` | DONE |
| BCrypt password hashing | Via Spring Security | DONE |
| Email service for OTP delivery | `EmailService.java` (Gmail SMTP) | DONE |

## Website (React + Vite)

| Task | Page / Component | Status |
|------|-----------------|--------|
| Login page with error handling | `Login/Login.jsx` | DONE |
| Registration page with campus auto-detection | `Register/Register.jsx` | DONE |
| Forgot Password page | `ForgotPassword/ForgotPassword.jsx` | DONE |
| OTP Verification page | `VerifyOTP/VerifyOTP.jsx` | DONE |
| Reset Password page | `ResetPassword/ResetPassword.jsx` | DONE |
| Protected Route guard | `ProtectedRoute.jsx` | DONE |
| Admin Route guard | `AdminRoute.jsx` | DONE |
| Axios interceptors (JWT injection, 401 redirect) | `services/api.js` | DONE |
| Auth service (all auth API calls) | `services/authService.js` | DONE |

---

## Technical Details

### JWT Token Structure
- **Algorithm:** HS256
- **Expiry:** 24 hours
- **Claims:** email, role
- **Header:** `Authorization: Bearer <token>`

### Password Policy
- Minimum 8 characters
- BCrypt hashing (Spring Security encoder)

### OTP (Forgot Password)
- 6-digit random code
- BCrypt-encoded storage in user document
- 10-minute expiry window
- Delivered via Gmail SMTP (Brevo service)

### University Domain Whitelist
Verified against campus collection in MongoDB:
- `@cit.edu` (CIT-U)
- `@usc.edu.ph` (USC)
- `@usjr.edu.ph` (USJ-R)
- `@uc.edu.ph` (UC)
- `@up.edu.ph` (UP Cebu)
- `@swu.edu.ph` (SWU)
- `@cnu.edu.ph` (CNU)
- `@ctu.edu.ph` (CTU)

---

## Outstanding Items (Deferred)

| Item | Deferred To |
|------|------------|
| Email verification endpoint (`POST /api/auth/verify-email`) | Phase 10 |
| JWT refresh token rotation | Phase 10 |
| Rate limiting on auth endpoints | Phase 10 |
