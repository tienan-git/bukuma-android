# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zoonooz/android-sdk/tools/proguard/proguard-android.txt
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

# Bukuma
-keep class android.support.v7.widget.SearchView { *; }
-keep class jp.com.labit.bukuma.api.response.** { *; }
-keep class jp.com.labit.bukuma.model.** { *; }
-keep class jp.com.labit.bukuma.api.OmiseApi$* { *; }
-keepclassmembers class * extends jp.com.labit.bukuma.ui.viewholder.BaseObjectViewHolder {
   public <init>(android.view.ViewGroup);
}

# Retrofit
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.adapter.rxjava.CompletableHelper$**
-keepattributes Signature
-keepattributes Exceptions
-keepnames class rx.Single

# Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

# RxJava 1.x
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
  long producerIndex;
  long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
  rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# ZBar
-keepclassmembers class net.sourceforge.zbar.ImageScanner { *; }
-keepclassmembers class net.sourceforge.zbar.Image { *; }
-keepclassmembers class net.sourceforge.zbar.Symbol { *; }
-keepclassmembers class net.sourceforge.zbar.SymbolSet { *; }

# AppFlyer
-dontwarn com.google.android.gms.**
-dontwarn com.appsflyer.**
