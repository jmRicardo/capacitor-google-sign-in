import XCTest
import GoogleSignIn
@testable import GoogleSignInPlugin

final class GoogleSignInErrorsTests: XCTestCase {
    func testCanceledMapsToCanceled() {
        let error = NSError(domain: GIDSignInError.errorDomain, code: GIDSignInError.Code.canceled.rawValue)
        XCTAssertEqual(GoogleSignInErrors.code(for: error), .canceled)
    }

    func testNoAuthInKeychainMapsToNoCredential() {
        let error = NSError(domain: GIDSignInError.errorDomain, code: GIDSignInError.Code.hasNoAuthInKeychain.rawValue)
        XCTAssertEqual(GoogleSignInErrors.code(for: error), .noCredential)
    }

    func testOtherSignInErrorsMapToUnknown() {
        let error = NSError(domain: GIDSignInError.errorDomain, code: GIDSignInError.Code.EMM.rawValue)
        XCTAssertEqual(GoogleSignInErrors.code(for: error), .unknown)
    }

    func testURLErrorsMapToNetwork() {
        XCTAssertEqual(GoogleSignInErrors.code(for: URLError(.notConnectedToInternet)), .network)
    }

    func testAppAuthNetworkErrorWrappingURLErrorMapsToNetwork() {
        let underlying = NSError(domain: NSURLErrorDomain, code: NSURLErrorTimedOut)
        let error = NSError(
            domain: GoogleSignInErrors.appAuthGeneralDomain,
            code: GoogleSignInErrors.appAuthNetworkErrorCode,
            userInfo: [NSUnderlyingErrorKey: underlying])
        XCTAssertEqual(GoogleSignInErrors.code(for: error), .network)
    }

    func testAppAuthGeneralErrorWithOtherCodeMapsToUnknown() {
        let error = NSError(domain: GoogleSignInErrors.appAuthGeneralDomain, code: -6)
        XCTAssertEqual(GoogleSignInErrors.code(for: error), .unknown)
    }

    func testCodesMatchTheTypeScriptContract() {
        XCTAssertEqual(
            [GoogleSignInErrorCode.canceled, .noCredential, .configError, .network, .unknown].map(\.rawValue),
            ["canceled", "no_credential", "config_error", "network", "unknown"])
    }
}

final class GoogleSignInConfigCheckTests: XCTestCase {
    private let clientID = "1234-abc.apps.googleusercontent.com"

    func testDeclaredReversedClientIDPasses() {
        let urlTypes: [[String: Any]] = [["CFBundleURLSchemes": ["com.googleusercontent.apps.1234-ABC"]]]
        XCTAssertNil(GoogleSignInConfigCheck.missingURLScheme(clientID: clientID, urlTypes: urlTypes))
    }

    func testMissingSchemeIsReported() {
        let urlTypes: [[String: Any]] = [["CFBundleURLSchemes": ["myapp"]]]
        XCTAssertEqual(
            GoogleSignInConfigCheck.missingURLScheme(clientID: clientID, urlTypes: urlTypes),
            "com.googleusercontent.apps.1234-abc")
    }

    func testNoURLTypesAtAllIsReported() {
        XCTAssertEqual(
            GoogleSignInConfigCheck.missingURLScheme(clientID: clientID, urlTypes: nil),
            "com.googleusercontent.apps.1234-abc")
    }
}
