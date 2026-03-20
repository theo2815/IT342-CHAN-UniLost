# Phase 10 - Testing, Polish & Deployment (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE
> **Depends On:** All previous phases

---

## Objective

Perform comprehensive testing on backend and website, fix remaining issues, optimize performance, and deploy to production.

---

## Already Completed (from earlier phases and audits)

The following items originally planned for Phase 10 have already been implemented:

| Item | Completed In | Details |
|------|-------------|---------|
| `GlobalExceptionHandler.java` | Phase 3 | Handles Auth, Forbidden, NotFound, IllegalArg, Validation, MaxUploadSize, OptimisticLocking, RuntimeException |
| Rate limiting on auth endpoints | Phase 1 | 3-tier rate limiting: auth 10/min, write 30/min, read 60/min per IP with sliding window |
| Naming mismatches (ACCEPTED vs APPROVED) | Phase 4 Audit | Backend uses `ACCEPTED`/`REJECTED` consistently; `StatusBadge` handles both for display |
| MongoDB indexing | Phases 3-6 | Comprehensive indexes on items, claims, chats, messages collections via `MongoIndexInitializer.java` |
| `@JsonIgnore` on sensitive fields | Phase 2 | `passwordHash`, `passwordResetToken`, `passwordResetExpiry` excluded from API responses |
| Entity enums | Phase 3 | `ItemStatus`, `Role`, `AccountStatus`, `ClaimStatus`, `ItemType` — all entities use enums (not strings) |
| Pagination on list endpoints | Phases 3-5 | Items, claims, users, admin endpoints all paginated |
| File upload validation | Phase 3 | Cloudinary + `MaxUploadSizeExceededException` handling (10MB per file, 30MB total) |
| JWT client-side expiry check | Phase 1 | `authService.isAuthenticated()` decodes JWT and checks expiry; malformed tokens return false + clear storage |
| Optimistic locking | Phase 4 | `@Version` on `ClaimEntity` prevents concurrent claim modification |

---

## 1. Testing

### Backend Testing (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Unit tests for all services | JUnit 5 + Mockito for service layer (UserService, ItemService, ClaimService, ChatService, AdminService) |
| 2 | Integration tests for controllers | `@SpringBootTest` + MockMvc for all API endpoints |
| 3 | Repository tests | `@DataMongoTest` for MongoDB queries and index verification |
| 4 | Security tests | JWT validation, role enforcement (`STUDENT`/`ADMIN`/`FACULTY`), campus scoping, chat participation checks |
| 5 | WebSocket tests | STOMP CONNECT auth, message broadcast, unauthorized SUBSCRIBE/SEND rejection |
| 6 | Edge case coverage | Duplicate claims, expired tokens, concurrent handovers, rate limit boundaries |

### Website Testing (React)

| # | Task | Details |
|---|------|---------|
| 1 | Component unit tests | Vitest + React Testing Library |
| 2 | API service mocking | MSW (Mock Service Worker) for API tests |
| 3 | Route guard tests | Verify `ProtectedRoute` and `AdminRoute` redirect correctly |
| 4 | Form validation tests | PostItem, ClaimModal, Register, Settings forms |
| 5 | WebSocket integration tests | STOMP connection, message receive, reconnection |
| 6 | Cross-browser testing | Chrome, Firefox, Safari, Edge |

### End-to-End Testing

| # | Task | Details |
|---|------|---------|
| 1 | Full user journey | Register -> post item -> claim -> chat -> handover |
| 2 | Admin moderation flow | Flag -> review -> suspend user / delete item |
| 3 | Multi-user messaging | Claim triggers chat creation, real-time message exchange |

---

## 2. Bug Fixes & Polish (Remaining Items)

