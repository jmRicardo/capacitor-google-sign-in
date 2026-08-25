# capacitor-google-sign-in

This plugin was created using the latest implementation available for Android and iOS.
The idea of this plugin is to use it to obtain the user’s token, and the rest of the logic is managed by our backend.
The plugin returns/deserializes the token to obtain the basic data, but I recommend doing this on your backend and validating it if possible.

##  Prerequisites

To use the plugin, you need a Google Cloud account:

For both, you need to obtain an OAuth client ID.

iOS Documentation: https://developers.google.com/identity/sign-in/ios/start-integrating

For iOS, you need to add both GIDClientID and GIDServerClientID (optional) to the info.plist.

Android Documentation: https://developer.android.com/identity/sign-in/credential-manager-siwg

For Android, you need to configure the ID in the capacitor config file as follows:

"GoogleSignIn": {
"AndroidServerClientId": "MY_CLIENT_ID"
}

Because I am using the latest dependencies in the Android integration, the project needs to compile to version 36 for it to work.

## ProGuard / R8 (Android)

Nothing to configure. The plugin ships its own `consumer-rules.pro`, so the keep
rules it needs are merged into your app's R8 configuration automatically when
you build with `minifyEnabled true`.

They are deliberately narrow: only the Capacitor annotation types (which R8
would otherwise drop, taking `PluginHandle.pluginAnnotation` with them and
turning any `checkPermissions()` call into a fatal NPE) and the plugin's
`@PluginMethod` entry points. Everything else in the plugin stays obfuscated,
and you do not need the usual catch-all
`-keep class * extends java.lang.annotation.Annotation` in your app.

## Extra

Additionally, with this plugin, you can request permissions to access Google resources. Again, this plugin only handles obtaining the token; the rest of the logic is done on the backend. There are two methods: one to check the status and another to request it. Both methods accept an array of scopes:

Example of a scope format:

const scopes = ['https://www.googleapis.com/auth/calendar']

## Breaking change in 0.9.0

`identityToken` now carries the **OIDC ID token** — the JWT signed by Google — on both
platforms. That is the field, and the only field, a backend can verify.

Up to 0.8.3 the response lied about what it held:

| Field | Up to 0.8.3 | From 0.9.0 |
|---|---|---|
| `identityToken` | iOS: the *access* token (`ya29.…`, not a JWT). Android: never set. | The ID token, both platforms. |
| `authorizationCode` | The ID token on both platforms. | **Removed.** |
| `accessToken` | — | The OAuth access token. iOS only, empty on Android. |
| `serverAuthCode` | iOS only, and missing from `restorePreviousSignIn`. | Same on both entry points, empty when not available. |

The practical consequence of the old behaviour was that a backend reading `identityToken`
had nothing verifiable to work with, so sign-in tended to be "validated" by trusting the
`user` id straight off the request body. That is not authentication: `user` travels
unsigned and anyone can type it. Read the subject off the verified `identityToken`.

**Migrating:** wherever you sent `authorizationCode` to your backend as proof of identity,
send `identityToken` instead. If your backend still accepts both while old app versions are
out there, it can keep taking whichever of the two parses and validates as a JWT.

Every field is now a plain `string`; anything the platform cannot provide comes back empty
instead of `null`.

Also in 0.9.0: neither platform logs the sign-in response any more. It contains the ID
token, and logcat is readable by anyone with adb on a debuggable build.

## Install

```bash
npm install capacitor-google-sign-in
npx cap sync
```

## API

<docgen-index>

* [`handleSignInButton()`](#handlesigninbutton)
* [`signOut()`](#signout)
* [`restorePreviousSignIn()`](#restoreprevioussignin)
* [`checkScopes(...)`](#checkscopes)
* [`requestScopes(...)`](#requestscopes)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### handleSignInButton()

```typescript
handleSignInButton() => Promise<SignInWithGoogleResponse>
```

**Returns:** <code>Promise&lt;<a href="#signinwithgoogleresponse">SignInWithGoogleResponse</a>&gt;</code>

--------------------


### signOut()

```typescript
signOut() => Promise<void>
```

--------------------


### restorePreviousSignIn()

```typescript
restorePreviousSignIn() => Promise<void>
```

--------------------


### checkScopes(...)

```typescript
checkScopes(options: { scopes: string[]; }) => Promise<{ value: boolean; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ scopes: string[]; }</code> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### requestScopes(...)

```typescript
requestScopes(options: { scopes: string[]; }) => Promise<{ value: boolean; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ scopes: string[]; }</code> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### Interfaces


#### SignInWithGoogleResponse

Every field is a plain string. Fields the platform cannot provide come back empty
instead of null, so callers only ever have one "missing" case to handle.

| Prop           | Type                                                                                                                                                     |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`response`** | <code>{ user: string; email: string; givenName: string; familyName: string; identityToken: string; accessToken: string; serverAuthCode: string; }</code> |

</docgen-api>
