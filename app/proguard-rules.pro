# Keep Retrofit interface method signatures (Retrofit uses reflection to build them)
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp internals needed at runtime
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Room entity and DAO classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep Gson DTOs (reflection-based serialization)
-keep class com.nagaraju.stocktracker.core.network.dto.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Coroutines internals
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
