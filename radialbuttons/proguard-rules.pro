# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all public classes and methods in the library
-keep public class com.radialbuttons.** { public *; }

# Keep Compose-related classes
-dontwarn androidx.compose.**
