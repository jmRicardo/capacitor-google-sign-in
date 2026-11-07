import Foundation
import GoogleSignIn

/// Values of `error.code` on the JS side. Mirrors `GoogleSignInErrorCode` in definitions.ts.
enum GoogleSignInErrorCode: String {
    case canceled
    case noCredential = "no_credential"
    case configError = "config_error"
    case network
    case unknown
}

enum GoogleSignInErrors {
    // AppAuth's OIDGeneralErrorDomain / OIDErrorCodeNetworkError. GoogleSignIn passes token
    // exchange errors through untouched, and AppAuth is not a direct dependency to import.
    static let appAuthGeneralDomain = "org.openid.appauth.general"
    static let appAuthNetworkErrorCode = -5

    static func code(for error: Error) -> GoogleSignInErrorCode {
        let nsError = error as NSError
        if nsError.domain == GIDSignInError.errorDomain {
            switch nsError.code {
            case GIDSignInError.Code.canceled.rawValue:
                return .canceled
            case GIDSignInError.Code.hasNoAuthInKeychain.rawValue:
                return .noCredential
            default:
                return .unknown
            }
        }
        return isNetwork(nsError) ? .network : .unknown
    }

    private static func isNetwork(_ error: NSError) -> Bool {
        if error.domain == NSURLErrorDomain {
            return true
        }
        if error.domain == appAuthGeneralDomain && error.code == appAuthNetworkErrorCode {
            return true
        }
        if let underlying = error.userInfo[NSUnderlyingErrorKey] as? NSError {
            return isNetwork(underlying)
        }
        return false
    }
}

enum GoogleSignInConfigCheck {
    /// GoogleSignIn raises an uncatchable NSException when the reversed client id is not a
    /// registered URL scheme. Returns the missing scheme, or nil when it is declared.
    static func missingURLScheme(clientID: String, urlTypes: Any?) -> String? {
        let expected = clientID.split(separator: ".").reversed().joined(separator: ".").lowercased()
        let declared = (urlTypes as? [[String: Any]] ?? [])
            .flatMap { $0["CFBundleURLSchemes"] as? [String] ?? [] }
            .map { $0.lowercased() }
        return declared.contains(expected) ? nil : expected
    }
}
