# ROXIFY ProGuard / R8 rules
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# kotlinx.serialization
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keepclassmembers class kotlinx.serialization.json.** { *; }
-dontnote kotlinx.serialization.**

# Media3
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coil
-dontwarn okhttp3.**
-dontwarn okio.**
