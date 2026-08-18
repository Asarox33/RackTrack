# RackTrack — R8 / ProGuard (release)

# Readable Play Console stacks after mapping upload
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,*Annotation*,Signature
-renamesourcefileattribute SourceFile

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# Compose / ViewModel: keep names used via reflection in AndroidX
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Billing + AdMob ship consumer rules; keep entry points we call by type
-keep class com.android.billingclient.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# Local JSON history uses org.json; keep if pulled from Android SDK stubs oddly
-dontwarn org.json.**
