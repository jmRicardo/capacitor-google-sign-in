import { WebPlugin } from '@capacitor/core';

import type { GoogleSignInPlugin, SignInResult } from './definitions';

/**
 * Not implemented on purpose: on the web, use Google Identity Services directly (the official
 * button or One Tap), which takes the same `nonce`. Google blocks OAuth inside embedded
 * WebViews, so GIS cannot replace the native flow in the app either.
 */
export class GoogleSignInWeb extends WebPlugin implements GoogleSignInPlugin {
  async signIn(): Promise<SignInResult> {
    throw this.unimplemented('Not available on the web: use Google Identity Services directly.');
  }

  async signOut(): Promise<void> {
    throw this.unimplemented('Not available on the web: use google.accounts.id.disableAutoSelect().');
  }
}
