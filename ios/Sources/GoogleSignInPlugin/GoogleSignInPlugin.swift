import Foundation
import Capacitor
import GoogleSignIn

@objc(GoogleSignInPlugin)
public class GoogleSignInPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "GoogleSignInPlugin"
    public let jsName = "GoogleSignIn"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "signIn", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "signOut", returnType: CAPPluginReturnPromise)
    ]

    // What the SDK read from Info.plist (GIDClientID, GIDServerClientID, GIDHostedDomain).
    // Kept apart because each sign-in overwrites GIDSignIn's configuration with its own hd.
    private var infoPlistConfiguration: GIDConfiguration?
    // Only touched on the main queue.
    private var signInInFlight = false

    override public func load() {
        infoPlistConfiguration = GIDSignIn.sharedInstance.configuration
        NotificationCenter.default.addObserver(
            self, selector: #selector(handleOpenURL(_:)), name: .capacitorOpenURL, object: nil)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    @objc func signIn(_ call: CAPPluginCall) {
        guard let nonce = call.getString("nonce"), !nonce.isEmpty else {
            reject(call, .configError, "signIn needs a nonce issued by your backend.")
            return
        }
        let hostedDomain = nonEmpty(call.getString("hostedDomain"))
        let hint = nonEmpty(call.getString("hint"))

        DispatchQueue.main.async {
            guard !self.signInInFlight else {
                self.reject(call, .unknown, "Another sign-in is already in progress.")
                return
            }
            guard let base = self.infoPlistConfiguration, !base.clientID.isEmpty else {
                self.reject(call, .configError, "GIDClientID is missing from Info.plist.")
                return
            }
            let urlTypes = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes")
            if let scheme = GoogleSignInConfigCheck.missingURLScheme(clientID: base.clientID, urlTypes: urlTypes) {
                self.reject(call, .configError, "Info.plist is missing the URL scheme \(scheme).")
                return
            }
            guard let presenter = self.topViewController() else {
                self.reject(call, .unknown, "There is no view controller to present the sign-in from.")
                return
            }

            GIDSignIn.sharedInstance.configuration = GIDConfiguration(
                clientID: base.clientID,
                serverClientID: base.serverClientID,
                hostedDomain: hostedDomain ?? base.hostedDomain,
                openIDRealm: base.openIDRealm)

            self.signInInFlight = true
            GIDSignIn.sharedInstance.signIn(
                withPresenting: presenter, hint: hint, additionalScopes: nil, nonce: nonce
            ) { result, error in
                self.signInInFlight = false
                guard let result else {
                    let code = error.map(GoogleSignInErrors.code(for:)) ?? .unknown
                    self.reject(call, code, error?.localizedDescription ?? "Sign-in failed.", error)
                    return
                }
                guard let idToken = result.user.idToken?.tokenString, !idToken.isEmpty else {
                    self.reject(call, .unknown, "Google returned no ID token.")
                    return
                }
                call.resolve([
                    "idToken": idToken,
                    "email": result.user.profile?.email ?? "",
                    "givenName": result.user.profile?.givenName ?? "",
                    "familyName": result.user.profile?.familyName ?? "",
                    "serverAuthCode": result.serverAuthCode ?? ""
                ])
            }
        }
    }

    @objc func signOut(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            GIDSignIn.sharedInstance.signOut()
            call.resolve()
        }
    }

    // Google documents handle(_:) as required in the app delegate; listening to Capacitor's
    // notification spares every app from editing AppDelegate.swift.
    @objc private func handleOpenURL(_ notification: Notification) {
        guard let object = notification.object as? [String: Any], let url = object["url"] as? URL else {
            return
        }
        DispatchQueue.main.async {
            _ = GIDSignIn.sharedInstance.handle(url)
        }
    }

    private func topViewController() -> UIViewController? {
        var top = bridge?.viewController
        while let presented = top?.presentedViewController, !presented.isBeingDismissed {
            top = presented
        }
        return top
    }

    private func nonEmpty(_ value: String?) -> String? {
        guard let value, !value.isEmpty else { return nil }
        return value
    }

    private func reject(_ call: CAPPluginCall, _ code: GoogleSignInErrorCode, _ message: String, _ error: Error? = nil) {
        call.reject(message, code.rawValue, error)
    }
}
