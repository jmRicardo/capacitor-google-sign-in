# capacitor-google-sign-in

Native Sign in with Google for Capacitor 8 apps on Android and iOS.

The plugin does one thing: it gets a Google **ID token bound to a nonce your backend
issued**. Your backend verifies that token and opens its own session. Nothing else in the
response is proof of identity.

- **Android:** Credential Manager (`androidx.credentials` 1.6.0, `googleid` 1.2.1).
- **iOS:** GoogleSignIn 10.1.0 through Swift Package Manager, or 9.2.0 through CocoaPods.
- **Web:** not supported on purpose. See [Web](#web).

Upgrading from 0.9.x? Every method changed: read the [CHANGELOG](CHANGELOG.md).

## Install

```bash
npm install capacitor-google-sign-in@1.0.0
npx cap sync
```

Requires `@capacitor/core` 8, iOS 15+ and Android `compileSdk` 36.

## Setup

### Google Cloud console

Create three OAuth clients in the same project:

| Client | For | Needs |
|---|---|---|
| **Web** (the "server client id") | Your backend, Android, and `GIDServerClientID` on iOS | Nothing else |
| **Android** | Lets Google recognise the app | Package name and the SHA-1 of **every** signing key: debug, upload and Play App Signing |
| **iOS** | `GIDClientID` | Bundle id |

The Android client id is never written in the app: Google matches it by package and SHA-1.
A missing SHA-1 shows up as `config_error`.

### iOS

Add to `Info.plist`:

```xml
<key>GIDClientID</key>
<string>IOS_CLIENT_ID.apps.googleusercontent.com</string>
<!-- Optional. With it, the ID token's aud is the web client id (see below). -->
<key>GIDServerClientID</key>
<string>WEB_CLIENT_ID.apps.googleusercontent.com</string>
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array>
      <!-- The iOS client id, reversed -->
      <string>com.googleusercontent.apps.IOS_CLIENT_ID</string>
    </array>
  </dict>
</array>
```

`GIDHostedDomain` also works and acts as the default `hostedDomain`. No `AppDelegate`
changes are needed: the plugin forwards Capacitor's open-URL notification to GoogleSignIn.

- **Swift Package Manager** (the default for new Capacitor 8 apps): `npx cap sync` adds the
  plugin to `CapApp-SPM`. GoogleSignIn is pinned to 10.1.0.
- **CocoaPods:** the podspec pins `GoogleSignIn` 9.2.0, the newest version on the CocoaPods
  trunk (10.x is only published for SPM). Nonce support starts in 9.0.0, so both behave the
  same. The trunk goes read-only on 2026-12-02, so plan the move to SPM.

### Android

In `capacitor.config.ts`, the **web** client id (not the Android one):

```ts
plugins: {
  GoogleSignIn: {
    AndroidServerClientId: 'WEB_CLIENT_ID.apps.googleusercontent.com',
  },
},
```

Without it, `signIn` rejects with `config_error` instead of crashing.

## Usage

```ts
import { GoogleSignIn } from 'capacitor-google-sign-in';

// 1. Your backend issues a single-use nonce for this attempt.
const { nonce } = await api.post('/auth/google/nonce');

try {
  // 2. Google puts it in the ID token.
  const { idToken } = await GoogleSignIn.signIn({ nonce });
  // 3. Your backend verifies the token, nonce included, and opens a session.
  await api.post('/auth/google', { idToken });
} catch (error) {
  if (error.code === 'canceled') return; // the person closed the sheet
  showError(error.code);
}

// On logout, after ending your own session:
await GoogleSignIn.signOut();
```

`signOut` makes the device forget the account, so Android does not auto-select it next
time. It does not end your backend session.

### What the backend must check

1. Signature against Google's JWKS, `iss` is `accounts.google.com` or
   `https://accounts.google.com`, and `exp`.
2. `aud` is one of **your** client ids:

   | Platform | `aud` | `azp` |
   |---|---|---|
   | Android | Web client id | Android client id |
   | iOS with `GIDServerClientID` | Web client id | iOS client id |
   | iOS without `GIDServerClientID` | iOS client id | iOS client id |
   | Web (GIS) | Web client id | Web client id |

   On iOS, GoogleSignIn sends `audience=<GIDServerClientID>` to Google (`GIDSignIn.m`),
   which is why `aud` changes; the example app prints both claims if you want to see it.
   Accept every client id you ship.
3. `nonce` is the one you issued, has not been used, and has not expired. Mark it used in
   the same step.
4. `email_verified` is `true` before you trust `email`, and `hd` if you restrict domains:
   the `hostedDomain` option only filters the account picker.

Read the subject from the verified token's `sub`. Never trust names, emails or ids the app
sends next to the token.

### Errors

`signIn` and `signOut` reject with `error.code` set to one of:

| `code` | Meaning | What the app usually does |
|---|---|---|
| `canceled` | The person closed Google's UI | Nothing |
| `no_credential` | No Google account to offer (none on the device, or none authorized with `filterByAuthorizedAccounts`) | Offer to add an account, or fall back to the button flow |
| `config_error` | Missing nonce, client id or URL scheme; client id or SHA-1 not matching the console; no Credential Manager on the device | Log it: it is a bug or a setup problem |
| `network` | No connection, or it dropped mid-flow | Ask to retry |
| `unknown` | Anything else, including a second `signIn` while one is running | Generic error; see `error.message` |

### Android: button or bottom sheet

```ts
// Default: the full-screen "Sign in with Google" flow, for a button the person tapped.
await GoogleSignIn.signIn({ nonce });

// Credential Manager's bottom sheet, for example on app start:
await GoogleSignIn.signIn({
  nonce,
  android: { flow: 'bottomSheet', filterByAuthorizedAccounts: true, autoSelect: true },
});
```

With `filterByAuthorizedAccounts`, an account that never signed in to the app is not
offered and the call rejects with `no_credential`: fall back to the button. `hint` is iOS
only; `android` is ignored on iOS.

## Web

Not in the plugin: `signIn` and `signOut` reject with `UNIMPLEMENTED` on the web.

- Google Identity Services cannot be opened from your own button and hand back an ID token:
  it needs its own button (`renderButton`) or One Tap (`prompt`, which goes through FedCM in
  Chrome).
- Inside the app's WebView it does not work at all: Google blocks OAuth in embedded
  WebViews.

So the web app uses GIS directly, with the same nonce:

```js
google.accounts.id.initialize({
  client_id: 'WEB_CLIENT_ID.apps.googleusercontent.com',
  nonce, // from your backend, as in the app
  callback: ({ credential }) => api.post('/auth/google', { idToken: credential }),
});
google.accounts.id.renderButton(document.querySelector('#google'), { type: 'standard' });

// On logout:
google.accounts.id.disableAutoSelect();
```

Pick one or the other with `Capacitor.isNativePlatform()`. The backend receives the same
kind of token from both.

## What is not here

- **Restoring a previous session** (`restorePreviousSignIn` up to 0.9): a restored ID token
  carries no fresh nonce, so a backend that checks nonces must reject it. Keep your own
  session (refresh token) instead.
- **Extra scopes** (`checkScopes` / `requestScopes` up to 0.9): they never worked on
  either platform. If you need Google API access, use a server-side authorization code flow.

## Example app

[`example/`](example) has both native projects and buttons for every path, including a
sign-in without nonce. After a successful sign-in it shows `aud`, `azp`, `hd` and whether
the token's `nonce` matches. See [`example/README.md`](example/README.md).

## ProGuard / R8 (Android)

Nothing to configure. The plugin ships its own `consumer-rules.pro`, so the keep rules it
needs are merged into your app's R8 configuration automatically when you build with
`minifyEnabled true`.

They are deliberately narrow: only the Capacitor annotation types (which R8 would otherwise
drop, taking `PluginHandle.pluginAnnotation` with them and turning any `checkPermissions()`
call into a fatal NPE) and the plugin's `@PluginMethod` entry points. Everything else in the
plugin stays obfuscated, and you do not need the usual catch-all
`-keep class * extends java.lang.annotation.Annotation` in your app.

## API

<docgen-index>

* [`signIn(...)`](#signin)
* [`signOut()`](#signout)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### signIn(...)

```typescript
signIn(options: SignInOptions) => Promise<SignInResult>
```

Sign in with Google. Every call mints a fresh ID token carrying `options.nonce`: nothing
is restored from an earlier session.

Rejects with a `GoogleSignInErrorCode` in `error.code`.

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#signinoptions">SignInOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#signinresult">SignInResult</a>&gt;</code>

--------------------


### signOut()

```typescript
signOut() => Promise<void>
```

Forgets the signed-in account on the device: the Google SDK session on iOS and the
Credential Manager state on Android, so the next sign-in does not auto-select it.
It does not end your own backend session.

--------------------


### Interfaces


#### SignInResult

Every field is a plain string. What the platform cannot provide comes back empty.

| Prop                 | Type                | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| -------------------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`idToken`**        | <code>string</code> | OIDC ID token: a JWT signed by Google, with the `nonce` you passed. It is the only field a backend may trust, after verifying signature, `iss`, `aud`, `exp` and `nonce`. `aud` is the server (web) client id on Android. On iOS, with `GIDServerClientID` in Info.plist the SDK asks Google for `audience=&lt;server client id&gt;`, so `aud` is the server client id and `azp` the iOS one; without it, `aud` is the iOS client id. List every client id you ship in the backend's accepted audiences. |
| **`email`**          | <code>string</code> | For display only: unsigned, read the verified `email` claim on the backend instead.                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **`givenName`**      | <code>string</code> | For display only.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **`familyName`**     | <code>string</code> | For display only.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **`serverAuthCode`** | <code>string</code> | One-time code to exchange on your backend for Google tokens. iOS only, and only when `GIDServerClientID` is set. Empty otherwise.                                                                                                                                                                                                                                                                                                                                                                        |


#### SignInOptions

| Prop               | Type                                                                  | Description                                                                                                                                                                                                                                       |
| ------------------ | --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`nonce`**        | <code>string</code>                                                   | Single-use value issued by your backend for this sign-in attempt. It comes back verbatim in the ID token's `nonce` claim, and the backend must compare it before accepting the token. Required: a sign-in without it rejects with `config_error`. |
| **`hostedDomain`** | <code>string</code>                                                   | Google Workspace domain to restrict the account picker to (`hd`). Only a UI filter: the backend still has to check the `hd` claim.                                                                                                                |
| **`hint`**         | <code>string</code>                                                   | iOS only: email or account id to preselect in Google's sign-in page. Ignored on Android.                                                                                                                                                          |
| **`android`**      | <code><a href="#androidsigninoptions">AndroidSignInOptions</a></code> | Android only. Ignored on iOS.                                                                                                                                                                                                                     |


#### AndroidSignInOptions

| Prop                             | Type                                   | Description                                                                                                                                                                                                       |
| -------------------------------- | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`flow`**                       | <code>'button' \| 'bottomSheet'</code> | - `button` (default): the full-screen "Sign in with Google" flow (`GetSignInWithGoogleOption`), meant for a button the person tapped. - `bottomSheet`: the Credential Manager bottom sheet (`GetGoogleIdOption`). |
| **`filterByAuthorizedAccounts`** | <code>boolean</code>                   | `bottomSheet` only: offer just the accounts that already signed in to this app. Default `false`. When none match, the call rejects with `no_credential`.                                                          |
| **`autoSelect`**                 | <code>boolean</code>                   | `bottomSheet` only: sign in without a tap when exactly one authorized account matches. Default `false`.                                                                                                           |

</docgen-api>
