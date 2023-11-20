import { WebPlugin } from '@capacitor/core';

import type {GoogleSignInPlugin, SignInWithGoogleResponse} from './definitions';

export class GoogleSignInWeb extends WebPlugin implements GoogleSignInPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    return options;
  }

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
}
