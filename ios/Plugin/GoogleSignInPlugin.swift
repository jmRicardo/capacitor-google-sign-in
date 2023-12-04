import Foundation
import Capacitor
import GoogleSignIn

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GoogleSignInPlugin)
public class GoogleSignInPlugin: CAPPlugin {
    
    private var user: GIDGoogleUser?
    
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
                
                self.user = result.user
                
                let serverAuthCode = result.serverAuthCode
                               
                call.resolve([
                    "response": [
                        "user": result.user.userID ?? "",
                        "email": result.user.profile?.email ?? "",
                        "givenName": result.user.profile?.givenName ?? "",
                        "familyName": result.user.profile?.familyName ?? "",
                        "identityToken": result.user.accessToken.tokenString,
                        "authorizationCode": result.user.idToken?.tokenString ?? "",
                        "serverAuthCode": result.serverAuthCode
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
                let auth = user.idToken?.tokenString
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
    
    @objc func checkScope(_ call: CAPPluginCall) {
       
        let driveScope = call.getString("scope") ?? ""
        let grantedScopes = user?.grantedScopes
        if grantedScopes == nil || !grantedScopes!.contains(driveScope) {
            // Request additional Drive scope.
        }
        call.resolve()
    }
    
    @objc func requestScope(_ call: CAPPluginCall) {
       
        let additionalScopes = [call.getString("scope") ?? ""]
        guard let currentUser = GIDSignIn.sharedInstance.currentUser else {
            call.reject("User not signed in")
            return ;  /* Not signed in. */
        }
        
        currentUser.addScopes(additionalScopes, presenting: (self.bridge?.viewController)!) { signInResult, error in
            guard error == nil else { return }
            guard let signInResult = signInResult else {
                // Inspect error
                call.reject(error!.localizedDescription)
                return
            }
            
            call.resolve()
            // Check if the user granted access to the scopes you requested.
        }
        
        call.resolve()
    }
}
