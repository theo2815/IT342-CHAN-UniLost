# Phase 10 - Testing, Polish & Deployment (Backend + Website)

> **Status:** PENDING
> **Priority:** MUST HAVE
> **Depends On:** All previous phases

---

## Objective

Perform comprehensive testing on backend and website, fix remaining issues, optimize performance, and deploy to production.

---

## 1. Testing

### Backend Testing (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Unit tests for all services | JUnit 5 + Mockito for service layer |
| 2 | Integration tests for controllers | `@SpringBootTest` + MockMvc for API endpoints |
| 3 | Repository tests | `@DataMongoTest` for MongoDB queries |
| 4 | Security tests | Verify JWT validation, role enforcement, campus scoping |
| 5 | Edge case coverage | Duplicate entries, expired tokens, invalid inputs |

### Website Testing (React)

| # | Task | Details |
|---|------|---------|
| 1 | Component unit tests | Vitest + React Testing Library |
| 2 | API service mocking | MSW (Mock Service Worker) for API tests |
| 3 | Route guard tests | Verify protected/admin routes redirect correctly |
| 4 | Form validation tests | All input forms handle errors properly |
| 5 | Cross-browser testing | Chrome, Firefox, Safari, Edge |

### End-to-End Testing

| # | Task | Details |
|---|------|---------|
| 1 | Full user journey | Register -> post item -> claim -> handover |
| 2 | Admin moderation flow | Flag -> review -> action lifecycle |

---

## 2. Bug Fixes & Polish

| # | Task | Details |
|---|------|---------|
| 1 | Fix all known naming mismatches | ACCEPTED vs APPROVED, category enums |
| 2 | Implement GlobalExceptionHandler | Replace all inline try-catch in controllers (if not done in Phase 3) |
| 3 | Add JWT refresh token rotation | Extend session without re-login |
| 4 | Add email verification endpoint | `POST /api/auth/verify-email` |
| 5 | Add rate limiting on auth endpoints | Prevent brute force attacks |
| 6 | Remove `UserProbe.java` | Clean up diagnostic tool |
| 7 | Loading states and error boundaries | Consistent UX for all async operations |
| 8 | Responsive design audit | All web pages work on mobile viewport |
| 9 | Accessibility audit | ARIA labels, keyboard navigation, color contrast |
| 10 | Image optimization | Lazy loading, proper sizing, WebP format |

---

## 3. Performance Optimization

| # | Task | Details |
|---|------|---------|
| 1 | MongoDB indexing | Create indexes for frequently queried fields |
| 2 | API response pagination | Ensure all list endpoints are paginated |
| 3 | Image CDN setup | Serve images via CDN for faster delivery |
| 4 | Frontend bundle optimization | Code splitting, tree shaking, lazy routes |
| 5 | API response caching | Cache leaderboard and campus data |
| 6 | Database connection pooling | Optimize MongoDB connection settings |

---

## 4. Security Hardening

| # | Task | Details |
|---|------|---------|
| 1 | Input sanitization audit | Prevent XSS in all user inputs |
| 2 | API rate limiting | Limit requests per IP/user |
| 3 | CORS configuration for production | Lock down to production domains only |
| 4 | Environment variable management | No secrets in code; use env files or vault |
| 5 | HTTPS enforcement | SSL/TLS for all endpoints |
| 6 | Content Security Policy headers | Prevent script injection |
| 7 | File upload validation | Restrict file types and sizes server-side |

---

## 5. Deployment

### Backend Deployment

| # | Task | Platform | Details |
|---|------|----------|---------|
| 1 | Containerize with Docker | Docker | Create `Dockerfile` + `docker-compose.yml` |
| 2 | Deploy to cloud | Railway / Render / AWS | Spring Boot JAR deployment |
| 3 | MongoDB Atlas production cluster | MongoDB Atlas | Configure production database |
| 4 | Environment configuration | All | Production env variables (JWT secret, DB URI, SMTP, etc.) |
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
- [ ] All known naming mismatches are resolved
- [ ] Application loads in under 3 seconds on standard connection
- [ ] Security audit passes with no critical vulnerabilities
- [ ] Backend is deployed and accessible via public URL
- [ ] Website is deployed and accessible via custom domain
- [ ] CI/CD pipeline automatically builds and tests on push
- [ ] All documentation is complete and up to date
