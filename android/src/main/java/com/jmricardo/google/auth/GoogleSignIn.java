package com.jmricardo.google.auth;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GoogleSignIn {

    public JSObject parseCredentialData(GoogleIdTokenCredential data) {
        var idToken = data.getIdToken();
        try {
            // JWTs are base64url encoded ('-' and '_'), so the standard decoder blows up
            // with IllegalArgumentException on any payload that happens to use them.
            var payload = new JSONObject(
                    new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8));
            JSObject response = new JSObject();
            JSObject user = new JSObject();
            user.put("user", payload.getString("sub"));
            user.put("email", data.getId());
            user.put("givenName", data.getGivenName());
            user.put("familyName", data.getFamilyName());
            // The ID token goes here and nowhere else: it is the JWT a backend can verify.
            // Up to 0.8.3 it was handed out as `authorizationCode` and `identityToken` was
            // never set at all, so no backend could actually validate a sign-in.
            user.put("identityToken", idToken);
            // Credential Manager only ever returns the ID token: there is no access token
            // and no authorization code to hand back.
            user.put("accessToken", "");
            user.put("serverAuthCode", "");
            response.put("response", user);

            // Deliberately not logged: the response carries the ID token, and logcat is
            // readable by anyone with adb on a debuggable build.
            return response;
        } catch (Exception e) {
            // This runs on the executor passed to getCredentialAsync(): anything thrown here
            // is an uncaught exception on a bare thread and kills the process.
            Log.e("GoogleIdTokenCredential", "Cannot parse credential data", e);
            return null;
        }
    }
}
