# Animal Battle - ProGuard Rules

# Keep Compose
-dontwarn androidx.compose.**

# Keep DataStore
-dontwarn androidx.datastore.**

# Keep models (serialized/deserialized via DataStore)
-keep class com.animalbattle.game.domain.model.** { *; }

# Keep remote API client (uses org.json)
-keep class com.animalbattle.game.data.remote.** { *; }
-dontwarn org.json.**

# Lottie animations use reflection for composition
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Navigation Compose
-keepnames class * extends androidx.compose.runtime.Parcelable
-keepnames class * extends java.io.Serializable

# Keep BuildConfig for API_BASE_URL
-keep class com.animalbattle.game.BuildConfig { *; }
