# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

# SqlCipher
-keep class net.zetetic.database.** { *; }

# JOSE
-keepclasseswithmembernames class com.nimbusds.jose.** { *; }

# PXL
-keep class com.pxl.PXLBeamOnBoard.** { *; }

# Warnings
-dontwarn java.awt.Component
-dontwarn java.awt.GraphicsEnvironment
-dontwarn java.awt.HeadlessException
-dontwarn java.awt.Window
-dontwarn org.apache.commons.lang3.builder.EqualsBuilder
-dontwarn org.apache.commons.lang3.builder.HashCodeBuilder
-dontwarn org.apache.commons.lang3.builder.ToStringBuilder
-dontwarn org.apache.commons.lang3.builder.ToStringStyle
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.MustBeClosed

# PXL Warnings
-dontwarn colorspace.ColorSpace$CSEnum
-dontwarn colorspace.ColorSpace
-dontwarn com.radaee.**
-dontwarn icc.ICCProfiler
-dontwarn jj2000.j2k.**
-dontwarn org.jnbis.Bitmap
-dontwarn org.jnbis.WsqDecoder
-dontwarn com.google.android.gms.tasks.Task
-dontwarn com.google.android.gms.tasks.Tasks
-dontwarn com.google.android.play.core.integrity.**

#TODO: Those rules can be removed, when added to the consumer proguard rules of the kmp sdk, Please test release build, when removing.
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

-dontwarn java.awt.**
-dontwarn javax.swing.**
