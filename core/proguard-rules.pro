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

-keep class per.goweii.codex.CodeFormat { *; }
-keep class per.goweii.codex.CodeResult { *; }
-keep class per.goweii.codex.CodeProcessor { *; }
-keep class per.goweii.codex.scanner.decorator.ScanDecorator { *; }
-keep class per.goweii.codex.scanner.CodeScanner { *; }
-keep class per.goweii.codex.decoder.CodeDecoder { *; }
-keep class per.goweii.codex.decoder.DecodeProcessor { *; }
-keep class per.goweii.codex.encoder.CodeEncoder { *; }
-keep class per.goweii.codex.encoder.EncodeProcessor { *; }
