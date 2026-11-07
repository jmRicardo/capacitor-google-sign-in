# Example app

A manual test bench for the plugin, with both native projects (iOS through SPM). It links
the plugin from `..`, so build the plugin first.

```bash
npm install && npm run build                    # in the repo root
cd example && npm install && npm run build && npx cap sync
npx cap open ios                                # or: npx cap open android
```

Out of the box there are no client ids, so every sign-in rejects with `config_error`.
That is one of the things to test: the app must not crash.

## Signing in for real

Bundle id and package: `com.jmricardo.googlesignin.example`.

- **iOS:** add `GIDClientID`, optionally `GIDServerClientID`, and the reversed iOS client id
  under `CFBundleURLTypes` to `ios/App/App/Info.plist` (see the main README).
- **Android:** set `plugins.GoogleSignIn.AndroidServerClientId` in `capacitor.config.json` to
  the web client id, and register the SHA-1 of your debug key on an Android client:

  ```bash
  keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android
  ```

Then `npx cap sync` and run. Do not commit the ids.

## What each button checks

| Button | Expected |
|---|---|
| Sign in | Google's UI. On success: `nonceMatches: true`, plus `aud`, `azp`, `hd` and `email_verified` from the token. Closing it gives `canceled`. |
| Sign in with the bottom sheet | Android only. With no account on the device, `no_credential`. |
| Sign in without a nonce | `config_error`, without opening anything. |
| Sign out | Resolves. On Android, the bottom sheet stops auto-selecting the account. |

With `GIDServerClientID` set, iOS should show `aud` = web client id and `azp` = iOS client
id; without it, both are the iOS client id.

The raw ID token never reaches the screen, only its length and the claims above.
