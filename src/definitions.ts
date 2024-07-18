export interface GoogleSignInPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  handleSignInButton(): Promise<SignInWithGoogleResponse>;
  signOut(): Promise<void>;
  restorePreviousSignIn(): Promise<void>;
  checkScope(options: { scope: string }): Promise<{ value: boolean }>;
  requestScope(options: { scope: string }): Promise<{ value: boolean }>;
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

