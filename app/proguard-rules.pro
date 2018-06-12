# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/edgargomez/Library/Android/sdk/tools/proguard/proguard-android.txt
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

#
# Keep source file names, line numbers
#
-keepattributes SourceFile,LineNumberTable


#
# Google
#
-dontwarn com.google.android.gms.internal.**

#
# EventBus
#
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#
# AVLoadingIndicatorView
#
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

#
# MPAndroidChart
#
-keep class com.github.mikephil.charting.** { *; }

#
# Fresco
#
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

#
# OkHttp
#
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# -dontwarn com.android.volley.toolbox.**
# -dontwarn com.facebook.infer.**

#
# HockeyApp
#
-keep public class net.hockeyapp.android.utils.* { public *; }
-dontwarn net.hockeyapp.android.utils.**



#
# Parse
#
#-keepnames class com.parse.** { *; }
#-keepattributes *Annotation*
#-keepattributes Signature
#-dontwarn android.net.SSLCertificateSocketFactory
#-dontwarn android.app.Notification
#-dontwarn com.squareup.**
#-dontwarn okio.**
#-dontwarn com.parse.**