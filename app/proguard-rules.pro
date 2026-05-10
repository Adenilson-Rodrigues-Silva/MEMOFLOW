# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# SQLCipher rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keep class net.zetetic.** { *; }

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep class com.arsdevstudio.memoflow.data.model.** { *; }
-keep class com.arsdevstudio.memoflow.data.local.entity.** { *; }

# Retrofit & OkHttp rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Firebase & Google Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Google Drive API
-keep class com.google.api.** { *; }
-keep class com.google.apis.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.common.**

# Keep line numbers for crash reporting (optional but recommended)
-keepattributes SourceFile,LineNumberTable

# Lottie rules
-keep class com.airbnb.lottie.** { *; }

# RichEditor rules
-keep class com.mohamedrejeb.richeditor.** { *; }

# Billing Library
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Keep Compose-specific things if needed (usually handled by the compiler)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
