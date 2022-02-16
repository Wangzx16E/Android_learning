# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#############################################
#
# 对于一些基本指令的添加
#
#############################################

#代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5
#混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames
#指定不去忽略非公共库的类
#-dontskipnonpubliclibraryclasses
#这句话能够使我们的项目混淆后产生映射文件
#包含有类名->混淆后类名的映射关系
-verbose
#指定不去忽略非公共库的类
#-dontskipnonpubliclibraryclassmembers
#不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify
#保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses
#避免混淆泛型
-keepattributes Signature
#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
#指定混淆是采用的算法，后面的参数是一个过滤器
#这个过滤器是谷歌推荐的算法，一般不做更改
#-optimizations !code/simplification/cast,!field/*,!class/merging/*

 # 忽略警告，避免打包时某些警告出现
 # -ignorewarning

#############################################
#
# Android开发中一些需要保留的公共部分
#
#############################################

#保留我们使用的四大组件，自定义的Application等等这些类不被混淆
#因为这些子类都有可能被外部调用
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

#保留support下的所有类及其内部类
-keep class android.support.** {*;}
-keep class android.arch.** {*;}
#保留R下面的资源
-keep class **.R$* {*;}
#保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
#保留在Activity中的方法参数是view的方法，
#这样以来我们在layout中写的onClick就不会被影响
-keepclassmembers class * extends android.app.Activity{
  public <fields>;
  public <methods>;
}
#保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#对于带有回调函数的onXXEvent的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
}

-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}
#------------------  下方是共性的排除项目         ----------------
# 方法名中含有“JNI”字符的，认定是Java Native Interface方法，自动排除
# 方法名中含有“JRI”字符的，认定是Java Reflection Interface方法，自动排除

-keepclasseswithmembernames class *{
	native <methods>;
}

-keepclasseswithmembers class * {
     *JNI*(...);
}
-keepclasseswithmembernames class * {
	 *JRI*(...);
}
-keep class **JNI* {*;}

##--------------- Gson  ----------
## Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
#
##--------------- okhttp3  ----------
#-dontwarn com.squareup.okhttp3.**
#-keep class com.squareup.okhttp3.** { *;}
#-dontwarn okio.**
#
###--------------- adjust  ----------
#-keep class com.adjust.sdk.** { *; }
#-keep class com.google.android.gms.common.ConnectionResult {
#    int SUCCESS;
#}
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
#    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
#}
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
#    java.lang.String getId();
#    boolean isLimitAdTrackingEnabled();
#}
#-keep public class com.android.installreferrer.** { *; }
#
###--------------- google  ----------
#-keep class com.android.** {*;}
#-keep class com.google.** {*;}
#-keep class com.facebook.** {*;}
#-keep class bolts.** {*;}
#-keep org.hamcrest.** {*;}
#
##------------------  mediationsdk-6.17.0   ----------------
#-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface {
#    public *;
#}
#-keepclassmembers class * implements android.os.Parcelable {
#    public static final android.os.Parcelable$Creator *;
#}
#-keep public class com.google.android.gms.ads.** {
#   public *;
#}
#-keep class com.ironsource.adapters.** { *;
#}
#-dontwarn com.ironsource.mediationsdk.**
#-dontwarn com.ironsource.adapters.**
#-keepattributes JavascriptInterface
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
#
##------------------ Facebook adapter ------------------
#-dontwarn com.facebook.ads.internal.**
#-keeppackagenames com.facebook.*
#-keep public class com.facebook.ads.**{ public protected *; }
#
##------------------ AdColony adapter ------------------
## For communication with AdColony's WebView
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
## Keep ADCNative class members unobfuscated
#-keepclassmembers class com.adcolony.sdk.ADCNative** {
#    *;
# }

#------------------  自定义排除项目      ----------------
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}