# Mobile App Architecture

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 1.9.10 | Language |
| Android SDK | 34 (target 33, min 21) | Platform |
| Jetpack Compose | BOM 2023.03.00 | UI framework |
| Material3 | (via BOM) | Design system |
| Hilt | 2.48 | Dependency injection |
| Retrofit | 2.9.0 | HTTP client |
| Room | 2.5.2 | Local database |
| DataStore | 1.0.0 | Preferences (token storage) |
| Glide | 4.16.0 | Image loading |
| Coroutines | 1.6.4 | Async operations |
| Navigation Compose | 2.5.3 | Screen navigation |

**App ID**: `com.hulampay.mobile`  
**Compose Compiler**: 1.5.3  
**KSP**: 1.9.0-1.0.12

## Package Structure

```
com.hulampay.mobile
├── App.kt                          # @HiltAndroidApp
├── di/
│   ├── AppModule.kt                # Provides ApplicationScope CoroutineScope
│   ├── NetworkModule.kt            # Provides OkHttpClient, Retrofit, API services
│   └── DatabaseModule.kt           # Provides Room database + DAOs
├── data/
│   ├── api/
│   │   ├── AuthApiService.kt       # Retrofit auth endpoints + AuthResponse data class
│   │   └── AuthInterceptor.kt      # Injects JWT from DataStore into requests
│   ├── remote/
│   │   └── AppApi.kt               # General API endpoints (template)
│   ├── entity/
│   │   └── ExampleEntity.kt        # Room entity template
│   ├── dao/
│   │   └── ExampleDao.kt           # Room DAO template
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database (version 1)
│   │   └── Converters.kt           # Room type converters (Long ↔ Date)
│   ├── model/
│   │   ├── User.kt                 # Full user model
│   │   ├── School.kt               # { schoolId, name, shortName, city, emailDomain }
│   │   ├── Resource.kt             # Sealed class: Loading, Success<T>, Error
│   │   └── ExampleModel.kt         # Template model
│   ├── mock/
│   │   ├── MockItems.kt            # MockItem data class + 9 categories, 8 schools
│   │   ├── MockClaims.kt           # MockClaim + helper methods
│   │   ├── MockNotifications.kt    # MockNotification + 7 notification types
│   │   └── MockAdminData.kt        # Admin stats, users, items
│   └── repository/
│       ├── AuthRepository.kt       # @Singleton — login, register, logout, getSchools
│       └── ExampleRepository.kt    # Template repository
├── navigation/
│   ├── Screen.kt                   # Sealed class — all screen routes
│   └── NavGraph.kt                 # Compose navigation graph
├── ui/
│   ├── activitys/
│   │   └── MainActivity.kt         # Single activity, @AndroidEntryPoint
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt       # @HiltViewModel
│   │   ├── RegisterScreen.kt
│   │   └── RegisterViewModel.kt    # @HiltViewModel
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   ├── home/
│   │   ├── MainScreen.kt
│   │   ├── HomeScreenState.kt      # Home screen UI state
│   │   └── HomeViewModel.kt
│   ├── detail/
│   │   ├── DetailScreen.kt
│   │   ├── DetailScreenState.kt    # Detail screen UI state
│   │   └── DetailViewModel.kt
│   ├── items/
│   │   ├── ItemFeedScreen.kt
│   │   ├── ItemDetailScreen.kt
│   │   ├── PostItemScreen.kt
│   │   ├── MyItemsScreen.kt
│   │   ├── MyClaimsScreen.kt
│   │   ├── ClaimDetailScreen.kt
│   │   ├── NotificationsScreen.kt
│   │   └── AdminScreen.kt
│   ├── profile/
│   │   ├── ProfileScreen.kt
│   │   └── ProfileViewModel.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   ├── components/
│   │   ├── AuthComponents.kt       # AuthInput, PrimaryButton
│   │   └── BottomNavBar.kt         # Feed, MyItems, Post, Alerts, Profile
│   └── theme/
│       ├── Color.kt                # Slate600, Sage, ErrorRed, Slate100
│       ├── Theme.kt                # Material3 theme
│       └── Type.kt                 # Typography
├── utils/
│   ├── UiStates.kt                 # Sealed: Idle, Loading, Success<T>, Error
│   └── TokenManager.kt             # DataStore — jwt_token, user_role
└── util/
    ├── Constants.kt                # BASE_URL = "http://10.0.2.2:8080/api/"
    ├── Extensions.kt              # View.show/gone, Context.toast, String.debug
    └── NetworkUtil.kt              # Connectivity check: WiFi, Cellular, Ethernet
```

