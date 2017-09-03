# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\Android SDK for Android Studio/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

# Keep device implementation classes in "obj" package
# This is needed because they are created using reflection
-keep class eu.power_switch.obj.** {
    *;
}

### External Libraries

# AboutLibraries
-keep class .R
-keep class **.R$* {
    <fields>;
}

# Dagger 2
-dontwarn com.google.errorprone.annotations.*

