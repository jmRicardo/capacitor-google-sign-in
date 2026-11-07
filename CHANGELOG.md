# Changelog

## 1.0.0

A rewrite of the API around one job: get an ID token bound to a nonce the backend issued.
Every method changed. Apps that cannot move yet can stay on 0.9.x.

### Breaking changes from 0.9.x

| 0.9.x | 1.0.0 |
|---|---|
| `handleSignInButton()` | `signIn({ nonce, hostedDomain?, hint?, android? })`. `nonce` is required: without it the call rejects with `config_error`. |
| Response wrapped in `{ response: { … } }` | Flat result: `{ idToken, email, givenName, familyName, serverAuthCode }`. |
| `response.identityToken` | `idToken`. |
| `response.user` (the `sub`, unsigned) | **Removed.** Read `sub` from the verified token on the backend. |
| `response.accessToken` (iOS only) | **Removed.** No scopes beyond sign-in, so there is nothing to call Google with. |
| `restorePreviousSignIn()` | **Removed.** A restored ID token has no fresh nonce, so it cannot be a login. Keep your own session instead. |
| `checkScopes()` / `requestScopes()` | **Removed.** They never worked: iOS registered other names (`checkScope`, `requestScope`), and Android never resolved `requestScopes`. |
| `signOut()` rejected on Android ("not implemented") | Clears the Credential Manager state on Android. |
| Rejections with free text (`"Error! …"`) | `error.code` is one of `canceled`, `no_credential`, `config_error`, `network`, `unknown`. |
| Missing `AndroidServerClientId` crashed the app (NPE) | Rejects with `config_error`. Same on iOS for a missing `GIDClientID` or URL scheme, which made GoogleSignIn raise an exception. |
| iOS through CocoaPods only (`ios/Plugin`, `CAP_PLUGIN` macro) | Swift Package Manager (`Package.swift`, `CAPBridgedPlugin`). The podspec stays for CocoaPods apps. |
| `GoogleSignIn` unpinned in the podspec | Pinned: 10.1.0 in `Package.swift`, 9.2.0 in the podspec (the newest on the CocoaPods trunk). Both support the nonce. Needs iOS 15. |
| `peerDependencies: "@capacitor/core": ">=8.0.0"` | `^8.0.0`. |
| Web methods resolved to `undefined` | Reject with `UNIMPLEMENTED`. Use Google Identity Services directly on the web. |

### Added

- `hostedDomain` on both platforms (`hd`), and `hint` on iOS.
- Android: `android.flow: 'bottomSheet'` uses `GetGoogleIdOption`, with
  `filterByAuthorizedAccounts` and `autoSelect`.
- Tests: a contract test that compares methods, plugin name and error codes between TS,
  Swift, Java and the web stub (`npm test`), Swift unit tests for error mapping and the URL
  scheme check, and Java unit tests for error mapping.
- The README documents what the backend must check, including `aud` and `azp` per platform.

### Fixed

- iOS presents on the main queue and has no force unwraps.
- Android no longer starts a thread per sign-in: callbacks run on the main executor.
- Android dropped `play-services-auth`, which only the removed scope methods used.

### Migrating

```ts
// 0.9.x
const { response } = await GoogleSignIn.handleSignInButton();
await api.post('/login/google', { identityToken: response.identityToken });

// 1.0.0
const { nonce } = await api.post('/auth/google/nonce');
const { idToken } = await GoogleSignIn.signIn({ nonce });
await api.post('/auth/google', { idToken });
```

The backend has to issue the nonce and compare it with the token's `nonce` claim before
accepting it.

## 0.9.0

- `identityToken` carries the OIDC ID token on both platforms. `authorizationCode` was
  removed. iOS adds `accessToken` and `serverAuthCode`.
- Neither platform logs the sign-in response.
