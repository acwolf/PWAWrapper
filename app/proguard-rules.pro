# Keep WebView and JavaScript interfaces for PWA Authentication
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Prevent obfuscation of WebView classes used for Auth
-keep class android.webkit.** { *; }
-keep class androidx.core.splashscreen.** { *; }

# If you use any native libraries or custom Chrome Clients
-keepattributes EnclosingMethod, InnerClasses, *Annotation*