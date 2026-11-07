export interface GoogleSignInPlugin {
  /**
   * Sign in with Google. Every call mints a fresh ID token carrying `options.nonce`: nothing
   * is restored from an earlier session.
   *
   * Rejects with a `GoogleSignInErrorCode` in `error.code`.
   */
  signIn(options: SignInOptions): Promise<SignInResult>;

  /**
   * Forgets the signed-in account on the device: the Google SDK session on iOS and the
   * Credential Manager state on Android, so the next sign-in does not auto-select it.
   * It does not end your own backend session.
   */
  signOut(): Promise<void>;
}

export interface SignInOptions {
  /**
   * Single-use value issued by your backend for this sign-in attempt. It comes back verbatim
   * in the ID token's `nonce` claim, and the backend must compare it before accepting the
   * token. Required: a sign-in without it rejects with `config_error`.
   */
  nonce: string;
  /**
   * Google Workspace domain to restrict the account picker to (`hd`). Only a UI filter:
   * the backend still has to check the `hd` claim.
   */
  hostedDomain?: string;
  /**
   * iOS only: email or account id to preselect in Google's sign-in page. Ignored on Android.
   */
  hint?: string;
  /** Android only. Ignored on iOS. */
  android?: AndroidSignInOptions;
}

export interface AndroidSignInOptions {
  /**
   * - `button` (default): the full-screen "Sign in with Google" flow
   *   (`GetSignInWithGoogleOption`), meant for a button the person tapped.
   * - `bottomSheet`: the Credential Manager bottom sheet (`GetGoogleIdOption`).
   */
  flow?: 'button' | 'bottomSheet';
  /**
   * `bottomSheet` only: offer just the accounts that already signed in to this app.
   * Default `false`. When none match, the call rejects with `no_credential`.
   */
  filterByAuthorizedAccounts?: boolean;
  /**
   * `bottomSheet` only: sign in without a tap when exactly one authorized account matches.
   * Default `false`.
   */
  autoSelect?: boolean;
}

/**
 * Every field is a plain string. What the platform cannot provide comes back empty.
 */
export interface SignInResult {
  /**
   * OIDC ID token: a JWT signed by Google, with the `nonce` you passed. It is the only
   * field a backend may trust, after verifying signature, `iss`, `aud`, `exp` and `nonce`.
   *
   * `aud` is the server (web) client id on Android. On iOS, with `GIDServerClientID` in
   * Info.plist the SDK asks Google for `audience=<server client id>`, so `aud` is the server
   * client id and `azp` the iOS one; without it, `aud` is the iOS client id. List every
   * client id you ship in the backend's accepted audiences.
   */
  idToken: string;
  /** For display only: unsigned, read the verified `email` claim on the backend instead. */
  email: string;
  /** For display only. */
  givenName: string;
  /** For display only. */
  familyName: string;
  /**
   * One-time code to exchange on your backend for Google tokens. iOS only, and only when
   * `GIDServerClientID` is set. Empty otherwise.
   */
  serverAuthCode: string;
}

/**
 * Stable values of `error.code` when `signIn` or `signOut` rejects.
 *
 * - `canceled`: the person closed Google's UI. Not an error to show.
 * - `no_credential`: no Google account to offer (none on the device, or none authorized
 *   when `filterByAuthorizedAccounts` is on).
 * - `config_error`: the app or the call is misconfigured: missing client id, URL scheme,
 *   or nonce, a client id or SHA-1 that does not match the Google Cloud console, or a
 *   device without Credential Manager support.
 * - `network`: no connection, or it dropped mid-flow.
 * - `unknown`: anything else, including a second `signIn` while one is in flight. See
 *   `error.message`.
 */
export type GoogleSignInErrorCode = 'canceled' | 'no_credential' | 'config_error' | 'network' | 'unknown';
