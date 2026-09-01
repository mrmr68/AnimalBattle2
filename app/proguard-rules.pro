# Animal Battle - ProGuard Rules

# Keep Compose
-dontwarn androidx.compose.**

# Keep DataStore
-dontwarn androidx.datastore.**

# Keep models
-keep class com.animalbattle.game.domain.model.** { *; }
