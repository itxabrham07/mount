# Add project specific ProGuard rules here.
-dontobfuscate

# Keep Xposed / LSPosed API references (provided at runtime by LSPosed framework)
-dontwarn de.robv.android.xposed.**
-keep class de.robv.android.xposed.** { *; }

# Keep LSPosed entry point class declared in assets/xposed_init and all app classes for hook/test compatibility
-keep class com.hollow.mount.** { *; }
