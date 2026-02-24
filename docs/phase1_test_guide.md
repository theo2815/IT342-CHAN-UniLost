# UniLost Phase 1 - Manual Test Guide

## Prerequisites

Before testing, make sure you have:

1. **MongoDB Atlas** cluster running and accessible
2. **Java 21** installed (`java -version`)
3. **Node.js + npm** installed (`node -v && npm -v`)
4. **Android Studio** with emulator (for mobile testing)

---

## Step 1: Start the Backend

```bash
cd backend
mvn spring-boot:run
```

**Verify**: Console should show `Started UnilostBackendApplication` on port **8080**.

**Check DataSeeder output**: Look for log messages confirming:
- 8 schools were seeded (or already exist)
- SUPER_ADMIN account was created (or already exists)

---

## Step 2: Start the React Frontend

```bash
cd website
npm install
npm run dev
```

**Verify**: Vite dev server starts on `http://localhost:5173`

---

## Test Cases

### TC-01: Database Rebrand Verification

**What to check**: The MongoDB database name changed from `hulampay` to `unilost`.

1. Open MongoDB Atlas dashboard (or use MongoDB Compass)
2. Verify a database named **`unilost`** exists
3. Verify collections: `users`, `schools`
4. Verify the old `hulampay` database is no longer being written to

**Expected**: All data goes to the `unilost` database.

---

### TC-02: School Seeding

**API Test**:
```
GET http://localhost:8080/api/schools
```

**Expected Response** (8 schools):

| Name | Short Name | Email Domain |
|------|-----------|-------------|
| Cebu Institute of Technology - University | CIT-U | cit.edu |
| University of San Carlos | USC | usc.edu.ph |
| University of San Jose - Recoletos | USJ-R | usjr.edu.ph |
| University of Cebu | UC | uc.edu.ph |
| University of the Philippines Cebu | UP Cebu | up.edu.ph |
| Southwestern University PHINMA | SWU | swu.edu.ph |
| Cebu Normal University | CNU | cnu.edu.ph |
| Cebu Technological University | CTU | ctu.edu.ph |

Each school should have: `id`, `name`, `shortName`, `city` ("Cebu City"), `emailDomain`.

---

### TC-03: SUPER_ADMIN Login

**API Test**:
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@unilost.com",
  "password": "admin123456"
}
```

**Expected Response**:
```json
{
  "token": "<JWT string>",
  "user": {
    "firstName": "UniLost",
    "lastName": "Admin",
    "email": "admin@unilost.com",
    "role": "SUPER_ADMIN",
    ...
  }
}
```

**Verify**:
- `user.role` is `"SUPER_ADMIN"`
- `token` is a valid JWT string
- You can decode the JWT at https://jwt.io — it should contain `"role": "SUPER_ADMIN"` in the payload

---

### TC-04: Student Registration (Valid Email Domain)

**API Test**:
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Dela Cruz",
  "email": "juan@cit.edu",
  "password": "password123",
  "address": "Cebu City",
  "phoneNumber": "09123456789",
  "studentIdNumber": "2024-0001"
}
```

**Expected**:
- **Status**: `201 Created`
- Response includes user object with `role: "STUDENT"`
- User's school is auto-assigned to CIT-U (check in MongoDB or `/api/auth/me`)

---

### TC-05: Registration Rejection (Invalid Email Domain)

**API Test**:
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "Fake",
  "lastName": "User",
  "email": "fake@gmail.com",
  "password": "password123",
  "address": "Somewhere"
}
```

**Expected**:
- **Status**: `400 Bad Request`
- Error message includes: "Email domain 'gmail.com' is not recognized"

---

### TC-06: Student Login + /me Endpoint

1. **Login** with the student created in TC-04:
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan@cit.edu",
  "password": "password123"
}
```

2. Copy the `token` from the response.

3. **Call /me**:
```
GET http://localhost:8080/api/auth/me
Authorization: Bearer <token>
```

