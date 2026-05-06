# UniLost — Project CLAUDE.md

> **Session Ritual.** This file is auto-loaded at the start of every session. Read it fully, then read the vault before touching code.

---

## 0. Required Reading Before You Touch Anything

The canonical second brain for this project lives in an Obsidian vault outside the repo:

**Vault root:** `C:\Users\Theo Cedric Chan\Documents\Obsidian Vault\UniLost Vault`

**Read in this order, every session:**

1. `00-INDEX.md` — orientation + reading order
2. `01-AGENT-RULES.md` — **non-negotiable rules**, do not skip
3. `04-STATUS-ROADMAP.md` — what's actually built (the repo's own phase docs are partly stale)
4. `05-KNOWN-ISSUES.md` — drift between docs and code, accepted trade-offs

Then read the surface-specific notes for whatever you're about to change:

- Backend work → `backend/overview.md`, `backend/api.md`, `backend/data-model.md`
- Website work → `website/overview.md`, `website/design-system.md`, `website/routes-features.md`
- Mobile work → `mobile/overview.md`, `mobile/status.md`

If you cannot read the vault for any reason, **stop and tell the user**. Do not proceed from this CLAUDE.md alone — it's a loader, not a substitute.

---

## 1. What UniLost Is

A campus-scoped Lost & Found platform for 8 universities in Cebu, Philippines. Three surfaces:

| Surface | Stack | Maturity |
|---------|-------|----------|
| Backend | Spring Boot 4.0.2 + Java 21 + MongoDB Atlas | Phases 1–6 + 8 + most of 9 done. 7 folded into the claims handover state machine. |
| Website | React 19 + Vite 7 + Axios + STOMP | All pages wired to real API. Production-shaped UI. |
| Mobile  | Kotlin + Jetpack Compose + Hilt + Retrofit | **Auth wired only.** Everything else is mock UI. |

Authoritative repo paths:

- `backend/src/main/java/edu/cit/chan/unilost/` — controllers, services, entities
- `website/src/` — pages, components, services, styles
- `mobile/app/src/main/java/com/hulampay/mobile/` — note: package name is `hulampay`, not `unilost`. Don't rename without explicit instruction.

---

## 2. Hard Rules (Non-Negotiable)

These are restated from the vault for visibility. Full detail in `01-AGENT-RULES.md`.

### R1 — Don't modify existing UI/UX

The website and mobile UI are considered finished. Don't change layout, spacing, colors, fonts, components, modals, navigation, or icons unless the user **explicitly** asks for that change in the current request. Adding new screens or features is fine; restyling existing ones is not.

### R2 — Reuse the existing design system

- **Website:** use tokens from `website/src/styles/tokens.css` (spacing, radius, type, transitions) and theme variables from `themes.css`. Use existing `components/ui/` primitives (`Button`, `Card`, `Modal`, `Badge`, `Input`, `Select`, `Toast`, `Spinner`, `Skeleton`, `Dropdown`, `Alert`, `FormError`, `EmptyState`, `ConfirmDialog`). **No Tailwind, no CSS-in-JS, no component library.** No new fonts (Inter + Outfit only).
- **Mobile:** Material3 theme in `ui/theme/`. Match auth-screen spacing/typography. Don't introduce a different design system.

### R3 — Backward-compatible only

Don't rename or remove existing routes, API fields, component props, or CSS class names. Additive only. If a contract truly must break, flag it and wait for explicit approval.

### R4 — Responsive on both form factors

The website must keep working on mobile-width browsers and desktop. The mobile app must keep working in portrait. No viewport-specific regressions.

### R5 — No hallucination

Verify before you reference. The repo's `docs/` and `docs-for-application-architecture/` folders are partly out of date. The vault and the actual code are the only sources of truth. If they disagree, the code wins.

### R6 — No unrequested refactors

A bug fix is just the bug fix. A feature is just the feature. No "while I'm here" cleanup. No taste-driven renames. No framework migrations.

### R7 — Match existing code style

- **Backend:** Lombok, constructor injection, Spring conventions. Services return DTOs (mapped via `util/DtoMapper`). Controllers use `@PreAuthorize` for admin gating.
- **Website:** function components. Services under `src/services/` return `{ success, data, error }` envelopes. Page-level components live in `pages/`, reusable bits in `components/`. CSS is component-scoped — one `.css` file next to each component.
- **Mobile:** MVVM with Hilt-injected ViewModels. ViewModels expose `StateFlow<UiState<T>>` from `utils/UiState.kt`. Repositories sit between API and ViewModel.

