export interface GoogleSignInPlugin {
  handleSignInButton(): Promise<SignInWithGoogleResponse>;
  signOut(): Promise<void>;
  restorePreviousSignIn(): Promise<void>;
  checkScopes(options: { scopes: string[] }): Promise<{ value: boolean }>;
  requestScopes(options: { scopes: string[] }): Promise<{ value: boolean }>;
}

/**
 * Every field is a plain string. Fields the platform cannot provide come back empty
 * instead of null, so callers only ever have one "missing" case to handle.
 */
export interface SignInWithGoogleResponse {
  response: {
    /**
     * Stable Google account id (the `sub` claim). Never trust it on its own: it travels
     * unsigned, so a backend has to read the subject off a verified `identityToken`.
     */
    user: string;
    email: string;
    givenName: string;
    familyName: string;
    /**
     * OIDC ID token: a JWT signed by Google, and the only field a backend can verify.
     * Its `aud` is the server client id on Android and the iOS client id on iOS, so a
     * backend serving both has to accept either one.
     */
    identityToken: string;
    /**
     * OAuth access token, to call Google APIs from the app. iOS only: on Android,
     * Credential Manager never issues one, so it comes back empty.
     */
    accessToken: string;
    /**
     * One-time code to exchange for tokens on your own backend. iOS only, and only when
     * `GIDServerClientID` is set. Empty otherwise.
     */
    serverAuthCode: string;
  };
}
