package com.jmricardo.google.auth;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSignIn {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public JSObject parseCredentialData(GoogleIdTokenCredential data) {
        var idToken = data.getIdToken();
        try {
            var payload = new JSONObject(new String(java.util.Base64.getDecoder().decode(idToken.split("\\.")[1])));
            JSObject response = new JSObject();
            JSObject user = new JSObject();
            user.put("user", payload.getString("sub"));
            user.put("email", data.getId());
            user.put("givenName", data.getGivenName());
            user.put("familyName", data.getFamilyName());
            user.put("authorizationCode", data.getIdToken());
            response.put("response", user);

            Log.i("GoogleIdTokenCredential", response.toString());
            return response;
        } catch (JSONException ignored) {
            return null;
        }
    }
}
