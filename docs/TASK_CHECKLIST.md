
# HulamPay Implementation Checklist

## Backend - Spring Boot

- [ ] **POST /api/auth/register**
  - **Status**: ❌ Not Implemented
  - **Details**: Registration is currently handled by `POST /api/users` in `UserController`. There is no dedicated `AuthController`.
  
- [ ] **POST /api/auth/login**
  - **Status**: ❌ Not Implemented
  - **Details**: There is no login endpoint on the backend. Authentication currently happens insecurely on the client-side (`authService.js` fetches user by email and compares plain text passwords).

- [ ] **GET /api/user/me (protected)**
  - **Status**: ❌ Not Implemented
  - **Details**: No endpoint exists to get the currently authenticated user context.

- [ ] **Database connection (MySQL)**
  - **Status**: ❌ Not Implemented
  - **Details**: Project is configured for **MongoDB**, not MySQL.
    - `pom.xml` contains `spring-boot-starter-data-mongodb`.
    - `application.properties` contains `spring.data.mongodb.uri`.

- [ ] **Password encryption (BCrypt)**
  - **Status**: ❌ Not Implemented
  - **Details**: Passwords are stored in plain text. `UserService.java` contains `// TODO: Add password encoding`.

## Web Application - ReactJS

- [x] **Register page**
  - **Status**: ✅ Implemented
  - **Details**: `src/pages/Register/Register.jsx` exists and connects to the backend (albeit to the wrong endpoint structure).

- [x] **Login page**
  - **Status**: ✅ Implemented
  - **Details**: `src/pages/Login/Login.jsx` exists.

- [x] **Dashboard/Profile page (protected)**
  - **Status**: ✅ Implemented
  - **Details**: `src/pages/Dashboard/Dashboard.jsx` and `src/pages/Profile/Profile.jsx` exist. `ProtectedRoute.jsx` handles client-side route protection.

- [x] **Logout functionality**
  - **Status**: ✅ Implemented
  - **Details**: Client-side logout implemented in `authService.js`.
