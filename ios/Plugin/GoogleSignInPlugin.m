#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(GoogleSignInPlugin, "GoogleSignIn",
           CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(handleSignInButton, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(signOut, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(restorePreviousSignIn, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(checkScope, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestScope, CAPPluginReturnPromise);
)
