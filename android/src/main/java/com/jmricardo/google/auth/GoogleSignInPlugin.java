package com.jmricardo.google.auth;

import static android.content.ContentValues.TAG;

import android.os.CancellationSignal;
import android.util.Log;

import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;

@CapacitorPlugin(name = "GoogleSignIn")
public class GoogleSignInPlugin extends Plugin {

    private GetSignInWithGoogleOption signInWithGoogleOption;
    private CredentialManager credentialManager;

    @PluginMethod
    public void handleSignInButton(PluginCall call) {
        var request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build();

        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> {
            Log.d(TAG, "Credential request was cancelled");
            call.reject("Credential request was cancelled");
        });

        credentialManager.getCredentialAsync(
                this.getContext(),
                request,
                cancellationSignal,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        var data = handleSignIn(result);
                        if (data!=null) {
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
                                call.resolve(response);
                            } catch (JSONException e) {
                                call.reject("Error parsing credential");
                            }
                        } else {
                            call.reject("Unexpected type of credential");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        call.reject("Error! " + e);
                    }
                });
    }

    @Override
    public void load() {
        var config = this.getConfig();
        var serverClientId = config.getString("AndroidServerClientId");
        if(serverClientId != null)
        {
            credentialManager = CredentialManager.create(this.getContext());
            signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(serverClientId)
                    .build();
        }
    }

    public GoogleIdTokenCredential handleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        if (credential instanceof CustomCredential customCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                return GoogleIdTokenCredential.createFrom(customCredential.getData());
            } else {
                Log.e(TAG, "Unexpected type of credential");
            }
        } else {
            Log.e(TAG, "Unexpected type of credential");
        }
        return null;
    }
}


