import { GoogleSignIn } from 'capacitor-google-sign-in';

const output = document.querySelector('#output');
const field = (id) => document.querySelector(`#${id}`).value.trim() || undefined;

// A real app asks its backend for the nonce. Here it is random, just to check that the
// ID token carries it back.
function randomNonce() {
  const bytes = crypto.getRandomValues(new Uint8Array(32));
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

function claims(idToken) {
  const payload = idToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  return JSON.parse(decodeURIComponent(escape(atob(payload))));
}

function show(value) {
  output.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

async function signIn(options) {
  show('Signing in…');
  try {
    const result = await GoogleSignIn.signIn(options);
    const token = claims(result.idToken);
    // The raw token stays out of the screen: only what is needed to check the contract.
    show({
      nonceMatches: options.nonce !== undefined && token.nonce === options.nonce,
      claims: {
        iss: token.iss,
        aud: token.aud,
        azp: token.azp,
        sub: token.sub,
        email: token.email,
        email_verified: token.email_verified,
        hd: token.hd,
        nonce: token.nonce,
        exp: new Date(token.exp * 1000).toISOString(),
      },
      result: { ...result, idToken: `${result.idToken.length} chars` },
    });
  } catch (error) {
    show({ code: error.code, message: error.message });
  }
}

document
  .querySelector('#signIn')
  .addEventListener('click', () =>
    signIn({ nonce: randomNonce(), hostedDomain: field('hostedDomain'), hint: field('hint') }),
  );
document
  .querySelector('#bottomSheet')
  .addEventListener('click', () =>
    signIn({ nonce: randomNonce(), hostedDomain: field('hostedDomain'), android: { flow: 'bottomSheet' } }),
  );
document.querySelector('#noNonce').addEventListener('click', () => signIn({}));
document.querySelector('#signOut').addEventListener('click', async () => {
  try {
    await GoogleSignIn.signOut();
    show('Signed out.');
  } catch (error) {
    show({ code: error.code, message: error.message });
  }
});