**Expected**:
- Returns the logged-in user's full profile
- `role` is `"STUDENT"`
- `school.name` is "Cebu Institute of Technology - University"
- `karmaScore` is `0`

---

### TC-07: Endpoint Security (RBAC)

**Test 1 — Unauthenticated access to users list**:
```
GET http://localhost:8080/api/users
```
**Expected**: `401 Unauthorized` or `403 Forbidden`

**Test 2 — Student trying to access users list** (use student token from TC-06):
```
GET http://localhost:8080/api/users
Authorization: Bearer <student_token>
```
**Expected**: `403 Forbidden` (only ADMIN/SUPER_ADMIN can list all users)

**Test 3 — SUPER_ADMIN accessing users list** (use admin token from TC-03):
```
GET http://localhost:8080/api/users
Authorization: Bearer <admin_token>
```
**Expected**: `200 OK` with list of users

**Test 4 — Student creating a school** (should be denied):
```
POST http://localhost:8080/api/schools
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "name": "Fake School",
  "shortName": "FS",
  "city": "Cebu City",
  "emailDomain": "fake.edu"
}
```
**Expected**: `403 Forbidden` (only SUPER_ADMIN can create schools)

---

### TC-08: React Frontend — Login Page Branding

1. Open `http://localhost:5173/login`
2. **Verify**:
   - [ ] Logo says **"UniLost"** (not HulamPay)
   - [ ] Subtitle says "Your campus lost & found network." (not marketplace)
   - [ ] Right panel says "Find What's Lost. Across Campus."
   - [ ] Features mention "Verified Students" and "Multi-Campus"
   - [ ] Footer says "New to UniLost? Create Account"
   - [ ] Page title in browser tab: "UniLost - Campus Lost & Found"

---

### TC-09: React Frontend — Registration Email Domain Detection

1. Open `http://localhost:5173/register`
2. **Verify branding**:
   - [ ] Logo says **"UniLost"**
   - [ ] Subtitle: "Join the Cebu City campus lost & found network."
   - [ ] No school dropdown exists (removed)

3. **Test email domain detection**:
   - Type `test@cit.edu` in the email field
   - [ ] Green text appears: "Detected: Cebu Institute of Technology - University (CIT-U)"

   - Change to `test@usc.edu.ph`
   - [ ] Text updates to: "Detected: University of San Carlos (USC)"

   - Change to `test@gmail.com`
   - [ ] Hint appears listing supported domains: "cit.edu, usc.edu.ph, ..."

   - Clear the email field
   - [ ] No detection text shown

4. **Test registration submit** with `test@cit.edu`:
   - Fill all required fields (first name, last name, email, password, confirm password)
   - Check "I agree to the Terms & Privacy Policy"
   - Click "Join UniLost"
   - [ ] Redirected to login page on success

5. **Test registration submit** with `test@gmail.com`:
   - Fill all required fields
   - Click "Join UniLost"
   - [ ] Error: "Use your university email (e.g., name@cit.edu)"

---

### TC-10: React Frontend — Login Flow

1. Login with the student account created in TC-09
2. **Verify**:
   - [ ] Redirected to `/dashboard`
   - [ ] Header shows "UniLost" with Search icon
   - [ ] User dropdown shows user name and role badge ("Student")
   - [ ] No "Admin Panel" link visible (student role)

3. **Logout**, then login as SUPER_ADMIN (`admin@unilost.com` / `admin123456`)
4. **Verify**:
   - [ ] Header dropdown shows role badge "Super Admin"
   - [ ] "Admin Panel" link is visible in dropdown

---

### TC-11: React Frontend — Dashboard Page

1. Login and navigate to `/dashboard`
2. **Verify**:
   - [ ] Welcome message: "Welcome back, [FirstName]."
   - [ ] Stat cards show: "Items Reported", "Items Recovered", "Pending Claims", "Active Listings"
   - [ ] No marketplace terms (Revenue, Orders, Visitors)
   - [ ] Chart title: "Item Activity" (not "Revenue Overview")

