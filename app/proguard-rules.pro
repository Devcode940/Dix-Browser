# ProGuard / R8 Rules for Dix Browser

# Keep WebView related classes (important for browser apps)
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *;
}

# Keep JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep custom WebView classes
-keep class com.devcode940.web.easybrowser.web.** { *; }

# Keep Kotlin data classes used in serialization
-keep class com.devcode940.web.easybrowser.ui.session.SavedTab { *; }

# Keep EncryptedSharedPreferences classes
-keep class androidx.security.crypto.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep ViewBinding classes
-keep class com.devcode940.web.databinding.** { *; }

# General optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*