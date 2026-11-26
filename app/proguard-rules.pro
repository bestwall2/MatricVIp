-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Picasso
-dontwarn com.squareup.picasso.**
-keep class com.squareup.picasso.** { *; }

# Media3
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }

# App models & holders
-keep class com.matricvip.Config { *; }
-keep class com.matricvip.Channel { *; }
-keep class com.matricvip.UrlHolder { *; }
-keep class com.matricvip.SubscriptionClient$** { *; }

# Common optional platform annotations
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