---

### TC-12: React Frontend — Profile Page

1. Navigate to `/profile`
2. **Verify**:
   - [ ] Shows user initials in avatar
   - [ ] School name displayed (auto-detected from email domain)
   - [ ] Sidebar shows: Email, Student ID, School, Role, Karma Score
   - [ ] Tabs are: "My Reports", "Claims", "Recovered" (not "My Listings", "Rentals")
   - [ ] Empty state says "Report a lost item or post a found item..."
   - [ ] Button says "Report Item" (not "Post Item")

---

### TC-13: React Frontend — Settings Page

1. Navigate to `/settings`
2. Click the "Theme" tab
3. **Verify**:
   - [ ] Text says "Choose how **UniLost** looks to you" (not Hulampay)
   - [ ] Light/Dark mode toggle works

---

### TC-14: Mobile App — Login Screen (Android Emulator)

**Prerequisites**: Backend running on port 8080. Start Android emulator.

```bash
cd mobile
./gradlew installDebug
```

1. Launch the app
2. **Verify**:
   - [ ] App name in launcher/taskbar: "UniLost"
   - [ ] Login screen header: "UniLost"
   - [ ] Subtitle: "Your campus lost & found network."
   - [ ] Footer: "New to UniLost? Create Account"

3. Login with `admin@unilost.com` / `admin123456`
4. **Verify**:
   - [ ] Navigates to dashboard
   - [ ] Dashboard shows "Welcome to UniLost"

---

### TC-15: Mobile App — Registration Screen

1. From login, tap "Create Account"
2. **Verify**:
   - [ ] Header: "UniLost"
   - [ ] Subtitle: "Join the Cebu City campus lost & found network."
   - [ ] No school dropdown exists (removed)
   - [ ] Email field label: "University Email (e.g., name@cit.edu)"

3. **Test email domain detection**:
   - Type `mobile@cit.edu` in email
   - [ ] Green text: "Detected: Cebu Institute of Technology - University (CIT-U)"

   - Change to `mobile@gmail.com`
   - [ ] Gray hint listing supported domains

4. **Test registration** with valid email:
   - Fill: first name, last name, `mobiletest@usc.edu.ph`, password, confirm password
   - Tap "Join UniLost"
   - [ ] Toast: "Registration Successful"
   - [ ] Navigated back to login screen

---

## Test Results Summary

| Test ID | Description | Pass/Fail | Notes |
|---------|-------------|-----------|-------|
| TC-01 | Database name = unilost | | |
| TC-02 | 8 schools seeded | | |
| TC-03 | SUPER_ADMIN login + role in JWT | | |
| TC-04 | Valid registration (cit.edu) | | |
| TC-05 | Invalid domain rejected (gmail.com) | | |
| TC-06 | /me endpoint returns user with school | | |
| TC-07 | RBAC: student blocked from admin endpoints | | |
| TC-08 | Login page UniLost branding | | |
| TC-09 | Register email domain auto-detection | | |
| TC-10 | Login flow + role badge in header | | |
| TC-11 | Dashboard lost & found stats | | |
| TC-12 | Profile page UniLost context | | |
| TC-13 | Settings "UniLost" branding | | |
| TC-14 | Mobile login screen branding | | |
| TC-15 | Mobile registration flow | | |

---

## Tools for API Testing

- **Postman**: Import the endpoints above, set `Authorization: Bearer <token>` header
- **curl**: Use from terminal
- **Browser DevTools**: Check Network tab for frontend API calls

## Quick curl Examples

```bash
# Get all schools
curl http://localhost:8080/api/schools

# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@unilost.com","password":"admin123456"}'

# Register a student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@cit.edu","password":"test123456"}'

# Get current user (replace TOKEN)
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer TOKEN"
```
