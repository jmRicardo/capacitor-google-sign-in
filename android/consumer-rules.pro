# ProGuard/R8 rules consumed by any app that depends on this plugin.
# They are merged into the app's configuration automatically via
# `consumerProguardFiles`, so the app does not need to add them by hand.
#
# Keep them as narrow as possible: every rule here is obfuscation the
# consuming app gives up.

# Capacitor resolves the plugin's @CapacitorPlugin annotation reflectively in
# PluginHandle. If R8 removes the annotation *types*, it also strips the
# PluginHandle.pluginAnnotation / legacyPluginAnnotation fields; then
# getPluginAnnotation() folds to a constant null and Bridge.getPermissionStates()
# dereferences it -> fatal NPE on any checkPermissions() call.
# The alternative usually copy-pasted into apps is
# `-keep class * extends java.lang.annotation.Annotation`, which keeps every
# annotation type in the app. These three keeps cost nothing by comparison.
-keep @interface com.getcapacitor.annotation.**
-keep @interface com.getcapacitor.NativePlugin
-keep @interface com.getcapacitor.PluginMethod

# The annotations above must still be readable at runtime once the plugin class
# is kept.
-keepattributes RuntimeVisibleAnnotations

# The bridge dispatches @PluginMethod methods by name. Only the entry points
# need their names; the rest of the plugin can be renamed and shrunk freely.
-keepclassmembers class com.jmricardo.google.auth.GoogleSignInPlugin {
    @com.getcapacitor.PluginMethod public <methods>;
}
