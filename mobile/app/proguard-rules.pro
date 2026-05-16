# Add project specific ProGuard rules here.
# Active when minifyEnabled = true on the release buildType.

# ---- Kotlin metadata + coroutines ----
-keep class kotlin.Metadata { *; }
-keepclassmembernames class kotlinx.coroutines.** { volatile <fields>; }

# ---- Retrofit + OkHttp ----
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Our Retrofit service interfaces (explicit, in case the generic rule misses them).
-keep interface com.hulampay.mobile.data.api.** { *; }

# ---- Gson DTOs ----
# Keep field names that Gson reflects on at runtime.
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.hulampay.mobile.data.model.** { *; }
-keep class com.hulampay.mobile.data.api.AppGson { *; }

# Internal Gson types touched via reflection.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.JsonAdapter

# ---- Hilt / Dagger ----
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep,allowobfuscation @interface dagger.hilt.android.AndroidEntryPoint
-keep,allowobfuscation @interface dagger.hilt.android.HiltAndroidApp
-keep,allowobfuscation @interface dagger.hilt.android.lifecycle.HiltViewModel
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel

# ---- Compose runtime ----
-keep class androidx.compose.runtime.** { *; }

# ---- Krossbow / STOMP ----
-keep class org.hildan.krossbow.** { *; }
-dontwarn org.hildan.krossbow.**

# ---- Coil ----
-dontwarn coil.**

# ---- Maps Compose / Play Services ----
-keep class com.google.maps.android.compose.** { *; }
-dontwarn com.google.maps.android.**
-dontwarn com.google.android.gms.**

# ---- Misc embedded annotations ----
-dontwarn javax.annotation.**

# Preserve line numbers for crash reports (release stack traces stay readable).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
