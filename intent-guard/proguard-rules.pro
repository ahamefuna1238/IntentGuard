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

# --- IntentGuard Library Security Rules ---

# 1. Keep the Public API
# Ensures the Manager and its configuration methods are not stripped
-keep class com.intent.guard.IntentGuardManager { *; }
-keep class com.intent.guard.request.** { *; }
-keep class com.intent.guard.auth.** { *; }

# 2. Preserve Callback Interfaces
# Prevents obfuscation of listeners implemented by the user
-keep interface com.intent.guard.ResultListener { *; }
-keep interface com.intent.guard.RequestListener { *; }

# 3. Keep Permission Metadata and Models
# Essential for PermissionInfo and Metadata keys used for Intent bundling
-keep class com.intent.guard.permission.** { *; }
-keep class com.intent.guard.core.Metadata { *; }

# 4. View Integrity for BottomSheets
# Prevents R8 from stripping view IDs used for programmatic binding in XML
-keepclassmembers class **.R$id {
    public static int m_headerText;
    public static int m_requester_app_icon;
    public static int m_descriptionText;
    public static int m_permissionList;
    public static int m_approveButton;
    public static int m_cancelButton;
}

# 5. Support for Material 3 Dialogs
# Keeps the necessary constructors for the AuthDialogBuilder
-keep class com.google.android.material.bottomsheet.BottomSheetDialog { *; }