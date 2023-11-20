import Foundation
import Capacitor
import GoogleSignIn

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GoogleSignInPlugin)
public class GoogleSignInPlugin: CAPPlugin {
    private let implementation = GoogleSignIn()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func handleSignInButton(_ call: CAPPluginCall) {
        
        GIDSignIn.sharedInstance.signIn(
            withPresenting: (self.bridge?.viewController)! ) { signInResult, error in
                guard let result = signInResult else {
                    // Inspect error
                    call.reject(error!.localizedDescription)
                    return
                }
                               
                call.resolve([
                    "response": [
                        "user": result.user.userID ?? "",
                        "email": result.user.profile?.email ?? "",
                        "givenName": result.user.profile?.givenName ?? "",
                        "familyName": result.user.profile?.familyName ?? "",
                        "identityToken": result.user.accessToken.tokenString,
                        "authorizationCode": result.user.idToken?.tokenString ?? ""
                    ]
                ])
            }
    }
    
    @objc func signOut(_ call: CAPPluginCall) {
        GIDSignIn.sharedInstance.signOut()
        call.resolve()
    }
    
    @objc func restorePreviousSignIn(_ call: CAPPluginCall) {
        GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
            if error != nil || user == nil {
                // Show the app's signed-out state.
                call.reject("No session")
            } else {
                // Show the app's signed-in state.
                guard let user = user else {
                    // Show the app's signed-out state.
                    call.reject("User without data")
                    return
                }
                call.resolve([
                    "response": [
                        "user": user.userID ?? "",
                        "email": user.profile?.email ?? "",
                        "givenName": user.profile?.givenName ?? "",
                        "familyName": user.profile?.familyName ?? "",
                        "identityToken": user.accessToken.tokenString,
                        "authorizationCode": user.idToken?.tokenString ?? ""
                    ]
                ])
            }
        }
    }
}