### R8 — Toasts, not alerts

Never use `alert()` or browser `confirm()`. Use the existing `Toast` component (`useToast` hook) and `ConfirmDialog` component.

### R9 — Phase status: trust the vault, then the code

`04-STATUS-ROADMAP.md` reflects the actual code. The repo's own `PHASE-OVERVIEW.md` and `claude.md` (lowercase, in `docs-for-application-architecture/`) are partly stale. Spot-check by opening the controller/service/page in question.

---

## 3. Critical Drift to Remember

These are the gotchas where docs disagree with code:

| Topic | Reality |
|-------|---------|
| Roles | Backend `Role` enum has only `STUDENT` and `ADMIN`. Older docs mention `FACULTY` — that role does not exist. |
| Handover collection | No `HandoverEntity` exists. Handover state lives on `ClaimEntity` (`finderMarkedReturnedAt`, `ownerConfirmedReceivedAt`) and `ItemEntity.status`. |
| JWT expiration | Default is 1 hour (`jwt.expiration=3600000` in `application.properties`). Older docs say 24h. |
| Item statuses | Now: `ACTIVE, CLAIMED, PENDING_OWNER_CONFIRMATION, RETURNED, EXPIRED, TURNED_OVER_TO_OFFICE, HIDDEN`. `HANDED_OVER` is deprecated. |
| Claim statuses | Now: `PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED`. `HANDED_OVER` is deprecated. |
| `schoolService.js` (website) | Calls `/schools` — backend has no such route. Likely dead code. Use `campusService.js`. |
| Mobile `School` model vs backend `CampusEntity` | Field names overlap but aren't identical. The adapter relies on JSON shape coincidence — be careful when extending. |
| Mobile package name | `com.hulampay.mobile`, not `com.unilost.*`. Inconsistent but not to be renamed without explicit instruction. |

Full list in the vault's `05-KNOWN-ISSUES.md`.

---

## 4. Common Commands

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Required env: `MONGODB_URI`, `MONGODB_DATABASE`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`. Optional: `JWT_SECRET`, `JWT_EXPIRATION`, `CORS_ALLOWED_ORIGINS`, `ADMIN_SEED_PASSWORD`.

### Website

```powershell
cd website
npm install
npm run dev    # http://localhost:5173
```

Required env (`website/.env`): `VITE_API_URL=http://localhost:8080/api`, `VITE_GOOGLE_MAPS_API_KEY=...`.

### Mobile

```powershell
cd mobile
.\gradlew.bat assembleDebug
```

Emulator base URL is `http://10.0.2.2:8080/api/`. Cleartext traffic is enabled for development only.

---

## 5. When You Add or Change Code

1. **Re-read the relevant vault note** for the surface you're touching.
2. **Check the existing pattern** before inventing a new one. If a similar feature already exists, mirror it.
3. **Verify endpoint contracts** by reading the controller, not docs.
4. **For UI changes:** test both light and dark themes (website) and portrait orientation (mobile).
5. **For new components:** use existing primitives in `components/ui/`. Don't introduce a new dependency without flagging it.
6. **Don't bundle unrelated changes.** One concern per change.
7. **Don't leave mock data alongside live data** on mobile — when you wire a screen, remove the corresponding mock import in the same change.
8. **Update the vault** if the code change invalidates a vault claim (especially `04-STATUS-ROADMAP.md` and `05-KNOWN-ISSUES.md`).

---

## 6. When the Vault and This File Disagree

The vault wins. This file is a session loader; the vault is the curated second brain. If both disagree with the code, the code wins — and you should fix the vault note before continuing.

---

## 7. Repo Folders You Can Ignore for Most Tasks

- `target/` (backend Maven build artefacts)
- `node_modules/`, `dist/` (website)
- `build/`, `.gradle/` (mobile)
- `docs-for-application-architecture/` — partly stale; superseded by the vault. Open only to verify what was previously claimed.
- `docs/POST_PHASE4_AUDIT_REPORT.md`, `docs/FULL_AUDIT_REPORT.md`, `docs/TASK_CHECKLIST*.md` — historical, predate Phase 5+.

---

## 8. Project Owner

Solo developer (Theo). Treat the codebase as one person's project, not a team's — there is no other reviewer, no sprint cadence, no compliance gate. Default to small, reversible changes. Ask before doing anything that touches multiple surfaces at once.