| # | Task | Details |
|---|------|---------|
| 1 | ~~Fix naming mismatches~~ | ~~DONE in Phase 4 audit~~ |
| 2 | ~~GlobalExceptionHandler~~ | ~~DONE in Phase 3~~ |
| 3 | Add JWT refresh token rotation | Extend session without re-login (deferred from Phase 1) |
| 4 | Add email verification endpoint | `POST /api/auth/verify-email` (deferred from Phase 1) |
| 5 | ~~Rate limiting~~ | ~~DONE in Phase 1~~ |
| 6 | Remove `UserProbe.java` | Clean up diagnostic tool (currently disabled — `@Component` commented out) |
| 7 | Loading states and error boundaries | Audit all pages for consistent loading/error UX |
| 8 | Responsive design audit | All web pages work on mobile viewport (Messages page already responsive) |
| 9 | Accessibility audit | ARIA labels, keyboard navigation, color contrast |
| 10 | Image optimization | Lazy loading, proper sizing, WebP format via Cloudinary transforms |

---

## 3. Performance Optimization

| # | Task | Details |
|---|------|---------|
| 1 | ~~MongoDB indexing~~ | ~~DONE across Phases 3-6 via MongoIndexInitializer~~ |
| 2 | ~~API response pagination~~ | ~~DONE on all list endpoints~~ |
| 3 | Image CDN setup | Cloudinary already handles storage; configure delivery optimization |
| 4 | Frontend bundle optimization | Code splitting, tree shaking, lazy routes |
| 5 | API response caching | Cache leaderboard and campus data |
| 6 | Database connection pooling | Optimize MongoDB Atlas connection settings |

---

## 4. Security Hardening

| # | Task | Details |
|---|------|---------|
| 1 | Input sanitization audit | Prevent XSS in all user inputs |
| 2 | ~~API rate limiting~~ | ~~DONE in Phase 1 (3-tier sliding window)~~ |
| 3 | CORS configuration for production | Lock down to production domains only (currently open for dev) |
| 4 | Environment variable management | No secrets in code; use env files or vault (Cloudinary, JWT secret, MongoDB URI, Brevo SMTP) |
| 5 | HTTPS enforcement | SSL/TLS for all endpoints |
| 6 | Content Security Policy headers | Prevent script injection |
| 7 | ~~File upload validation~~ | ~~DONE in Phase 3 (10MB/file, 30MB total)~~ |
| 8 | ~~WebSocket authentication~~ | ~~DONE in Phase 6 (JWT on STOMP CONNECT, blocks unauthorized SUBSCRIBE/SEND)~~ |

---

## 5. Deployment

### Backend Deployment

| # | Task | Platform | Details |
|---|------|----------|---------|
| 1 | Containerize with Docker | Docker | Create `Dockerfile` + `docker-compose.yml` |
| 2 | Deploy to cloud | Railway / Render / AWS | Spring Boot JAR deployment |
| 3 | MongoDB Atlas production cluster | MongoDB Atlas | Already using Atlas — configure production tier |
| 4 | Environment configuration | All | Production env variables (JWT secret, DB URI, Brevo SMTP, Cloudinary keys) |
| 5 | CI/CD pipeline | GitHub Actions | Auto-build, test, and deploy on push to main |

### Website Deployment

| # | Task | Platform | Details |
|---|------|----------|---------|
| 1 | Production build optimization | Vite | `npm run build` with production config |
| 2 | Deploy to hosting | Vercel / Netlify | Static site deployment with environment variables |
| 3 | Custom domain setup | DNS | Configure domain + SSL certificate |
| 4 | CDN configuration | Cloudflare | Edge caching for static assets |

---

## 6. Documentation

| # | Task | Details |
|---|------|---------|
| 1 | API documentation | Swagger/OpenAPI spec for all endpoints |
| 2 | User guide | How to use the platform (for students/faculty) |
| 3 | Admin guide | How to use the moderation dashboard |
| 4 | Developer setup guide | How to run the project locally |

---

## Acceptance Criteria

- [ ] All unit and integration tests pass with >80% coverage
- [ ] No critical or high-severity bugs remain
- [x] All known naming mismatches are resolved
- [ ] Application loads in under 3 seconds on standard connection
- [x] Rate limiting implemented on all endpoint tiers
- [x] GlobalExceptionHandler handles all error types
- [x] MongoDB indexes created for all frequently queried fields
- [ ] Security audit passes with no critical vulnerabilities
- [ ] Backend is deployed and accessible via public URL
- [ ] Website is deployed and accessible via custom domain
- [ ] CI/CD pipeline automatically builds and tests on push
- [ ] All documentation is complete and up to date
