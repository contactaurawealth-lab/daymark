# Proguard rules for Daymark
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
