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

# WorkManager is pulled in by Play services / ads. Room creates WorkDatabase_Impl
# via reflection at androidx.startup — R8 must keep the generated Impl + ctor
# (otherwise: Unable to get provider InitializationProvider / Failed to create WorkDatabase).
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-dontwarn androidx.work.**
-dontwarn androidx.room.**

# Local JSON history uses org.json; keep if pulled from Android SDK stubs oddly
-dontwarn org.json.**
