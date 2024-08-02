export interface GoogleSignInPlugin {
  handleSignInButton(): Promise<SignInWithGoogleResponse>;
  signOut(): Promise<void>;
  restorePreviousSignIn(): Promise<void>;
  checkScopes(options: { scopes: string[] }): Promise<{ value: boolean }>;
  requestScopes(options: { scopes: string[] }): Promise<{ value: boolean }>;
}

export interface SignInWithGoogleResponse {
  response: {
    user: string | null;
    email: string | null;
    givenName: string | null;
    familyName: string | null;
    identityToken: string;
    authorizationCode: string | null;
    serverAuthCode: string | null;
  };
}

