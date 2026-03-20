# Mobile Phase 10 - Testing & Deployment

> **Status:** PENDING
> **Depends On:** All previous mobile phases

---

## Objective

Perform comprehensive testing on the Android app and prepare for Google Play Store release.

---

## 1. Testing

| # | Task | Details |
|---|------|---------|
| 1 | ViewModel unit tests | JUnit + Turbine for StateFlow testing |
| 2 | Repository tests | Mock Retrofit responses with MockWebServer |
| 3 | UI tests | Compose UI testing framework (`createComposeRule`) |
| 4 | Navigation tests | Verify screen transitions and deep linking |
| 5 | Device compatibility | Test on multiple screen sizes (phone, tablet) and Android versions (API 26+) |
| 6 | End-to-end flow | Register -> post item -> claim -> handover on real device |

## 2. Polish

| # | Task | Details |
|---|------|---------|
| 1 | Loading states | Consistent shimmer/skeleton loading across all screens |
| 2 | Error handling | User-friendly error messages for network failures |
| 3 | Offline support | Graceful degradation when offline (cached data via Room) |
| 4 | Dark theme | Verify Material 3 theme works in both light and dark mode |
| 5 | Performance profiling | Check for memory leaks, janky frames using Android Profiler |

## 3. Deployment

| # | Task | Platform | Details |
|---|------|----------|---------|
| 1 | Generate signed APK/AAB | Android Studio | Release build with signing key |
| 2 | Create Play Store listing | Google Play Console | App description, screenshots, privacy policy |
| 3 | Beta testing | Google Play Console | Internal testing track for QA |
| 4 | Production release | Google Play | Publish to production after beta validation |
| 5 | ProGuard/R8 configuration | Gradle | Code shrinking and obfuscation for release |

---

## Acceptance Criteria

- [ ] All unit and UI tests pass
- [ ] App runs smoothly on Android 8.0+ (API 26+)
- [ ] No crashes or ANRs in testing
- [ ] Signed AAB is built and ready for upload
- [ ] Play Store listing is complete with screenshots and description
- [ ] Beta testing round completed with no critical issues
