# Useful obfuscated stack traces
-printmapping out.map

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# AGAPI
-keep class com.augumenta.agapi.** { *; }
-dontwarn com.augumenta.agapi.**

# Removes debug and verbose log messages
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Guava
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
