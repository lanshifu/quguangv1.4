# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in e:\Users\Administrator\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
-ignorewarnings

# 这里根据具体的SDK版本修改
#-libraryjars libs/BmobSDK_V3.4.6_0415.jar

-keepattributes Signature

# 不混淆BmobSDK
-keep class cn.bmob.v3.** {*;}

# 保证继承自BmobObject、BmobUser类的JavaBean不被混淆
-keep class * extends cn.bmob.v3.BmobObject {
    *;
}

