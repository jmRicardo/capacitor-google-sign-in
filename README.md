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

Because I am using the latest dependencies in the Android integration, the project needs to compile to version 35 for it to work.

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

| Prop           | Type                                                                                                                                                                                                           |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`response`** | <code>{ user: string \| null; email: string \| null; givenName: string \| null; familyName: string \| null; identityToken: string; authorizationCode: string \| null; serverAuthCode: string \| null; }</code> |

</docgen-api>
