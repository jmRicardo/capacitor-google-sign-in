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

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

@CapacitorPlugin(name = "GoogleSignIn")
public class GoogleSignInPlugin extends Plugin {

    private GetSignInWithGoogleOption signInWithGoogleOption;
    private CredentialManager credentialManager;
    private final GoogleSignIn googleSignIn = new GoogleSignIn();

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
                            var parsed = googleSignIn.parseCredentialData(data);
                            if (parsed!=null) {
                                call.resolve(parsed);
                            } else {
                                call.reject("Cannot parse credential data");
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