## Architecture Pattern: MVVM

```
Screen (Compose) → ViewModel → Repository → API Service / DAO
                  ↕ StateFlow     ↕ Result<T>
              UI observes      Wraps success/error
```

**State Management**: `StateFlow` + `UiState<T>` sealed class

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## Dependency Injection (Hilt)

### NetworkModule (`@Module @InstallIn(SingletonComponent)`)
- Provides `TokenManager` (singleton)
- Provides `AuthInterceptor` (injects JWT from DataStore into requests)
- Provides `HttpLoggingInterceptor` (BODY level)
- Provides `OkHttpClient` (auth + logging interceptors)
- Provides `Retrofit` (base URL from Constants, Gson converter)
- Provides `AuthApiService` and `AppApi` instances

### DatabaseModule (`@Module @InstallIn(SingletonComponent)`)
- Provides `AppDatabase` (Room, destructive migration fallback)
- Provides `ExampleDao`

### AppModule (`@Module @InstallIn(SingletonComponent)`)
- Provides `ApplicationScope` `CoroutineScope` with `SupervisorJob`

## Networking

### AuthApiService (Retrofit)

```kotlin
@POST("auth/login")
suspend fun login(@Body credentials: Map<String, String>): Response<AuthResponse>

@POST("auth/register")
suspend fun register(@Body registrationData: Map<String, Any>): Response<AuthResponse>

@GET("schools")
suspend fun getSchools(): Response<List<School>>
```

### AuthRepository

```kotlin
suspend fun login(email, password): Result<AuthResponse>
  // On success: saves token + role to DataStore

suspend fun register(registrationData: Map<String, Any>): Result<AuthResponse>
  // On success: saves token + role to DataStore

suspend fun logout()
  // Clears token + role from DataStore

suspend fun getSchools(): Result<List<School>>
```

### AuthInterceptor
- Reads token from `TokenManager` (DataStore)
- Adds `Authorization: Bearer {token}` to all requests
- Runs in coroutine blocking context

### Base URL
- `http://10.0.2.2:8080/api/` (Android emulator → host localhost)
- Cleartext traffic enabled in AndroidManifest for emulator dev

## Token Management (DataStore)

```kotlin
class TokenManager(context: Context) {
    private val dataStore = context.dataStore  // Preferences DataStore
    
    val token: Flow<String?>    // Key: "jwt_token"
    val role: Flow<String?>     // Key: "user_role"
    
    suspend fun saveToken(token: String)
    suspend fun saveRole(role: String)
    suspend fun clearToken()    // Clears both token and role
}
```

## Local Database (Room)

```kotlin
@Database(entities = [ExampleEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getExampleDao(): ExampleDao
}
```

Currently a template setup — real entities will be added in future phases.

## UI Components

### AuthComponents.kt
- **AuthInput**: `OutlinedTextField` with leading icon, 12dp rounded corners
- **PrimaryButton**: 50dp height, `CircularProgressIndicator` when loading

### BottomNavBar.kt
5 tabs: Feed, MyItems, Post, Alerts, Profile  
Active state detected via `currentBackStackEntryAsState()`

### Theme Colors
| Name | Hex | Usage |
|------|-----|-------|
| Slate600 | #475569 | Primary |
| Sage | #84a98c | Secondary |
| ErrorRed | #ef4444 | Error |
| Slate100 | #f1f5f9 | Background |

## Permissions

- `INTERNET` — API calls
- `ACCESS_NETWORK_STATE` — Connectivity checks
- `usesCleartextTraffic=true` — Emulator localhost (disable in production)

## Build & Run

```bash
cd mobile
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install on connected device/emulator
```

## Development Status

| Feature | UI Ready | API Connected |
|---------|----------|---------------|
| Login/Register | ✅ | ✅ (auth endpoints) |
| Dashboard | ✅ | ❌ (mock data) |
| Item Feed/Detail | ✅ | ❌ (mock data) |
| Post Item | ✅ | ❌ (mock data) |
| Claims | ✅ | ❌ (mock data) |
| Notifications | ✅ | ❌ (mock data) |
| Admin | ✅ | ❌ (mock data) |
| Profile/Settings | ✅ | ❌ (mock data) |
