import { WebPlugin } from '@capacitor/core';

import type {GoogleSignInPlugin, SignInWithGoogleResponse} from './definitions';

export class GoogleSignInWeb extends WebPlugin implements GoogleSignInPlugin {

  // @ts-ignore
  async handleSignInButton(): Promise<SignInWithGoogleResponse> {
    this.unimplemented('Not implemented on web.')
  }

  async restorePreviousSignIn(): Promise<void> {
    this.unimplemented('Not implemented on web.')
  }

  async signOut(): Promise<void> {
    this.unimplemented('Not implemented on web.')
  }

  // @ts-ignore
  checkScopes(options: { scopes: string[] }): Promise<{ value: boolean }> {
    this.unimplemented('Not implemented on web.')
  }

  // @ts-ignore
  requestScopes(options: { scopes: string[] }): Promise<{ value: boolean }> {
    this.unimplemented('Not implemented on web.')
  }
}
