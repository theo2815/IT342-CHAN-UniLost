# Backend Architecture

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 4.0.2 | Framework |
| MongoDB | Atlas (cloud) | Database |
| Spring Security | (via starter) | Auth & authorization |
| jjwt | 0.11.5 | JWT token generation/validation |
| Spring Mail | (via starter) | Gmail SMTP for OTP emails |
| Lombok | (latest) | Boilerplate reduction |
| Maven | (wrapper) | Build tool |

## Package Structure

```
edu.cit.chan.unilost
├── UniLostApplication.java         # @SpringBootApplication entry point
├── UserProbe.java                  # Diagnostic tool (deactivated — @Component commented out)
├── config/
│   ├── SecurityConfig.java         # Spring Security filter chain, CORS, auth rules
│   ├── MongoConfig.java            # MongoDB configuration
│   └── DataSeeder.java             # CommandLineRunner: seeds 8 campuses + admin account
├── controller/
│   ├── AuthController.java         # /api/auth/** — register, login, password reset
│   ├── UserController.java         # /api/users/** — CRUD
│   └── CampusController.java       # /api/campuses/** — CRUD
├── service/
│   ├── UserService.java            # Auth logic, password reset, OTP, user CRUD
│   ├── CampusService.java          # Campus CRUD with GeoJSON
│   └── EmailService.java           # Gmail SMTP for OTP delivery
├── repository/
│   ├── UserRepository.java         # MongoRepository<UserEntity, String>
│   ├── CampusRepository.java
│   ├── ItemRepository.java
│   ├── ClaimRepository.java
│   ├── ChatRepository.java
│   ├── MessageRepository.java
│   ├── HandoverRepository.java
│   └── NotificationRepository.java
├── entity/
│   ├── UserEntity.java             # users collection
│   ├── CampusEntity.java           # campuses collection
│   ├── ItemEntity.java             # items collection
│   ├── ClaimEntity.java            # claims collection
│   ├── ChatEntity.java             # chats collection
│   ├── MessageEntity.java          # messages collection
│   ├── HandoverEntity.java         # handovers collection
│   └── NotificationEntity.java     # notifications collection
├── dto/
│   ├── UserDTO.java
│   ├── CampusDTO.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── AuthResponse.java
├── filter/
│   └── JwtAuthenticationFilter.java  # OncePerRequestFilter — extracts & validates JWT
└── util/
    └── JwtUtils.java                # Token generation, parsing, validation (HS256)
```

## Security Configuration

### SecurityConfig.java

```
CSRF: Disabled (stateless API)
Sessions: STATELESS
CORS Origins: http://localhost:5173, http://localhost:3000
CORS Methods: GET, POST, PUT, DELETE, OPTIONS
Password Encoder: BCryptPasswordEncoder
```

### Authorization Rules

| Pattern | Access |
|---------|--------|
| `/api/auth/**` | Public |
| GET `/api/campuses`, GET `/api/campuses/**` | Public |
| POST/PUT/DELETE `/api/campuses/**` | ADMIN only |
| `/api/admin/**` | ADMIN only |
| GET `/api/users` | ADMIN or FACULTY |
| All other endpoints | Authenticated |

### JWT Flow

1. `AuthController.login()` → validates credentials via `UserService.authenticate()`
2. `JwtUtils.generateToken(email, role)` → HS256 signed, 24h expiry
3. Response: `AuthResponse { token, type: "Bearer", user: UserDTO }`
4. Client includes `Authorization: Bearer {token}` on all requests
5. `JwtAuthenticationFilter` (runs before UsernamePasswordAuthenticationFilter):
   - Extracts token from Authorization header
   - Validates via `JwtUtils.validateToken()`
   - Extracts email + role from claims
   - Sets `SecurityContextHolder` with `UsernamePasswordAuthenticationToken`
   - Builds `GrantedAuthority` from role (e.g., `ROLE_STUDENT`)

### JWT Token Structure

```json
{
  "sub": "user@cit.edu",
  "role": "STUDENT",
  "iat": 1710266400000,
  "exp": 1710352800000
}
```

## Configuration (application.properties)

```properties
spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=${MONGODB_DATABASE}
jwt.secret=${JWT_SECRET:UniLostSuperSecretKeyForJWTTokenSigningMustBe256BitsLongAtLeast!2025}
jwt.expiration=${JWT_EXPIRATION:86400000}  # 24 hours
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

> Environment variables override defaults. JWT secret and mail credentials are required for production.

## Data Seeding (DataSeeder.java)

Runs on startup via `CommandLineRunner`. Idempotent — checks existence before inserting.

**Seeded Campuses** (8):

| ID | Name | Email Domain | Coordinates [lng, lat] |
|----|------|-------------|----------------------|
| CIT-U-MAIN | Cebu Institute of Technology - University | cit.edu | [123.8779, 10.2948] |
| USC-MAIN | University of San Carlos - Main | usc.edu.ph | [123.8988, 10.3001] |
| USJR-MAIN | University of San Jose - Recoletos | usjr.edu.ph | [123.8961, 10.2995] |
| UC-MAIN | University of Cebu - Main | uc.edu.ph | [123.9000, 10.3020] |
| UP-CEBU | University of the Philippines Cebu | up.edu.ph | [123.8853, 10.3231] |
| SWU-MAIN | Southwestern University PHINMA | swu.edu.ph | [123.8930, 10.3060] |
| CNU-MAIN | Cebu Normal University | cnu.edu.ph | [123.8920, 10.3050] |
| CTU-MAIN | Cebu Technological University - Main | ctu.edu.ph | [123.8975, 10.2935] |

**Seeded Admin Account**:
- Email: `admin@cit.edu`, Password: `admin123456` (BCrypt)
- Role: ADMIN, Campus: CIT-U-MAIN, Verified: true

## Error Handling

No `GlobalExceptionHandler` yet. Controllers use try-catch with:
- `400` — Invalid input / domain not recognized
- `401` — Failed auth / invalid JWT
- `403` — Insufficient permissions
- `404` — Resource not found
- `409` — Email already registered

## Email Service

`EmailService.sendPasswordResetOtp(email, otp)`:
- Sends HTML-formatted email via Gmail SMTP (STARTTLS, port 587)
- Subject: "UniLost - Password Reset Code"
- Body: Professional template with 6-digit OTP and 10-minute expiry notice
- Uses `MimeMessageHelper` with UTF-8

## Build & Run

```bash
cd backend
./mvnw spring-boot:run          # Linux/Mac
.\mvnw.cmd spring-boot:run      # Windows
```

Required environment variables: `MONGODB_URI`, `MONGODB_DATABASE`, `MAIL_USERNAME`, `MAIL_PASSWORD`
