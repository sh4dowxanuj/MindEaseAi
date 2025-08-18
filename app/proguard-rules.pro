# Keep Retrofit interfaces
-keep interface com.mindeaseai.** { *; }

# Keep model classes used by Gson serialization/deserialization
-keepclassmembers class com.mindeaseai.gemini.** {
    <fields>;
}

# Retrofit / OkHttp warnings suppression
-dontwarn okio.**
-dontwarn javax.annotation.**
# ProGuard optimization rules for MindEaseAi
# Add your custom rules below

# Enable optimization
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

## Keep Application class (update to actual package)
-keep class com.mindeaseai.** { *; }

# Keep all activities, services, receivers, and providers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep annotated classes (e.g., for Gson, Retrofit, etc.)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.squareup.moshi.JsonAdapter

# --- Recommended rules for Kotlin ---
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**
-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

# --- Room ---
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# --- Gson models ---
-keep class com.mindeaseai.model.** { *; }

# --- Data Binding ---
-keep class **BR { *; }
-keep class * extends androidx.databinding.ViewDataBinding { *; }

# --- Retrofit ---
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# --- OkHttp ---
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# --- General AndroidX ---
-keep class androidx.** { *; }
-dontwarn androidx.**

# --- Keep all enums ---
-keepclassmembers enum * { *; }

# --- Keep all Parcelable implementations ---
-keep class * implements android.os.Parcelable { *; }
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Add more rules as needed for your libraries
